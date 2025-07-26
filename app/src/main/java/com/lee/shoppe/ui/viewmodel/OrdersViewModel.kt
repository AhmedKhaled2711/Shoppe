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

    fun getOrders(userId: Long) {
        viewModelScope.launch(Dispatchers.IO){
            repository.getCustomerOrders(userId)
            .catch {
                e -> _orders.value = NetworkState.Failure(e)
            }
            .collect { response ->
                _orders.value = NetworkState.Success(response)
            }
        }

    }

    override fun onCleared() {
        super.onCleared()
    }
} 