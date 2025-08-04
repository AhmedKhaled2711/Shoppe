package com.lee.shoppe.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fashionshop.Model.OrderBody
import com.example.fashionshop.Model.OrderBodyResponse
import com.lee.shoppe.data.model.OneCustomer
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
import retrofit2.HttpException
import javax.inject.Inject

@HiltViewModel
class OrderDetailsViewModel @Inject constructor(
    private var repository: Repository
) : ViewModel() {

    private val _order = MutableStateFlow<NetworkState<OrderBodyResponse>>(NetworkState.Idle)
    val order: StateFlow<NetworkState<OrderBodyResponse>> = _order.asStateFlow()
    private var _productCode = MutableStateFlow<NetworkState<PriceRule>>(NetworkState.Loading)
    var productCode: StateFlow<NetworkState<PriceRule>> = _productCode
    private val _singleCustomer = MutableStateFlow<OneCustomer?>(null)
    val singleCustomer: StateFlow<OneCustomer?> = _singleCustomer.asStateFlow()

    fun getAdsCode() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = repository.getDiscountCodes().catch { e -> _productCode.value = NetworkState.Failure(e) }
                    .collect {
                        _productCode.value = NetworkState.Success(it)
                    }
            } catch (e: HttpException) {
                _productCode.value = NetworkState.Failure(e)
            } catch (e: Exception) {
                _productCode.value = NetworkState.Failure(e)
            }
        }
    }

    fun createOrder(
        orderBody: Map<String, OrderBody>,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _order.value = NetworkState.Loading
            repository.createOrder(orderBody)
                .catch {
                    e -> _order.value = NetworkState.Failure(e)
                    onError.invoke("HTTP Error: ${e.message}")
                }
                .collect { response ->
                    _order.value = NetworkState.Success(response)
                    onSuccess.invoke()
                }
        }
    }

    /**
     * Fetches a single customer with optional force refresh
     * @param customerId The ID of the customer to fetch
     * @param forceRefresh If true, bypasses cache and fetches fresh data from the server
     * @return The customer data or null if the request fails
     */
    suspend fun getSingleCustomer(customerId: Long, forceRefresh: Boolean = false): OneCustomer? {
        return try {
            val customer = repository.getSingleCustomer(customerId, forceRefresh)
            _singleCustomer.value = customer
            customer
        } catch (e: Exception) {
            Log.e("OrderDetailsViewModel", "Error fetching customer: ${e.message}")
            null
        }
    }
}