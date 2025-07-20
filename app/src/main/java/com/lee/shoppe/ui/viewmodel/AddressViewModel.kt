package com.lee.shoppe.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lee.shoppe.data.model.Address
import com.lee.shoppe.data.model.AddressDefault
import com.lee.shoppe.data.model.AddressDefaultRequest
import com.lee.shoppe.data.model.AddressRequest
import com.lee.shoppe.data.model.AddressUpdateRequest
import com.lee.shoppe.data.model.CustomerAddress
import com.lee.shoppe.data.model.OneCustomer
import com.lee.shoppe.data.model.UpdateCustomerRequest
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
import retrofit2.HttpException

@HiltViewModel
class AddressViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {
    private val _addresses = MutableStateFlow<NetworkState<List<Address>>>(NetworkState.Loading)
    val addresses: StateFlow<NetworkState<List<Address>>> = _addresses.asStateFlow()

    private val _actionState = MutableStateFlow<NetworkState<Unit>>(NetworkState.Idle)
    val actionState: StateFlow<NetworkState<Unit>> = _actionState.asStateFlow()

    fun fetchAddresses(customerId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            _addresses.value = NetworkState.Loading
            try {
                val oneCustomer = repository.getCustomers(customerId)
                _addresses.value = NetworkState.Success(oneCustomer.customer.addresses)
            } catch (e: Exception) {
                _addresses.value = NetworkState.Failure(e)
            }
        }
    }

    fun addAddress(customerId: Long, address: Address) {
        viewModelScope.launch(Dispatchers.IO) {
            _actionState.value = NetworkState.Loading
            try {
                repository.addSingleCustomerAddress(customerId, AddressRequest(address))
                _actionState.value = NetworkState.Success(Unit)
                fetchAddresses(customerId)
            } catch (e: Exception) {
                _actionState.value = NetworkState.Failure(e)
            }
        }
    }

    fun editAddress(customerId: Long, addressId: Long, address: Address) {
        viewModelScope.launch(Dispatchers.IO) {
            _actionState.value = NetworkState.Loading
            try {
                val updateRequest = AddressUpdateRequest(
                    customer_address = CustomerAddress(
                        address1 = address.address1,
                        address2 = address.address2.toString(),
                        city = address.city,
                        company = address.company.toString(),
                        country = address.country,
                        country_code = address.country_code,
                        country_name = address.country_name,
                        customer_id = address.customer_id,
                        default = address.default,
                        first_name = address.first_name,
                        id = address.id,
                        last_name = address.last_name,
                        name = address.name,
                        phone = address.phone,
                        province = address.province.toString(),
                        province_code = address.province_code,
                        zip = address.zip
                    )
                )
                repository.editSingleCustomerAddress(customerId, addressId, updateRequest)
                _actionState.value = NetworkState.Success(Unit)
                fetchAddresses(customerId)
            } catch (e: Exception) {
                _actionState.value = NetworkState.Failure(e)
            }
        }
    }

    fun deleteAddress(customerId: Long, addressId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            _actionState.value = NetworkState.Loading
            try {
                repository.deleteSingleCustomerAddress(customerId, addressId)
                _actionState.value = NetworkState.Success(Unit)
                fetchAddresses(customerId)
            } catch (e: Exception) {
                _actionState.value = NetworkState.Failure(e)
            }
        }
    }

    fun setDefaultAddress(customerId: Long, addressId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            _actionState.value = NetworkState.Loading
            try {
                val defaultRequest = AddressDefaultRequest(AddressDefault(true))
                repository.editSingleCustomerAddressStar(customerId, addressId, defaultRequest)
                _actionState.value = NetworkState.Success(Unit)
                fetchAddresses(customerId)
            } catch (e: Exception) {
                _actionState.value = NetworkState.Failure(e)
            }
        }
    }
} 