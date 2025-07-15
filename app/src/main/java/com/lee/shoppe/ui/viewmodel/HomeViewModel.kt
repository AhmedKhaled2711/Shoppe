package com.lee.shoppe.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lee.shoppe.data.model.BrandResponse
import com.lee.shoppe.data.model.PriceRule
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
class HomeViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {

    private val TAG = "HomeViewModel"

    private val _brands = MutableStateFlow<NetworkState<BrandResponse>>(NetworkState.Loading)
    val brands: StateFlow<NetworkState<BrandResponse>> = _brands.asStateFlow()

    private val _priceRules = MutableStateFlow<NetworkState<PriceRule>>(NetworkState.Loading)
    val priceRules: StateFlow<NetworkState<PriceRule>> = _priceRules.asStateFlow()

    fun getBrands() {
        Log.d(TAG, "Fetching brands...")
        viewModelScope.launch(Dispatchers.IO) {
            repository.getBrands()
                .catch { e ->
                    Log.e(TAG, "Error fetching brands: ${e.message}", e)
                    _brands.value = NetworkState.Failure(e)
                }
                .collect { response ->
                    Log.d(TAG, "Brands fetched successfully: $response")
                    _brands.value = NetworkState.Success(response)
                }
        }
    }

    fun getDiscountCodes() {
        Log.d(TAG, "Fetching discount codes...")
        viewModelScope.launch(Dispatchers.IO) {
            repository.getDiscountCodes()
                .catch { e ->
                    Log.e(TAG, "Error fetching discount codes: ${e.message}", e)
                    _priceRules.value = NetworkState.Failure(e)
                }
                .collect { response ->
                    Log.d(TAG, "Discount codes fetched successfully: $response")
                    _priceRules.value = NetworkState.Success(response)
                }
        }
    }
}