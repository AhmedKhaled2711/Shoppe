package com.lee.shoppe.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fashionshop.Model.OrderResponse
import com.lee.shoppe.data.repository.Repository
import com.lee.shoppe.data.network.networking.NetworkState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrdersViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {

    private var _orders = MutableStateFlow<NetworkState<OrderResponse>>(NetworkState.Loading)
    val orders =_orders.asStateFlow()

    /**
     * Fetches customer orders with optional cache control
     * @param userId The ID of the customer
     * @param forceRefresh If true, bypasses cache and forces a network request
     */
    fun getOrders(userId: Long, forceRefresh: Boolean = false) {
        _orders.value = NetworkState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = if (forceRefresh) {
                    // Force a network request by adding a no-cache header
                    repository.getCustomerOrders(userId, true)
                } else {
                    repository.getCustomerOrders(userId)
                }
                _orders.value = NetworkState.Success(response)
            } catch (e: Exception) {
                // If we failed to force refresh, try once more with cache
                if (forceRefresh) {
                    try {
                        val cachedResponse = repository.getCustomerOrders(userId, false)
                        _orders.value = NetworkState.Success(cachedResponse)
                    } catch (e2: Exception) {
                        _orders.value = NetworkState.Failure(e2)
                    }
                } else {
                    _orders.value = NetworkState.Failure(e)
                }
            }
        }
    }

    /**
     * Refreshes the orders list by forcing a network request
     */
    fun refreshOrders(userId: Long) {
        getOrders(userId, true)
    }

    override fun onCleared() {
        super.onCleared()
    }
} 