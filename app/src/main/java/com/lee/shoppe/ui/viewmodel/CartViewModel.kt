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
class CartViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {

    private val TAG = "CartViewModel"
    private val _cartProducts = MutableStateFlow<NetworkState<DraftOrderResponse>>(NetworkState.Loading)
    val cartProducts: StateFlow<NetworkState<DraftOrderResponse>> = _cartProducts.asStateFlow()

    private val _cartListId = MutableStateFlow<Long>(0L)
    val cartListId: StateFlow<Long> = _cartListId.asStateFlow()

    fun getCartProducts(listId: Long) {
        Log.d(TAG, "Fetching cart products for list ID: $listId")
        if (listId <= 0) {
            Log.w(TAG, "Invalid listId: $listId, cannot fetch cart products")
            _cartProducts.value = NetworkState.Failure(Exception("Invalid cart list ID"))
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            _cartProducts.value = NetworkState.Loading
            repository.getDraftOrder(listId)
                .catch { e ->
                    Log.e(TAG, "Error fetching cart products: ${e.message}", e)
                    _cartProducts.value = NetworkState.Failure(e)
                }
                .collect { response ->
                    Log.d(TAG, "Cart products fetched successfully: $response")
                    _cartProducts.value = NetworkState.Success(response)
                }
        }
    }

    fun addProductToCart(product: Product, listId: Long, onCartListCreated: ((Long) -> Unit)? = null) {
        Log.d(TAG, "Adding product ${product.id} to cart for list ID: $listId")
        if (listId <= 0) {
            Log.w(TAG, "Invalid listId: $listId, creating new cart list")
            // Create a new cart list
            viewModelScope.launch(Dispatchers.IO) {
                val draftOrderResponse = DraftOrderResponse(DraftOrderResponse.DraftOrder())
                repository.createDraftOrders(draftOrderResponse)
                    .catch { e ->
                        Log.e(TAG, "Error creating cart list: ${e.message}", e)
                        _cartProducts.value = NetworkState.Failure(e)
                    }
                    .collect { response ->
                        val newListId = response.draft_order.id
                        Log.d(TAG, "Cart list created with ID: $newListId")
                        // Notify the caller about the new list ID
                        withContext(Dispatchers.Main) {
                            onCartListCreated?.invoke(newListId)
                        }
                        // Now add the product to the new list
                        addProductToCart(product, newListId)
                    }
            }
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            _cartProducts.value = NetworkState.Loading
            repository.getDraftOrder(listId)
                .catch { e ->
                    Log.e(TAG, "Error adding product to cart: ${e.message}", e)
                    _cartProducts.value = NetworkState.Failure(e)
                }
                .collect {
                    val draftOrder = it.draft_order

                    val updatedLineItems = draftOrder.line_items.toMutableList().apply {
                        product.id?.let { it1 -> convertProductToLineItem(product, it1) }
                            ?.let { it2 -> add(it2) }
                    }

                    val updatedDraftOrder = draftOrder.copy(line_items = updatedLineItems)
                    repository.updateDraftOrder(listId, DraftOrderResponse(updatedDraftOrder))
                        .catch { e ->
                            Log.e(TAG, "Error updating draft order: ${e.message}", e)
                            _cartProducts.value = NetworkState.Failure(e)
                        }
                        .collect { response ->
                            Log.d(TAG, "Product added to cart successfully: $response")
                            _cartProducts.value = NetworkState.Success(response)
                        }
                }
        }
    }

    fun removeProductFromCart(productId: Long, listId: Long) {
        Log.d(TAG, "Removing product $productId from cart for list ID: $listId")
        if (listId <= 0) {
            Log.e(TAG, "Invalid listId: $listId, cannot remove from cart")
            _cartProducts.value = NetworkState.Failure(Exception("Invalid cart list ID"))
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            _cartProducts.value = NetworkState.Loading
            repository.getDraftOrder(listId)
                .catch { e ->
                    Log.e(TAG, "Error removing product from cart: ${e.message}", e)
                    _cartProducts.value = NetworkState.Failure(e)
                }
                .collect {
                    val draftOrder = it.draft_order

                    val updatedLineItems = draftOrder.line_items.filter { lineItem ->
                        val values = lineItem.sku?.split("*")
                        val skuProductId = values?.getOrNull(0)
                        Log.d(TAG, "Comparing for removal: lineItem.sku=${lineItem.sku}, skuProductId=$skuProductId, lineItem.variant_id=${lineItem.variant_id}, targetProductId=$productId")
                        val equal = skuProductId == productId.toString() || lineItem.variant_id == productId
                        !equal
                    }

                    val updatedDraftOrder = draftOrder.copy(line_items = updatedLineItems)
                    repository.updateDraftOrder(listId, DraftOrderResponse(updatedDraftOrder))
                        .catch { e ->
                            Log.e(TAG, "Error updating draft order: ${e.message}", e)
                            _cartProducts.value = NetworkState.Failure(e)
                        }
                        .collect { response ->
                            Log.d(TAG, "Product removed from cart successfully: $response")
                            _cartProducts.value = NetworkState.Success(response)
                        }
                }
        }
    }

    fun updateProductQuantity(productId: Long, quantity: Int, listId: Long) {
        Log.d(TAG, "Updating quantity for product $productId to $quantity in cart list ID: $listId")
        if (listId <= 0) {
            Log.e(TAG, "Invalid listId: $listId, cannot update quantity")
            _cartProducts.value = NetworkState.Failure(Exception("Invalid cart list ID"))
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            _cartProducts.value = NetworkState.Loading
            repository.getDraftOrder(listId)
                .catch { e ->
                    Log.e(TAG, "Error updating product quantity: ${e.message}", e)
                    _cartProducts.value = NetworkState.Failure(e)
                }
                .collect { response ->
                    val updatedLineItems = response.draft_order.line_items.map { item ->
                        val values = item.sku?.split("*")
                        val skuProductId = values?.getOrNull(0)
                        Log.d(TAG, "Comparing for quantity update: item.sku=${item.sku}, skuProductId=$skuProductId, item.variant_id=${item.variant_id}, targetProductId=$productId")
                        val equal = skuProductId == productId.toString() || item.variant_id == productId
                        if (equal) {
                            item.copy(quantity = quantity)
                        } else {
                            item
                        }
                    }
                    val updatedDraftOrder = response.draft_order.copy(line_items = updatedLineItems)
                    repository.updateDraftOrder(listId, DraftOrderResponse(updatedDraftOrder))
                        .catch { e ->
                            Log.e(TAG, "Error updating draft order: ${e.message}", e)
                            _cartProducts.value = NetworkState.Failure(e)
                        }
                        .collect { response ->
                            Log.d(TAG, "Product quantity updated successfully: $response")
                            _cartProducts.value = NetworkState.Success(response)
                        }
                }
        }
    }

    fun clearCart(listId: Long) {
        Log.d(TAG, "Clearing cart for list ID: $listId")
        if (listId <= 0) {
            Log.e(TAG, "Invalid listId: $listId, cannot clear cart")
            _cartProducts.value = NetworkState.Failure(Exception("Invalid cart list ID"))
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            _cartProducts.value = NetworkState.Loading
            repository.getDraftOrder(listId)
                .catch { e ->
                    Log.e(TAG, "Error clearing cart: ${e.message}", e)
                    _cartProducts.value = NetworkState.Failure(e)
                }
                .collect { response ->
                    // Keep the dummy item structure as required by DraftOrder model
                    val dummyItem = DraftOrderResponse.DraftOrder.LineItem(
                        variant_id = null,
                        quantity = 1,
                        id = null,
                        title = "dummy",
                        price = "1",
                        sku = null,
                        product_id = null
                    )
                    val updatedDraftOrder = response.draft_order.copy(line_items = listOf(dummyItem))
                    repository.updateDraftOrder(listId, DraftOrderResponse(updatedDraftOrder))
                        .catch { e ->
                            Log.e(TAG, "Error updating draft order: ${e.message}", e)
                            _cartProducts.value = NetworkState.Failure(e)
                        }
                        .collect { response ->
                            Log.d(TAG, "Cart cleared successfully: $response")
                            _cartProducts.value = NetworkState.Success(response)
                        }
                }
        }
    }

    fun checkAndCreateCartListIfNeeded(onCartListCreated: (Long) -> Unit) {
        Log.d(TAG, "Checking if cart list needs to be created")
        viewModelScope.launch(Dispatchers.IO) {
            val draftOrderResponse = DraftOrderResponse(DraftOrderResponse.DraftOrder())
            repository.createDraftOrders(draftOrderResponse)
                .catch { e ->
                    Log.e(TAG, "Error creating cart list: ${e.message}", e)
                    _cartProducts.value = NetworkState.Failure(e)
                }
                .collect { response ->
                    val newListId = response.draft_order.id
                    Log.d(TAG, "Cart list created with ID: $newListId")
                    withContext(Dispatchers.Main) {
                        onCartListCreated(newListId)
                    }
                }
        }
    }

    fun isProductInCart(productId: Long, listId: Long, inCart: () -> Unit, notInCart: () -> Unit) {
        Log.d(TAG, "Checking if product $productId is in cart for list ID: $listId")
        if (listId <= 0) {
            Log.w(TAG, "Invalid listId: $listId, defaulting to not in cart")
            notInCart()
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            repository.getDraftOrder(listId)
                .catch { e ->
                    Log.e(TAG, "Error checking cart status: ${e.message}", e)
                    withContext(Dispatchers.Main) {
                        notInCart() // Default to not in cart on error
                    }
                }
                .collect {
                    val response = it.draft_order.line_items
                    if (response.size > 1) {
                        val isInCart = response.toMutableList().any { lineItem ->
                            val values = lineItem.sku?.split("*")
                            values?.get(0)?.equals(productId.toString()) ?: false
                        }

                        withContext(Dispatchers.Main) {
                            if (isInCart) inCart() else notInCart()
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            notInCart()
                        }
                    }
                }
        }
    }

    private fun convertProductToLineItem(product: Product, vId: Long): DraftOrderResponse.DraftOrder.LineItem {
        return DraftOrderResponse.DraftOrder.LineItem(
            variant_id = vId,
            quantity = 1,
            id = product.id,
            title = product.title,
            price = product.variants?.get(0)?.price,
            sku = product.id.toString() + "*" + (product.image?.src ?: ""),
            product_id = product.id,
            properties = listOf(
                DraftOrderResponse.DraftOrder.LineItem.Property(
                    name = "ProductImage(src=${product.image?.src ?: ""})",
                    value = "${product.variants?.get(0)?.inventory_quantity ?: 1}*${product.variants?.get(0)?.price ?: "0.00"}"
                )
            )
        )
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "CartViewModel cleared")
    }
}
