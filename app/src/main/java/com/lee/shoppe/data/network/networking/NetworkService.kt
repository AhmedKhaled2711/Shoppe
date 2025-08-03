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
import com.lee.shoppe.data.model.Customers
import com.lee.shoppe.data.model.DraftOrderResponse
import com.lee.shoppe.data.model.OneCustomer
import com.lee.shoppe.data.model.PriceRule
import com.lee.shoppe.data.model.ProductResponse
import com.lee.shoppe.data.model.UpdateCustomerRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface NetworkService {

    @GET("customers.json")
    suspend fun getCustomers(): Customers

    @POST("customers.json")
    suspend fun createCustomer(
        @Body customer: CustomerRequest
    ): CustomerResponse

    @GET("customers/{id}.json")
    suspend fun getSingleCustomer(
        @Path("id") id: Long,
        @Header("Cache-Control") cacheControl: String? = null
    ): OneCustomer

    @POST("draft_orders.json")
    suspend fun createDraftOrders(
        @Body draftOrder: DraftOrderResponse
    ): DraftOrderResponse

    @PUT("customers/{id}.json")
    suspend fun updateCustomer(
        @Path("id") id: Long,
        @Body customer: UpdateCustomerRequest
    ): CustomerResponse

    @GET("customers/search.json")
    suspend fun getCustomerByEmail(
        @Query("email") email: String
    ): CustomerResponse

    @GET("price_rules.json")
    suspend fun getDiscountCodes(): PriceRule

    @GET("smart_collections.json")
    suspend fun getBrands(): BrandResponse

    @GET("products.json")
    suspend fun getBrandProducts(@Query("vendor") vendor: String): ProductResponse
    
    @GET("draft_orders/{id}.json")
    suspend fun getDraftOrder(@Path("id") id: Long): DraftOrderResponse
    
    @PUT("draft_orders/{id}.json")
    suspend fun updateDraftOrder(
        @Path("id") id: Long,
        @Body draftOrder: DraftOrderResponse
    ): DraftOrderResponse

    @GET("products/{id}.json")
    suspend fun getProductById(@Path("id") id: Long): ProductResponse

    //Address
    @POST("customers/{id}/addresses.json")
    suspend fun addSingleCustomerAddress( @Path("id") id: Long, @Body addressRequest: AddressRequest): AddressRequest
    @PUT("customers/{CustomerId}/addresses/{id}.json")
    suspend fun editSingleCustomerAddress(
        @Path("CustomerId") customerId: Long,
        @Path("id") id: Long,
        @Body addressRequest: AddressUpdateRequest
    ): AddressUpdateRequest
    @DELETE("customers/{CustomerId}/addresses/{id}.json")
    suspend fun deleteSingleCustomerAddress(
        @Path("CustomerId") customerId: Long,
        @Path("id") id: Long,
    )
    @PUT("customers/{CustomerId}/addresses/{id}.json")
    suspend fun editSingleCustomerAddressStar(
        @Path("CustomerId") customerId: Long,
        @Path("id") id: Long,
        @Body addressRequest: AddressDefaultRequest
    ): AddressUpdateRequest

    @FormUrlEncoded
    @POST("v1/checkout/sessions")
    suspend fun createCheckoutSession(
        @Field("success_url") successUrl: String,
        @Field("cancel_url") cancelUrl: String,
        @Field("customer_email") customerEmail: String,
        @Field("line_items[0][price_data][currency]") currency: String,
        @Field("line_items[0][price_data][product_data][name]") productName: String,
        @Field("line_items[0][price_data][product_data][description]") productDescription: String,
        @Field("line_items[0][price_data][unit_amount_decimal]") unitAmountDecimal: Int,
        @Field("line_items[0][quantity]") quantity: Int,
        @Field("mode") mode: String,
        @Field("payment_method_types[0]") paymentMethodType: String
    ): CheckoutSessionResponse

    @POST("orders.json")
    suspend fun createOrder(
        @Body order:  Map<String, OrderBody>
    ): OrderBodyResponse

    @GET("orders/{id}.json")
    suspend fun getSingleOrder(@Path("id") orderId: Long): OrderResponse

    @GET("customers/{id}/orders.json")
    suspend fun getCustomerOrders(@Path("id") userId: Long): OrderResponse
}