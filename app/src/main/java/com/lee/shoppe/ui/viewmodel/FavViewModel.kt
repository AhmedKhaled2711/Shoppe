package com.lee.shoppe.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lee.shoppe.data.model.DraftOrderResponse
import com.lee.shoppe.data.model.Product
import com.lee.shoppe.data.network.networking.NetworkState
import com.lee.shoppe.data.repository.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class FavViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {

    private val TAG = "FavViewModel"

    private val _product = MutableStateFlow<NetworkState<DraftOrderResponse>>(NetworkState.Loading)
    val product: StateFlow<NetworkState<DraftOrderResponse>> = _product.asStateFlow()

    fun getFavProducts(listId: Long) {
        Log.d(TAG, "Fetching favorite products for list ID: $listId")
        viewModelScope.launch(Dispatchers.IO) {
            repository.getDraftOrder(listId)
                .catch { e ->
                    Log.e(TAG, "Error fetching favorite products: ${e.message}", e)
                    _product.value = NetworkState.Failure(e)
                }
                .collect { response ->
                    Log.d(TAG, "Favorite products fetched successfully: $response")
                    _product.value = NetworkState.Success(response)
                }
        }
    }

    fun isFavProduct(id: Long, listId: Long, favTrue: () -> Unit, favFalse: () -> Unit) {
        Log.d(TAG, "Checking if product $id is in favorites for list ID: $listId")
        if (listId <= 0) {
            Log.w(TAG, "Invalid listId: $listId, defaulting to not favorite")
            favFalse()
            return
        }
        
        viewModelScope.launch(Dispatchers.IO) {
            _product.value = NetworkState.Loading
            repository.getDraftOrder(listId)
                .catch { e ->
                    Log.e(TAG, "Error checking favorite status: ${e.message}", e)
                    _product.value = NetworkState.Failure(e)
                    withContext(Dispatchers.Main) {
                        favFalse() // Default to not favorite on error
                    }
                }
                .collect {
                    val response = it.draft_order.line_items
                    if (response.size > 1) {
                        val isFav = response.toMutableList().any { lineItem ->
                            val values = lineItem.sku?.split("*")
                            values?.get(0)?.equals(id.toString()) ?: false
                        }

                        withContext(Dispatchers.Main) {
                            if (isFav) favTrue() else favFalse()
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            favFalse()
                        }
                    }
                }
        }
    }

    private fun convertProductToLineItem(product: Product): DraftOrderResponse.DraftOrder.LineItem {
        val imageSrc = product.image?.src ?: ""
        return DraftOrderResponse.DraftOrder.LineItem(
            variant_id = null,
            quantity = 1,
            id = product.id,
            title = product.title,
            price = product.variants?.get(0)?.price,
            sku = "${product.id}*$imageSrc",
            product_id = product.id,
            properties = listOf(
                DraftOrderResponse.DraftOrder.LineItem.Property(
                    name = "ProductImage(src=$imageSrc)",
                    value = "${product.variants?.get(0)?.inventory_quantity ?: 1}*${product.variants?.get(0)?.price ?: "0.00"}"
                )
            )
        )
    }

    fun checkAndCreateFavListIfNeeded(onFavListCreated: (Long) -> Unit) {
        Log.d(TAG, "Checking if favorites list needs to be created")
        viewModelScope.launch(Dispatchers.IO) {
            val draftOrderResponse = DraftOrderResponse(DraftOrderResponse.DraftOrder())
            repository.createDraftOrders(draftOrderResponse)
                .catch { e ->
                    Log.e(TAG, "Error creating favorites list: ${e.message}", e)
                    _product.value = NetworkState.Failure(e)
                }
                .collect { response ->
                    val newListId = response.draft_order.id
                    Log.d(TAG, "Favorites list created with ID: $newListId")
                    withContext(Dispatchers.Main) {
                        onFavListCreated(newListId)
                    }
                }
        }
    }

    fun insertFavProduct(product: Product, listId: Long, onFavListCreated: ((Long) -> Unit)? = null) {
        Log.d(TAG, "Adding product ${product.id} to favorites for list ID: $listId")
        if (listId <= 0) {
            Log.w(TAG, "Invalid listId: $listId, creating new favorites list")
            // Create a new favorites list
            viewModelScope.launch(Dispatchers.IO) {
                val draftOrderResponse = DraftOrderResponse(DraftOrderResponse.DraftOrder())
                repository.createDraftOrders(draftOrderResponse)
                    .catch { e ->
                        Log.e(TAG, "Error creating favorites list: ${e.message}", e)
                        _product.value = NetworkState.Failure(e)
                    }
                    .collect { response ->
                        val newListId = response.draft_order.id
                        Log.d(TAG, "Favorites list created with ID: $newListId")
                        // Notify the caller about the new list ID
                        withContext(Dispatchers.Main) {
                            onFavListCreated?.invoke(newListId)
                        }
                        // Now add the product to the new list
                        insertFavProduct(product, newListId)
                    }
            }
            return
        }
        
        viewModelScope.launch(Dispatchers.IO) {
            _product.value = NetworkState.Loading
            repository.getDraftOrder(listId)
                .catch { e ->
                    Log.e(TAG, "Error adding product to favorites: ${e.message}", e)
                    _product.value = NetworkState.Failure(e)
                }
                .collect {
                    val draftOrder = it.draft_order

                    val updatedLineItems = draftOrder.line_items.toMutableList().apply {
                        add(convertProductToLineItem(product))
                    }

                    val updatedDraftOrder = draftOrder.copy(line_items = updatedLineItems)
                    repository.updateDraftOrder(listId, DraftOrderResponse(updatedDraftOrder))
                        .catch { e ->
                            Log.e(TAG, "Error updating draft order: ${e.message}", e)
                            _product.value = NetworkState.Failure(e)
                        }
                        .collect { response ->
                            Log.d(TAG, "Product added to favorites successfully: $response")
                            _product.value = NetworkState.Success(response)
                        }
                }
        }
    }

    fun deleteFavProduct(id: Long, listId: Long) {
        Log.d(TAG, "Removing product $id from favorites for list ID: $listId")
        if (listId <= 0) {
            Log.e(TAG, "Invalid listId: $listId, cannot remove from favorites")
            _product.value = NetworkState.Failure(Exception("Invalid favorites list ID"))
            return
        }
        
        viewModelScope.launch(Dispatchers.IO) {
            _product.value = NetworkState.Loading
            repository.getDraftOrder(listId)
                .catch { e ->
                    Log.e(TAG, "Error removing product from favorites: ${e.message}", e)
                    _product.value = NetworkState.Failure(e)
                }
                .collect {
                    val draftOrder = it.draft_order

                    val updatedLineItems = draftOrder.line_items.filter { lineItem ->
                        val values = lineItem.sku?.split("*")
                        val equal = values?.get(0)?.equals(id.toString()) ?: false
                        (!equal)
                    }

                    val updatedDraftOrder = draftOrder.copy(line_items = updatedLineItems)
                    repository.updateDraftOrder(listId, DraftOrderResponse(updatedDraftOrder))
                        .catch { e ->
                            Log.e(TAG, "Error updating draft order: ${e.message}", e)
                            _product.value = NetworkState.Failure(e)
                        }
                        .collect { response ->
                            Log.d(TAG, "Product removed from favorites successfully: $response")
                            _product.value = NetworkState.Success(response)
                        }
                }
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "FavViewModel cleared")
    }
} 