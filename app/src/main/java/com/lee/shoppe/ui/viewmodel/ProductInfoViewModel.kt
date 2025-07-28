package com.lee.shoppe.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lee.shoppe.data.model.DraftOrderResponse
import com.lee.shoppe.data.model.Product
import com.lee.shoppe.data.model.ProductDetails
import com.lee.shoppe.data.model.ProductResponse
import com.lee.shoppe.data.model.Review
import com.lee.shoppe.data.model.Reviews
import com.lee.shoppe.data.network.networking.NetworkState
import com.lee.shoppe.data.repository.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class ProductInfoViewModel @Inject constructor(
    val repository: Repository
) : ViewModel() {

    private val TAG = "ProductInfoViewModel"

    private val _product = MutableStateFlow<NetworkState<ProductResponse>>(NetworkState.Loading)
    val product: StateFlow<NetworkState<ProductResponse>> = _product.asStateFlow()

    private val _productCard = MutableStateFlow<NetworkState<DraftOrderResponse>>(NetworkState.Idle)
    val productCard: StateFlow<NetworkState<DraftOrderResponse>> = _productCard.asStateFlow()

    private val _productSuggestions = MutableStateFlow<NetworkState<ProductResponse>>(NetworkState.Loading)
    val productSuggestions: StateFlow<NetworkState<ProductResponse>> = _productSuggestions.asStateFlow()

    private val _reviews = MutableStateFlow<NetworkState<List<Review>>>(NetworkState.Idle)
    val reviews: StateFlow<NetworkState<List<Review>>> = _reviews.asStateFlow()

    private var lastApiCallTime = 0L
    private val MIN_TIME_BETWEEN_CALLS = TimeUnit.SECONDS.toMillis(1) // 1 second delay between API calls

    private suspend fun ensureRateLimit() {
        val now = System.currentTimeMillis()
        val timeSinceLastCall = now - lastApiCallTime
        
        if (timeSinceLastCall < MIN_TIME_BETWEEN_CALLS) {
            val delayTime = MIN_TIME_BETWEEN_CALLS - timeSinceLastCall
            delay(delayTime)
        }
        lastApiCallTime = System.currentTimeMillis()
    }

    fun getProductInfo(id: Long) {
        Log.d(TAG, "Fetching product info for ID: $id")
        viewModelScope.launch(Dispatchers.IO) {
            try {
                ensureRateLimit()
                
                repository.getProductById(id)
                    .retryWhen { cause, attempt ->
                        if (cause is IOException || cause is SocketTimeoutException || 
                            (cause is HttpException && cause.code() == 429)) {
                            val waitTime = (attempt * 1000L).coerceAtMost(5000L) // Exponential backoff, max 5 seconds
                            Log.w(TAG, "Retrying API call after $waitTime ms (attempt: ${attempt + 1})")
                            delay(waitTime)
                            true
                        } else {
                            false
                        }
                    }
                    .catch { e ->
                        Log.e(TAG, "Error fetching product info: ${e.message}", e)
                        val errorMessage = when (e) {
                            is HttpException -> {
                                when (e.code()) {
                                    429 -> "Too many requests. Please wait a moment and try again."
                                    in 500..599 -> "Server error. Please try again later."
                                    else -> "Network error: ${e.message}"
                                }
                            }
                            is SocketTimeoutException -> "Connection timeout. Please check your internet connection."
                            is IOException -> "Network error. Please check your internet connection."
                            else -> "An error occurred: ${e.localizedMessage ?: e.message}"
                        }
                        _product.value = NetworkState.Failure(Exception(errorMessage))
                    }
                    .collect { response ->
                        Log.d(TAG, "Product info fetched successfully: $response")
                        _product.value = NetworkState.Success(response)
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error in getProductInfo: ${e.message}", e)
                _product.value = NetworkState.Failure(Exception("An unexpected error occurred"))
            }
        }
    }

    fun getProductSuggestions(vendor: String) {
        Log.d(TAG, "Fetching product suggestions for vendor: $vendor")
        viewModelScope.launch(Dispatchers.IO) {
            try {
                ensureRateLimit()
                
                repository.getBrandProducts(vendor)
                    .retryWhen { cause, attempt ->
                        if (cause is IOException || cause is SocketTimeoutException || 
                            (cause is HttpException && cause.code() == 429)) {
                            val waitTime = (attempt * 1000L).coerceAtMost(5000L) // Exponential backoff, max 5 seconds
                            Log.w(TAG, "Retrying API call after $waitTime ms (attempt: ${attempt + 1})")
                            delay(waitTime)
                            true
                        } else {
                            false
                        }
                    }
                    .catch { e ->
                        Log.e(TAG, "Error fetching product suggestions: ${e.message}", e)
                        val errorMessage = when (e) {
                            is HttpException -> {
                                when (e.code()) {
                                    429 -> "Too many requests. Please wait a moment and try again."
                                    in 500..599 -> "Server error. Please try again later."
                                    else -> "Network error: ${e.message}"
                                }
                            }
                            is SocketTimeoutException -> "Connection timeout. Please check your internet connection."
                            is IOException -> "Network error. Please check your internet connection."
                            else -> "An error occurred: ${e.message}"
                        }
                        _productSuggestions.value = NetworkState.Failure(Exception(errorMessage))
                    }
                    .collect { response ->
                        Log.d(TAG, "Product suggestions fetched successfully: $response")
                        _productSuggestions.value = NetworkState.Success(response)
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error in getProductSuggestions: ${e.message}", e)
                _productSuggestions.value = NetworkState.Failure(Exception("An unexpected error occurred"))
            }
        }
    }

    fun getReviews() {
        viewModelScope.launch {
            _reviews.value = NetworkState.Loading
            try {
                val reviewList = Reviews().getReviews()
                _reviews.value = NetworkState.Success(reviewList)
            } catch (e: Exception) {
                _reviews.value = NetworkState.Failure(e)
            }
        }
    }

    private fun convertProductToLineItem(product: ProductDetails, vId: Long): DraftOrderResponse.DraftOrder.LineItem {
        val imageSrc = product.image?.src ?: product.images?.getOrNull(0)?.src ?: ""
        return DraftOrderResponse.DraftOrder.LineItem(
            variant_id = vId,
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

    fun insertCardProduct(product: ProductDetails, vId: Long, listId: Long) {
        Log.d(TAG, "Adding product ${product.id} to cart with variant ID: $vId")
        viewModelScope.launch(Dispatchers.IO) {
            _productCard.value = NetworkState.Loading
            repository.getDraftOrder(listId)
                .catch { e ->
                    Log.e(TAG, "Error adding product to cart: ${e.message}", e)
                    if (e is HttpException && e.code() == 429) {
                        _productCard.value = NetworkState.Failure(Exception("Too many requests. Please try again later."))
                    } else {
                        _productCard.value = NetworkState.Failure(e)
                    }
                }
                .collect { draftOrderResponse ->
                    val draftOrder = draftOrderResponse.draft_order

                    // Check if variant_id already exists in line_items
                    val existingLineItem = draftOrder.line_items.find { it.variant_id == vId }
                    if (existingLineItem == null) {
                        val updatedLineItems = draftOrder.line_items.toMutableList().apply {
                            add(convertProductToLineItem(product, vId))
                        }
                        val updatedDraftOrder = draftOrder.copy(line_items = updatedLineItems)
                        
                        Log.d(TAG, "Updated draft order: $updatedDraftOrder")
                        
                        repository.updateDraftOrder(listId, DraftOrderResponse(updatedDraftOrder))
                            .catch { e ->
                                Log.e(TAG, "Error updating draft order: ${e.message}", e)
                                _productCard.value = NetworkState.Failure(e)
                            }
                            .collect { response ->
                                Log.d(TAG, "Product added to cart successfully: $response")
                                _productCard.value = NetworkState.Success(response)
                            }
                    } else {
                        Log.d(TAG, "Product already in cart")
                        _productCard.value = NetworkState.Failure(Exception("Item is already in the cart"))
                    }
                }
        }
    }

    fun resetProductCardState() {
        _productCard.value = NetworkState.Idle
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "ProductInfoViewModel cleared")
    }
} 