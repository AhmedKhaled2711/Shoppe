package com.lee.shoppe.data.network.networking

import com.example.fashionshop.Model.OrderBody
import com.example.fashionshop.Model.OrderBodyResponse
import com.example.fashionshop.Model.OrderResponse
import com.lee.shoppe.data.model.AddressDefaultRequest
import com.lee.shoppe.data.model.AddressRequest
import com.lee.shoppe.data.model.AddressUpdateRequest
import com.lee.shoppe.data.model.BrandResponse
import com.lee.shoppe.data.model.CheckoutSessionResponse
import com.lee.shoppe.data.model.CustomerRequest
import com.lee.shoppe.data.model.CustomerResponse
import com.lee.shoppe.data.model.DraftOrderResponse
import com.lee.shoppe.data.model.OneCustomer
import com.lee.shoppe.data.model.PriceRule
import com.lee.shoppe.data.model.ProductResponse
import com.lee.shoppe.data.model.UpdateCustomerRequest
import retrofit2.http.Path
import retrofit2.http.Query

interface NetworkManager {

    suspend fun getCustomers( id: Long): OneCustomer
    suspend fun createCustomer(customer: CustomerRequest): CustomerResponse
    suspend fun createDraftOrders(draftOrder: DraftOrderResponse): DraftOrderResponse
    suspend fun updateCustomer(id: Long, customer: UpdateCustomerRequest): CustomerResponse
    suspend fun getCustomerByEmail(email: String): CustomerResponse
    suspend fun getDiscountCodes(): PriceRule
    suspend fun getBrands(): BrandResponse
    suspend fun getBrandProducts(@Query("vendor") vendor: String): ProductResponse
    suspend fun getDraftOrder(id: Long): DraftOrderResponse
    suspend fun updateDraftOrder(id: Long, draftOrder: DraftOrderResponse): DraftOrderResponse
    suspend fun getProductById(id: Long): ProductResponse
    suspend fun addSingleCustomerAddress( id: Long,addressRequest: AddressRequest): AddressRequest
    suspend fun editSingleCustomerAddress(customerId:Long,id:Long,addressRequest: AddressDefaultRequest): AddressUpdateRequest
    suspend fun deleteSingleCustomerAddress(customerId:Long,id:Long)
    suspend fun createCheckoutSession(successUrl: String,cancelUrl: String,customerEmail: String,currency: String,productName: String,productDescription: String,
                                      unitAmountDecimal: Int, quantity: Int,mode: String, paymentMethodType: String
    ): CheckoutSessionResponse
    suspend fun createOrder(order:  Map<String, OrderBody>): OrderBodyResponse

    suspend fun getSingleOrder(@Path("id") orderId: Long): OrderResponse


}