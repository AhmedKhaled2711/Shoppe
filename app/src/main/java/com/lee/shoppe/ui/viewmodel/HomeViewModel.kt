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
import retrofit2.HttpException
import kotlinx.coroutines.delay

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {

    private val TAG = "HomeViewModel"

    private val _brands = MutableStateFlow<NetworkState<BrandResponse>>(NetworkState.Loading)
    val brands: StateFlow<NetworkState<BrandResponse>> = _brands.asStateFlow()

    private val _priceRules = MutableStateFlow<NetworkState<PriceRule>>(NetworkState.Loading)
    val priceRules: StateFlow<NetworkState<PriceRule>> = _priceRules.asStateFlow()

    private var hasFetched = false

    fun fetchDataIfNeeded() {
        if (!hasFetched) {
            getBrands()
            getDiscountCodes()
            hasFetched = true
        }
    }

    fun getBrands() {
        Log.d(TAG, "Fetching brands...")
        viewModelScope.launch(Dispatchers.IO) {
            var retry = false
            repository.getBrands()
                .catch { e ->
                    Log.e(TAG, "Error fetching brands: ${e.message}", e)
                    if (e is HttpException && e.code() == 429) {
                        // Too Many Requests: show user-friendly message and retry once after delay
                        _brands.value = NetworkState.Failure(Exception("Too many requests. Please try again later."))
                        if (!retry) {
                            retry = true
                            delay(2000) // Wait 2 seconds before retrying
                            try {
                                repository.getBrands()
                                    .catch { retryErr ->
                                        Log.e(TAG, "Retry failed: ${retryErr.message}", retryErr)
                                        _brands.value = NetworkState.Failure(Exception("Too many requests. Please try again later."))
                                    }
                                    .collect { response ->
                                        Log.d(TAG, "Brands fetched successfully after retry: $response")
                                        _brands.value = NetworkState.Success(response)
                                    }
                            } catch (retryException: Exception) {
                                Log.e(TAG, "Retry exception: ${retryException.message}", retryException)
                                _brands.value = NetworkState.Failure(Exception("Too many requests. Please try again later."))
                            }
                        }
                    } else {
                        _brands.value = NetworkState.Failure(e)
                    }
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