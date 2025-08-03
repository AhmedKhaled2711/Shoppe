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

    private val _singleAddress = MutableStateFlow<NetworkState<Address>>(NetworkState.Idle)
    val singleAddress: StateFlow<NetworkState<Address>> = _singleAddress.asStateFlow()

    fun resetSingleAddressState() {
        _singleAddress.value = NetworkState.Idle
    }


    fun fetchAddress(customerId: Long, addressId: Long, forceRefresh: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            Log.d("AddressViewModel", "Fetching address $addressId for customer $customerId, forceRefresh: $forceRefresh")
            _singleAddress.value = NetworkState.Loading
            try {
                // Use getSingleCustomer with forceRefresh to ensure we get fresh data when needed
                val oneCustomer = repository.getSingleCustomer(customerId, forceRefresh)
                val address = oneCustomer.customer.addresses.find { it.id == addressId }
                
                if (address != null) {
                    Log.d("AddressViewModel", "Successfully found address: $address")
                    _singleAddress.value = NetworkState.Success(address)
                } else {
                    val error = NoSuchElementException("Address with ID $addressId not found for customer $customerId")
                    Log.e("AddressViewModel", error.message ?: "Address not found")
                    _singleAddress.value = NetworkState.Failure(error)
                }
            } catch (e: Exception) {
                Log.e("AddressViewModel", "Error fetching address: ${e.message}", e)
                _singleAddress.value = NetworkState.Failure(e)
            }
        }
    }


    fun fetchAddresses(customerId: Long, forceRefresh: Boolean = true) {
        viewModelScope.launch(Dispatchers.IO) {
            Log.d("AddressViewModel", "Fetching addresses for customer $customerId, forceRefresh: $forceRefresh")
            _addresses.value = NetworkState.Loading
            try {
                // Use the new forceRefresh parameter in getSingleCustomer
                val oneCustomer = repository.getSingleCustomer(customerId, forceRefresh)
                val addresses = oneCustomer.customer.addresses
                Log.d("AddressViewModel", "Fetched ${addresses.size} addresses")
                _addresses.value = NetworkState.Success(addresses)
            } catch (e: Exception) {
                Log.e("AddressViewModel", "Error fetching addresses", e)
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
                        address2 = address.address2,
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
            Log.d("AddressViewModel", "Deleting address $addressId for customer $customerId")
            _actionState.value = NetworkState.Loading
            try {
                // Delete the address
                repository.deleteSingleCustomerAddress(customerId, addressId)
                Log.d("AddressViewModel", "Successfully deleted address $addressId")
                
                // Update the UI immediately by filtering out the deleted address
                val currentState = _addresses.value
                if (currentState is NetworkState.Success) {
                    val updatedList = currentState.data.filter { it.id != addressId }
                    _addresses.value = NetworkState.Success(updatedList)
                    Log.d("AddressViewModel", "Immediately updated UI with ${updatedList.size} addresses")
                }
                
                // Force a fresh fetch from server with cache busting
                fetchAddresses(customerId, true)
                
                _actionState.value = NetworkState.Success(Unit)
            } catch (e: Exception) {
                Log.e("AddressViewModel", "Error deleting address $addressId", e)
                _actionState.value = NetworkState.Failure(e)
                
                // Even if there's an error, try to refresh the list
                fetchAddresses(customerId, true)
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
    
    fun resetActionState() {
        _actionState.value = NetworkState.Idle
    }
}