package com.lee.shoppe.data.repository

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
import kotlinx.coroutines.flow.Flow
import retrofit2.http.Path
import retrofit2.http.Query

interface Repository {

    suspend fun createCustomer(customer: CustomerRequest): Flow<CustomerResponse>
    suspend fun createDraftOrders(draftOrder: DraftOrderResponse): Flow<DraftOrderResponse>
    suspend fun updateCustomer(id: Long, customer: UpdateCustomerRequest): Flow<CustomerResponse>
    suspend fun getCustomerByEmail(email: String): Flow<CustomerResponse>
    suspend fun getBrands(): Flow<BrandResponse>
    suspend fun getDiscountCodes():Flow <PriceRule>
    suspend fun getBrandProducts(@Query("vendor") vendor: String): Flow<ProductResponse>
    suspend fun getDraftOrder(id: Long): Flow<DraftOrderResponse>
    suspend fun updateDraftOrder(id: Long, draftOrder: DraftOrderResponse): Flow<DraftOrderResponse>
    suspend fun getProductById(id: Long): Flow<ProductResponse>
    suspend fun addSingleCustomerAddress( id: Long,addressRequest: AddressRequest): AddressRequest
    suspend fun editSingleCustomerAddress(customerId:Long,id:Long,addressRequest: AddressUpdateRequest): AddressUpdateRequest
    suspend fun deleteSingleCustomerAddress(customerId:Long,id:Long)
    suspend fun createCheckoutSession(successUrl: String,cancelUrl: String,customerEmail: String,currency: String,productName: String,productDescription: String,
                                      unitAmountDecimal: Int, quantity: Int,mode: String, paymentMethodType: String
    ): CheckoutSessionResponse
    suspend fun createOrder(order:  Map<String, OrderBody>): Flow<OrderBodyResponse>

    suspend fun getSingleOrder(@Path("id") orderId: Long): Flow<OrderResponse>
    suspend fun editSingleCustomerAddressStar(
        customerID:Long,
        id: Long,
        addressRequest: AddressDefaultRequest
    ): AddressUpdateRequest
    suspend fun getCustomerOrders(
        @Path("id") userId: Long,
        forceRefresh: Boolean = false
    ): OrderResponse
    suspend fun getSingleCustomer(id: Long, forceRefresh: Boolean = false): OneCustomer

}