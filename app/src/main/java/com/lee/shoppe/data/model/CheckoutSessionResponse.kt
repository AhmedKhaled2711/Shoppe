package com.lee.shoppe.data.model

data class CheckoutSessionResponse(
    val after_expiration: Any,
    val allow_promotion_codes: Any,
    val amount_subtotal: Int,
    val amount_total: Int,
    val automatic_tax: AutomaticTax,
    val billing_address_collection: Any,
    val cancel_url: String,
    val client_reference_id: Any,
    val client_secret: Any,
    val consent: Any,
    val consent_collection: Any,
    val created: Int,
    val currency: String,
    val currency_conversion: Any,
    val custom_fields: List<Any>,
    val custom_text: CustomText,
    val customer: Any,
    val customer_creation: String,
    val customer_details: CustomerDetails,
    val customer_email: String,
    val expires_at: Int,
    val id: String,
    val invoice: Any,
    val invoice_creation: InvoiceCreation,
    val livemode: Boolean,
    val locale: Any,
    val metadata: MetadataX,
    val mode: String,
    val `object`: String,
    val payment_intent: Any,
    val payment_link: Any,
    val payment_method_collection: String,
    val payment_method_configuration_details: Any,
    val payment_method_options: PaymentMethodOptions,
    val payment_method_types: List<String>,
    val payment_status: String,
    val phone_number_collection: PhoneNumberCollection,
    val recovered_from: Any,
    val saved_payment_method_options: Any,
    val setup_intent: Any,
    val shipping_address_collection: Any,
    val shipping_cost: Any,
    val shipping_details: Any,
    val shipping_options: List<Any>,
    val status: String,
    val submit_type: Any,
    val subscription: Any,
    val success_url: String,
    val total_details: TotalDetails,
    val ui_mode: String,
    val url: String
)

data class AutomaticTax(
    val enabled: Boolean,
    val liability: Any,
    val status: Any
)

data class CustomText(
    val after_submit: Any,
    val shipping_address: Any,
    val submit: Any,
    val terms_of_service_acceptance: Any
)

data class CustomerDetails(
    val address: Any,
    val email: String,
    val name: Any,
    val phone: Any,
    val tax_exempt: String,
    val tax_ids: Any
)

data class InvoiceCreation(
    val enabled: Boolean,
    val invoice_data: InvoiceData
)

data class InvoiceData(
    val account_tax_ids: Any,
    val custom_fields: Any,
    val description: Any,
    val footer: Any,
    val issuer: Any,
    val metadata: MetadataX,
    val rendering_options: Any
)

class MetadataX

data class PaymentMethodOptions(
    val card: Card
)

data class Card(
    val request_three_d_secure: String
)

data class PhoneNumberCollection(
    val enabled: Boolean
)

data class TotalDetails(
    val amount_discount: Int,
    val amount_shipping: Int,
    val amount_tax: Int
)