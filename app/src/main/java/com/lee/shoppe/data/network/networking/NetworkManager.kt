package com.lee.shoppe.data.network.networking

import com.lee.shoppe.data.model.BrandResponse
import com.lee.shoppe.data.model.CustomerRequest
import com.lee.shoppe.data.model.CustomerResponse
import com.lee.shoppe.data.model.DraftOrderResponse
import com.lee.shoppe.data.model.OneCustomer
import com.lee.shoppe.data.model.PriceRule
import com.lee.shoppe.data.model.ProductResponse
import com.lee.shoppe.data.model.UpdateCustomerRequest
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


}