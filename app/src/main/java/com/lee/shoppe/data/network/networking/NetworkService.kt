package com.lee.shoppe.data.network.networking

import com.lee.shoppe.data.model.BrandResponse
import com.lee.shoppe.data.model.CustomerRequest
import com.lee.shoppe.data.model.CustomerResponse
import com.lee.shoppe.data.model.Customers
import com.lee.shoppe.data.model.DraftOrderResponse
import com.lee.shoppe.data.model.OneCustomer
import com.lee.shoppe.data.model.PriceRule
import com.lee.shoppe.data.model.ProductResponse
import com.lee.shoppe.data.model.UpdateCustomerRequest
import retrofit2.http.Body
import retrofit2.http.GET
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
    suspend fun getSingleCustomer(@Path("id") id: Long): OneCustomer

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
}