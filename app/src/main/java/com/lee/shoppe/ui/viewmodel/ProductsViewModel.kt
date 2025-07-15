package com.lee.shoppe.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lee.shoppe.data.model.BrandResponse
import com.lee.shoppe.data.model.PriceRule
import com.lee.shoppe.data.model.ProductResponse
import com.lee.shoppe.data.network.networking.NetworkState
import com.lee.shoppe.data.repository.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductsViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {

    private val TAG = "ProductsViewModel"

    private val _products = MutableStateFlow<NetworkState<ProductResponse>>(NetworkState.Loading)
    val products: StateFlow<NetworkState<ProductResponse>> = _products.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private var originalProducts: ProductResponse? = null

    fun getProducts(vendor : String) {
        Log.d(TAG, "Fetching products...")
        viewModelScope.launch(Dispatchers.IO) {
            repository.getBrandProducts(vendor)
                .catch { e ->
                    Log.e(TAG, "Error fetching products: ${e.message}", e)
                    _products.value = NetworkState.Failure(e)
                }
                .collect { response ->
                    Log.d(TAG, "products fetched successfully: $response")
                    originalProducts = response
                    _products.value = NetworkState.Success(response)
                }
        }
    }

    fun emitSearch(query: String) {
        _searchQuery.value = query
        filterProducts()
    }

    fun filterProductsByPrice(fromPrice: Float?, toPrice: Float?) {
        val original = originalProducts ?: return
        val filteredProducts = original.products?.filter { product ->
            val price = product.variants?.firstOrNull()?.price?.toFloatOrNull() ?: 0f
            val fromCondition = fromPrice == null || price >= fromPrice
            val toCondition = toPrice == null || price <= toPrice
            fromCondition && toCondition
        }
        
        _products.value = NetworkState.Success(
            original.copy(products = filteredProducts)
        )
    }

    private fun filterProducts() {
        val original = originalProducts ?: return
        val query = _searchQuery.value.lowercase()
        
        if (query.isEmpty()) {
            _products.value = NetworkState.Success(original)
            return
        }

        val filteredProducts = original.products?.filter { product ->
            product.title?.lowercase()?.contains(query) == true ||
            product.product_type?.lowercase()?.contains(query) == true ||
            product.tags?.lowercase()?.contains(query) == true
        }

        _products.value = NetworkState.Success(
            original.copy(products = filteredProducts)
        )
    }

    fun toggleFavorite(productId: Int) {
        Log.d(TAG, "Toggle favorite for product ID: $productId")
        // TODO: Implement favorite toggle logic
    }
}