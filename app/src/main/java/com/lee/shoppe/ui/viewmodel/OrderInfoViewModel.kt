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
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrderInfoViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {
    private val _order = MutableStateFlow<NetworkState<OrderResponse>>(NetworkState.Loading)
    val order: StateFlow<NetworkState<OrderResponse>> = _order

    fun getOrder(orderId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getSingleOrder(orderId)
                .catch { e -> _order.value = NetworkState.Failure(e) }
                .collect { response ->
                    _order.value = NetworkState.Success(response)
                }
        }
    }
} 