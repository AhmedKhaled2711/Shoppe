package com.lee.shoppe.data.network.networking

import com.lee.shoppe.data.model.BrandResponse
import com.lee.shoppe.data.model.CustomerRequest
import com.lee.shoppe.data.model.CustomerResponse
import com.lee.shoppe.data.model.DraftOrderResponse
import com.lee.shoppe.data.model.OneCustomer
import com.lee.shoppe.data.model.PriceRule
import com.lee.shoppe.data.model.ProductResponse
import com.lee.shoppe.data.model.UpdateCustomerRequest

class NetworkManagerImp private constructor(): NetworkManager {

    private val networkService : NetworkService by lazy {
        RetrofitHelper.retrofitInstance.create(NetworkService::class.java)
    }

    companion object{
        private var instance: NetworkManagerImp?=null
        fun getInstance(): NetworkManagerImp {
            return instance ?: synchronized(this){
                val temp= NetworkManagerImp()
                instance =temp
                temp
            }
        }
    }

    override suspend fun getCustomers(id: Long): OneCustomer {
        val response= networkService.getSingleCustomer(id)
        return response
    }

    override suspend fun createCustomer(customer: CustomerRequest): CustomerResponse {
        return networkService.createCustomer(customer)
    }

    override suspend fun createDraftOrders(draftOrder: DraftOrderResponse): DraftOrderResponse {
        return networkService.createDraftOrders(draftOrder)
    }

    override suspend fun updateCustomer(
        id: Long,
        customer: UpdateCustomerRequest
    ): CustomerResponse {
        return networkService.updateCustomer(id, customer)
    }

    override suspend fun getCustomerByEmail(email: String): CustomerResponse {
        return networkService.getCustomerByEmail(email)
    }

    override suspend fun getDiscountCodes(): PriceRule {
        return networkService.getDiscountCodes()
    }

    override suspend fun getBrands(): BrandResponse {
        return networkService.getBrands()
    }

    override suspend fun getBrandProducts(vendor: String): ProductResponse {
        return  networkService.getBrandProducts(vendor)
    }

    override suspend fun getDraftOrder(id: Long): DraftOrderResponse {
        return networkService.getDraftOrder(id)
    }

    override suspend fun updateDraftOrder(id: Long, draftOrder: DraftOrderResponse): DraftOrderResponse {
        return networkService.updateDraftOrder(id, draftOrder)
    }

    override suspend fun getProductById(id: Long): ProductResponse {
        return networkService.getProductById(id)
    }

}