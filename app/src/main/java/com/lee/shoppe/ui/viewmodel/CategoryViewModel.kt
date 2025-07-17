package com.lee.shoppe.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lee.shoppe.data.model.Product
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
import java.util.Locale
import retrofit2.HttpException

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {

    private val _products = MutableStateFlow<NetworkState<ProductResponse>>(NetworkState.Loading)
    val products: StateFlow<NetworkState<ProductResponse>> = _products.asStateFlow()

    private var _allProducts: List<Product> = emptyList()
    private var _subProducts: List<Product> = emptyList()

    fun getProducts(vendor: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getBrandProducts(vendor)
                .catch { e ->
                    if (e is HttpException && e.code() == 429) {
                        _products.value = NetworkState.Failure(Exception("Too many requests. Please try again later."))
                    } else {
                        _products.value = NetworkState.Failure(e)
                    }
                }
                .collect { response ->
                    val productsList = response.products ?: emptyList()
                    _allProducts = productsList
                    _subProducts = productsList
                    _products.value = NetworkState.Success(response)
                }
        }
    }

    fun filterProducts(mainCategory: String, subCategory: String) {
        _products.value = NetworkState.Loading
        viewModelScope.launch {
            var filteredProducts = _allProducts.filter { it.tags?.contains(mainCategory, true) ?: false }
            if (subCategory.isNotBlank()) {
                filteredProducts = filteredProducts.filter { it.product_type.equals(subCategory, true) }
            }
            _subProducts = filteredProducts
            _products.value = NetworkState.Success(ProductResponse(filteredProducts))
        }
    }

    fun searchProducts(query: String) {
        _products.value = NetworkState.Loading
        viewModelScope.launch {
            val filteredProducts = _subProducts.filter {
                it.title?.lowercase(Locale.getDefault())?.contains(query.lowercase(Locale.getDefault())) ?: false
            }
            _products.value = NetworkState.Success(ProductResponse(filteredProducts))
        }
    }

    fun filterProductsByPrice(from: Float?, to: Float?) {
        _products.value = NetworkState.Loading
        viewModelScope.launch {
            val filteredProducts = _subProducts.filter {
                val price = it.variants?.firstOrNull()?.price?.toFloatOrNull() ?: 0f
                val isWithinFrom = from?.let { price >= it } ?: true
                val isWithinTo = to?.let { price <= it } ?: true
                isWithinFrom && isWithinTo
            }
            _products.value = NetworkState.Success(ProductResponse(filteredProducts))
        }
    }
}