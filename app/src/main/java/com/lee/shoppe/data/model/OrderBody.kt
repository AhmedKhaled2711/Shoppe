package com.example.fashionshop.Model

// Address class
data class AddressBody(
    val first_name: String,
    val address1: String,
    val phone: String,
    val city: String,
    val zip: String,
    val country: String,
    val last_name: String,
    val name: String,
    val country_code: String,
)


data class DefaultAddressBody(
    val first_name: String,
    val last_name: String,
    val address1: String,
    val city: String,
    val country: String,
    val zip: String,
    val phone: String,
    val name: String,
    val country_code: String,
    val default: Boolean
)

data class CustomerBody(
    val id: Long,
    val email: String,
    val first_name: String,
    val last_name: String,
    val currency: String,
    val default_address: DefaultAddressBody
)

// LineItem class
data class LineItemBody(
    val variant_id: Long?,
    var quantity: Int?,
    val id: Long? = null,
    val title: String? = "dummy",
    val price: String? = "1",
    val sku: String? = null,
    val properties: List<Property> = listOf(Property("custom engraving", "Happy Birthday Mom!"))
) {
    data class Property(
        val name: String,
        val value: String
    )
}


// Order class
data class OrderBody(
    val billing_address: AddressBody,
    val customer: CustomerBody,
    val line_items: List<LineItemBody>,
    val total_tax: Double,
    val currency: String,
    val total_discounts : String ,
    val referring_site : String

)


data class OrderBodyResponse (
    val order: OrderBody? = null
)





