package com.lee.shoppe.data.network.networking

import android.content.Context
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
import com.lee.shoppe.util.NetworkUtil
import kotlinx.coroutines.TimeoutCancellationException
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkManagerImp @Inject constructor(
    private val context: Context,
    private val retrofitHelper: RetrofitHelper,
    private val retrofitHelperPayment: RetrofitHelperPayment
) : NetworkManager {

    private val networkService: NetworkService by lazy {
        retrofitHelper.retrofitInstance.create(NetworkService::class.java)
    }

    private val networkServicePayment: NetworkService by lazy {
        retrofitHelperPayment.retrofitInstance.create(NetworkService::class.java)
    }

    private suspend fun <T> safeApiCall(apiCall: suspend () -> T): T {
        return try {
            if (!NetworkUtil.isNetworkAvailable(context)) {
                throw IOException("No internet connection")
            }
            apiCall()
        } catch (e: Exception) {
            throw when (e) {
                is HttpException -> {
                    val errorMessage = when (e.code()) {
                        401 -> "Authentication failed"
                        403 -> "Access denied"
                        404 -> "Resource not found"
                        429 -> "Too many requests. Please try again later."
                        in 500..599 -> "Server error. Please try again later."
                        else -> "Network error occurred: ${e.message()}"
                    }
                    IOException(errorMessage, e)
                }
                is SocketTimeoutException -> IOException("Connection timeout. Please check your internet connection.", e)
                is UnknownHostException -> IOException("Unable to connect to the server. Please check your internet connection.", e)
                is TimeoutCancellationException -> IOException("Request timed out. Please try again.", e)
                is IOException -> e
                else -> IOException("An unexpected error occurred: ${e.message}", e)
            }
        }
    }



    override suspend fun getSingleCustomer(id: Long, forceRefresh: Boolean): OneCustomer {
        return safeApiCall {
            val cacheControl = if (forceRefresh) "no-cache, no-store, must-revalidate" else null
            networkService.getSingleCustomer(id, cacheControl)
        }
    }

    override suspend fun createCustomer(customer: CustomerRequest): CustomerResponse {
        return safeApiCall {
            networkService.createCustomer(customer)
        }
    }

    override suspend fun createDraftOrders(draftOrder: DraftOrderResponse): DraftOrderResponse {
        return safeApiCall {
            networkService.createDraftOrders(draftOrder)
        }
    }

    override suspend fun updateCustomer(
        id: Long,
        customer: UpdateCustomerRequest
    ): CustomerResponse {
        return safeApiCall {
            networkService.updateCustomer(id, customer)
        }
    }

    override suspend fun getCustomerByEmail(email: String): CustomerResponse {
        return safeApiCall {
            networkService.getCustomerByEmail(email)
        }
    }

    override suspend fun getDiscountCodes(): PriceRule {
        return safeApiCall {
            networkService.getDiscountCodes()
        }
    }

    override suspend fun getBrands(): BrandResponse {
        return safeApiCall {
            networkService.getBrands()
        }
    }

    override suspend fun getBrandProducts(vendor: String): ProductResponse {
        return safeApiCall {
            networkService.getBrandProducts(vendor)
        }
    }

    override suspend fun getDraftOrder(id: Long): DraftOrderResponse {
        return safeApiCall {
            networkService.getDraftOrder(id)
        }
    }

    override suspend fun updateDraftOrder(id: Long, draftOrder: DraftOrderResponse): DraftOrderResponse {
        return safeApiCall {
            networkService.updateDraftOrder(id, draftOrder)
        }
    }

    override suspend fun getProductById(id: Long): ProductResponse {
        return safeApiCall {
            networkService.getProductById(id)
        }
    }

    override suspend fun addSingleCustomerAddress(
        id: Long,
        addressRequest: AddressRequest
    ): AddressRequest {
        return safeApiCall {
            networkService.addSingleCustomerAddress(id, addressRequest)
        }
    }

    override suspend fun editSingleCustomerAddress(
        customerId: Long,
        id: Long,
        addressRequest: AddressUpdateRequest
    ): AddressUpdateRequest {
        return safeApiCall {
            networkService.editSingleCustomerAddress(customerId, id, addressRequest)
        }
    }

    override suspend fun editSingleCustomerAddressStar(
        customerId: Long,
        id: Long,
        addressRequest: AddressDefaultRequest
    ): AddressUpdateRequest {
        return safeApiCall {
            networkService.editSingleCustomerAddressStar(customerId, id, addressRequest)
        }
    }

    override suspend fun getCustomerOrders(
        userId: Long,
        forceRefresh: Boolean
    ): OrderResponse {
        return safeApiCall {
            val cacheControl = if (forceRefresh) "no-cache, no-store, must-revalidate" else null
            networkService.getCustomerOrders(userId, cacheControl)
        }
    }

    override suspend fun deleteSingleCustomerAddress(customerId: Long, id: Long) {
        return safeApiCall {
            networkService.deleteSingleCustomerAddress(customerId, id)
        }
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
        return safeApiCall {
            networkServicePayment.createCheckoutSession(
                successUrl = successUrl,
                cancelUrl = cancelUrl,
                customerEmail = customerEmail,
                currency = currency,
                productName = productName,
                productDescription = productDescription,
                unitAmountDecimal = unitAmountDecimal,
                quantity = quantity,
                mode = mode,
                paymentMethodType = paymentMethodType
            )
        }
    }

    override suspend fun createOrder(order: Map<String, OrderBody>): OrderBodyResponse {
        return safeApiCall {
            networkService.createOrder(order)
        }
    }

    override suspend fun getSingleOrder(orderId: Long): OrderResponse {
        return safeApiCall {
            networkService.getSingleOrder(orderId)
        }
    }

}