package com.lee.shoppe.data.network.networking

sealed class NetworkState<out T> {
    object Idle : NetworkState<Nothing>()
    object Loading : NetworkState<Nothing>()
    data class Success<out T>(val data: T) : NetworkState<T>()
    data class Failure(val error: Throwable) : NetworkState<Nothing>()
}
