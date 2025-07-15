package com.lee.shoppe.data.repository

import com.lee.shoppe.data.model.BrandResponse
import com.lee.shoppe.data.model.CustomerRequest
import com.lee.shoppe.data.model.CustomerResponse
import com.lee.shoppe.data.model.DraftOrderResponse
import com.lee.shoppe.data.model.OneCustomer
import com.lee.shoppe.data.model.PriceRule
import com.lee.shoppe.data.model.ProductResponse
import com.lee.shoppe.data.model.UpdateCustomerRequest
import kotlinx.coroutines.flow.Flow
import retrofit2.http.Query

interface Repository {

    suspend fun getCustomers( id: Long): OneCustomer
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


}