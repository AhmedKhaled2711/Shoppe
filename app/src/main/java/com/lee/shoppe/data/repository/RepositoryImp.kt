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

    override suspend fun addSingleCustomerAddress(
        id: Long,
        addressRequest: AddressRequest
    ): AddressRequest {
        return networkManager.addSingleCustomerAddress(id,addressRequest)
    }

    override suspend fun editSingleCustomerAddress(
        customerId: Long,
        id: Long,
        addressRequest: AddressUpdateRequest
    ): AddressUpdateRequest {
        return networkManager.editSingleCustomerAddress(customerId, id, addressRequest)
    }

    override suspend fun deleteSingleCustomerAddress(customerId: Long, id: Long) {
        return networkManager.deleteSingleCustomerAddress(customerId,id)
    }

    override suspend fun createCheckoutSession(
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
    ): CheckoutSessionResponse {
        return  networkManager.createCheckoutSession(successUrl,cancelUrl,customerEmail,currency,productName,productDescription,unitAmountDecimal,quantity,mode,paymentMethodType)

    }

    override suspend fun createOrder(order: Map<String, OrderBody>): Flow<OrderBodyResponse> {
        return flowOf(networkManager.createOrder(order))
    }

    override suspend fun getSingleOrder(orderId: Long): Flow<OrderResponse> {
        return flow {
            emit(networkManager.getSingleOrder(orderId))
        }
    }

    override suspend fun editSingleCustomerAddressStar(
        customerId: Long,
        id: Long,
        addressRequest: AddressDefaultRequest
    ): AddressUpdateRequest {
        return networkManager.editSingleCustomerAddressStar(customerId, id, addressRequest)
    }

}