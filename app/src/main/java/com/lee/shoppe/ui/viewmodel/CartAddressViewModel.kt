package com.lee.shoppe.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lee.shoppe.data.model.AddressDefault
import com.lee.shoppe.data.model.AddressDefaultRequest
import com.lee.shoppe.data.model.AddressUpdateRequest
import com.lee.shoppe.data.model.OneCustomer
import com.lee.shoppe.data.network.networking.NetworkState
import com.lee.shoppe.data.repository.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import javax.inject.Inject

@HiltViewModel
class CartAddressViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {

    private var _products = MutableStateFlow<NetworkState<OneCustomer>>(NetworkState.Loading)
    var products: StateFlow<NetworkState<OneCustomer>> = _products

    private var _products1 = MutableStateFlow<NetworkState<AddressUpdateRequest>>(NetworkState.Loading)
    var products1: StateFlow<NetworkState<AddressUpdateRequest>> = _products1


    fun getCustomerData(id: Long, forceRefresh: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            Log.i("CartAddressViewModel", "Fetching customer with ID: $id, forceRefresh: $forceRefresh")
            try {
                val response = repository.getSingleCustomer(id, forceRefresh)
                _products.value = NetworkState.Success(response)
            } catch (e: HttpException) {
                Log.e("CartAddressViewModel", "HTTP error fetching customer: ${e.message}", e)
                _products.value = NetworkState.Failure(e)
            } catch (e: Exception) {
                Log.e("CartAddressViewModel", "Error fetching customer: ${e.message}", e)
                _products.value = NetworkState.Failure(e)
            }
        }
    }

    fun editSingleCustomerAddress(customerId:Long,id: Long, addressRequest: AddressDefaultRequest) {
        viewModelScope.launch(Dispatchers.IO) {

            try {
                val response = repository.editSingleCustomerAddressStar(customerId,id, addressRequest)
                _products1.value = NetworkState.Success(response)
                getCustomerData(customerId , forceRefresh = true)

            } catch (e: HttpException) {
                _products1.value = NetworkState.Failure(e)
            }catch (e: Exception) {
                _products1.value = NetworkState.Failure(e)
            }
        }
    }

    fun sendeditAddressRequest(
        id: Long, default: Boolean ,customerId: Long
    ) {
        val address = AddressDefault(
            default
        )
        val addressRequest = AddressDefaultRequest(address)
        editSingleCustomerAddress(customerId,id, addressRequest)


    }

    fun senddeleteAddressRequest(
        id: Long,
        customerId: Long
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            Log.i("Deleted", "Deleted Address: ${customerId}")
            repository.deleteSingleCustomerAddress(customerId,id)
            getCustomerData(customerId , forceRefresh = true)
        }
    }

}