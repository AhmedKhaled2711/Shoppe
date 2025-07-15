package com.lee.shoppe.ui.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.lee.shoppe.data.model.CustomerData
import com.lee.shoppe.data.model.CustomerRequest
import com.lee.shoppe.data.model.CustomerResponse
import com.lee.shoppe.data.model.DraftOrderResponse
import com.lee.shoppe.data.model.UpdateCustomerRequest
import com.lee.shoppe.data.network.networking.NetworkState
import com.lee.shoppe.data.repository.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.asStateFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AuthenticationViewModel @Inject constructor(
    private var repository: Repository
) : ViewModel() {

    private var _customers = MutableStateFlow<NetworkState<CustomerResponse>>(NetworkState.Loading)
    var customers: StateFlow<NetworkState<CustomerResponse>> = _customers

    private var _customer = MutableStateFlow<NetworkState<CustomerResponse>>(NetworkState.Loading)
    var customer: StateFlow<NetworkState<CustomerResponse>> = _customer

    private val nameRegex = Regex("^[a-zA-Z]{3,}$")
    private val emailRegex = Regex("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$")
    private val passwordRegex = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]{8,}$")

    private val _signupUiState = MutableStateFlow(SignupUiState())
    val signupUiState = _signupUiState.asStateFlow()

    fun createCustomer(customer: CustomerRequest) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.createCustomer(customer)
                .catch {_customer.value = NetworkState.Failure(it) }
                .collect {updateCustomer(it.customer!!.id) }
        }
    }

    fun updateCustomer(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val draftOrderResponse = DraftOrderResponse(DraftOrderResponse.DraftOrder())
            var favListId = 0L
            var cartId = 0L

            repository.createDraftOrders(draftOrderResponse)
                .catch {_customer.value = NetworkState.Failure(it)}
                .collect { favListId = it.draft_order.id }
            repository.createDraftOrders(draftOrderResponse)
                .catch {_customer.value = NetworkState.Failure(it)}
                .collect { cartId = it.draft_order.id }

            val updateCustomerRequest = UpdateCustomerRequest(
                UpdateCustomerRequest.Customer(favListId, cartId)
            )
            repository.updateCustomer(id, updateCustomerRequest)
                .catch {_customer.value = NetworkState.Failure(it)}
                .collect {_customer.value = NetworkState.Success(it)}
        }
    }

    fun getCustomerByEmail(email: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getCustomerByEmail(email)
                .catch { 
                    Log.e("Backend", "Error fetching customer for $email", it)
                    _customers.value = NetworkState.Failure(it) 
                }
                .collect { 
                    Log.d("Backend", "Customer response for $email: $it")
                    _customers.value = NetworkState.Success(it) 
                }
        }
    }

    fun saveCustomerData(context: Context, data: CustomerResponse.Customer){
        val customer = CustomerData.getInstance(context)
        customer.isLogged = true
        customer.id = data.id
        customer.name = data.first_name
        customer.email = data.email
        customer.currency = data.currency
        customer.favListId = data.note
        customer.cartListId = data.multipass_identifier
        // Save login state in SharedPreferenceManager
        val sharedPrefs = com.lee.shoppe.data.network.caching.SharedPreferenceManager(context)
        sharedPrefs.save(com.lee.shoppe.data.network.caching.SharedPreferenceManager.Key.IS_LOGGED_IN, "true")
    }

    fun onFirstNameChange(value: String) {
        _signupUiState.update { it.copy(firstName = value, firstNameError = false) }
    }
    fun onLastNameChange(value: String) {
        _signupUiState.update { it.copy(lastName = value, lastNameError = false) }
    }
    fun onEmailChange(value: String) {
        _signupUiState.update { it.copy(email = value, emailError = false) }
    }
    fun onPasswordChange(value: String) {
        _signupUiState.update { it.copy(password = value, passwordError = false) }
    }
    fun onConfirmPasswordChange(value: String) {
        _signupUiState.update { it.copy(confirmPassword = value, confirmPasswordError = false) }
    }

    fun signup() {
        val state = _signupUiState.value
        var valid = true
        var firstNameError = false
        var lastNameError = false
        var emailError = false
        var passwordError = false
        var confirmPasswordError = false
        var errorMessage: String? = null

        if (state.firstName.isBlank() || !nameRegex.matches(state.firstName)) {
            firstNameError = true
            valid = false
        }
        if (state.lastName.isBlank() || !nameRegex.matches(state.lastName)) {
            lastNameError = true
            valid = false
        }
        if (state.email.isBlank() || !emailRegex.matches(state.email)) {
            emailError = true
            valid = false
        }
        if (state.password.isBlank() || !passwordRegex.matches(state.password)) {
            passwordError = true
            valid = false
            errorMessage = "Password must be at least 8 characters, with uppercase, lowercase, and a digit."
        }
        if (!passwordError && (state.confirmPassword.isBlank() || state.password != state.confirmPassword)) {
            confirmPasswordError = true
            valid = false
            errorMessage = "Passwords do not match."
        }
        if (!valid) {
            _signupUiState.update {
                it.copy(
                    firstNameError = firstNameError,
                    lastNameError = lastNameError,
                    emailError = emailError,
                    passwordError = passwordError,
                    confirmPasswordError = confirmPasswordError,
                    errorMessage = errorMessage
                )
            }
            return
        }
        // Proceed with signup
        _signupUiState.update { it.copy(isLoading = true, errorMessage = null) }
        val request = CustomerRequest(
            CustomerRequest.Customer(
                first_name = state.firstName + " " + state.lastName,
                last_name = "",
                email = state.email.trim()
            )
        )
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.createCustomer(request)
                    .catch { e ->
                        _signupUiState.update { it.copy(isLoading = false, errorMessage = e.message) }
                        Log.e("Signup", "Signup failed: ${e.message}")

                    }
                    .collect {
                        _signupUiState.update { it.copy(isLoading = false, signupSuccess = true) }
                        Log.d("Signup", "Signup operation completed successfully")

                    }
            } catch (e: Exception) {
                _signupUiState.update { it.copy(isLoading = false, errorMessage = e.message) }
                Log.e("Signup", "Signup exception caught: ${e.message}")

            }
        }
    }
    fun clearSignupSuccess() {
        _signupUiState.update { it.copy(signupSuccess = false) }
    }

    override fun onCleared() {
        super.onCleared()
    }
}