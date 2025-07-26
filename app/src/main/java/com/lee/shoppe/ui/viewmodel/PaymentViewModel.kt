package com.lee.shoppe.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lee.shoppe.data.model.CheckoutSessionResponse
import com.lee.shoppe.data.repository.Repository
import com.lee.shoppe.data.network.networking.NetworkState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class PaymentViewModel @Inject constructor(
    private val repo: Repository
) : ViewModel() {
    private var _productPayment = MutableStateFlow<NetworkState<CheckoutSessionResponse>>(NetworkState.Loading)
    var productPayment: StateFlow<NetworkState<CheckoutSessionResponse>> = _productPayment

    fun getPaymentProducts(
        successUrl: String,
        cancelUrl: String,
        customerEmail: String,
        currency: String,
        productName: String,
        productDescription: String,
        unitAmountDecimal: Int,
        quantity: Int,
        mode: String,
        paymentMethodType: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = repo.createCheckoutSession(
                    successUrl, cancelUrl, customerEmail, currency, productName, productDescription, unitAmountDecimal, quantity, mode, paymentMethodType
                )
                println("✅ Payment Success: $response")
                _productPayment.value = NetworkState.Success(response)
            } catch (e: Exception) {
                println("❌ Payment Error: ${e.message}")
                _productPayment.value = NetworkState.Failure(e)
            }
        }
    }
} 