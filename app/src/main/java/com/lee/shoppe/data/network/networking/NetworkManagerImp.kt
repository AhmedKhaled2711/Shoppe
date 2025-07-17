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

    override suspend fun addSingleCustomerAddress(
        id: Long,
        addressRequest: AddressRequest
    ): AddressRequest {
        return networkService.addSingleCustomerAddress(id,addressRequest)
    }

    override suspend fun editSingleCustomerAddress(
        customerId: Long,
        id: Long,
        addressRequest: AddressDefaultRequest
    ): AddressUpdateRequest {
        return networkService.editSingleCustomerAddress(customerId,id,addressRequest)
    }

    override suspend fun deleteSingleCustomerAddress(customerId: Long, id: Long) {
        return networkService.deleteSingleCustomerAddress(customerId,id)
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
        return   networkService.createCheckoutSession(successUrl,cancelUrl,customerEmail,currency,productName,productDescription,unitAmountDecimal,quantity,mode,paymentMethodType)

    }

    override suspend fun createOrder(order: Map<String, OrderBody>): OrderBodyResponse {
        return networkService.createOrder(order)
    }

    override suspend fun getSingleOrder(orderId: Long): OrderResponse {
        return networkService.getSingleOrder(orderId)
    }

}