package com.lee.shoppe.data.model

import java.io.Serializable

data class Address(
    val address1: String,
    val address2: Any,
    val city: String,
    val company: Any,
    val country: String,
    val country_code: String,
    val country_name: String,
    val customer_id: Long,
    var default: Boolean,
    val first_name: String,
    val id: Long,
    val last_name: String,
    val name: String,
    val phone: String,
    val province: Any,
    val province_code: Any,
    val zip: String
) : Serializable

data class AddressRequest(  val address: Address)

data class AddressDefaultRequest(val address: AddressDefault)

data class AddressDefault(val default: Boolean)

data class AddressUpdateRequest(val customer_address: CustomerAddress)

data class CustomerAddress(
    val address1: String,
    val address2: String,
    val city: String,
    val company: String,
    val country: String,
    val country_code: String,
    val country_name: String,
    val customer_id: Long,
    val default: Boolean,
    val first_name: String,
    val id: Long,
    val last_name: String,
    val name: String,
    val phone: String,
    val province: String,
    val province_code: Any,
    val zip: String
)
