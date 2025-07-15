package com.lee.shoppe.data.repository

import com.lee.shoppe.data.model.BrandResponse
import com.lee.shoppe.data.model.CustomerRequest
import com.lee.shoppe.data.model.CustomerResponse
import com.lee.shoppe.data.model.DraftOrderResponse
import com.lee.shoppe.data.model.OneCustomer
import com.lee.shoppe.data.model.PriceRule
import com.lee.shoppe.data.model.ProductResponse
import com.lee.shoppe.data.model.UpdateCustomerRequest
import com.lee.shoppe.data.network.networking.NetworkManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class RepositoryImp @Inject constructor(
    private val networkManager: NetworkManager
) : Repository {

    companion object {
        private var instance: RepositoryImp? = null
        fun getInstance(productNetworkManager: NetworkManager)
                : RepositoryImp {
            return instance ?: synchronized(this) {
                val temp = RepositoryImp(productNetworkManager)
                instance = temp
                temp
            }
        }
    }

    override suspend fun getCustomers(id: Long): OneCustomer {
        return networkManager.getCustomers(id)
    }

    override suspend fun createCustomer(customer: CustomerRequest): Flow<CustomerResponse> {
        return flowOf(networkManager.createCustomer(customer))
    }

    override suspend fun createDraftOrders(draftOrder: DraftOrderResponse): Flow<DraftOrderResponse> {
        return flowOf(networkManager.createDraftOrders(draftOrder))
    }

    override suspend fun updateCustomer(
        id: Long,
        customer: UpdateCustomerRequest
    ): Flow<CustomerResponse> {
        return flowOf(networkManager.updateCustomer(id, customer))
    }

    override suspend fun getCustomerByEmail(email: String): Flow<CustomerResponse> {
        return flowOf(networkManager.getCustomerByEmail(email))
    }

    override suspend fun getDiscountCodes(): Flow<PriceRule> {
        return flowOf(networkManager.getDiscountCodes())

    }

    override suspend fun getBrands(): Flow<BrandResponse> {
        return flow { emit(networkManager.getBrands()) }
    }

    override suspend fun getBrandProducts(vendor: String): Flow<ProductResponse> {
        return flow { emit(networkManager.getBrandProducts(vendor)) }
    }

    override suspend fun getDraftOrder(id: Long): Flow<DraftOrderResponse> {
        return flow { emit(networkManager.getDraftOrder(id)) }
    }

    override suspend fun updateDraftOrder(id: Long, draftOrder: DraftOrderResponse): Flow<DraftOrderResponse> {
        return flow { emit(networkManager.updateDraftOrder(id, draftOrder)) }
    }

    override suspend fun getProductById(id: Long): Flow<ProductResponse> {
        return flowOf(networkManager.getProductById(id))
    }

}