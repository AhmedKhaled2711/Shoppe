package com.lee.shoppe.data.model

data class CustomerRequest(val customer: Customer) {
    data class Customer(
        val first_name: String?,
        val last_name: String?,
        val email: String?
    )
}

data class UpdateCustomerRequest(val customer: Customer) {
    data class Customer(
        val note: Long?,
        val multipass_identifier: Long?,
    )
}

data class CustomerResponse(
    val customer: Customer? = null,
    val customers: List<Customer>? = null,
) {
    data class Customer(
        val id: Long,
        val first_name: String,
        val last_name: String,
        val email: String,
        val phone: String,
        val currency: String,
        var note: Long,
        var multipass_identifier: Long,
    )
}