package com.example.fashionshop.Model

import com.lee.shoppe.data.model.DraftOrderResponse
import java.io.Serializable

data class OrderResponse (
    val orders: List<Order>? = null,
    val order: Order? = null
)

data class Order (
    val id: Long? = null,
    val admin_graphql_api_id: String? = null,
    val browser_ip: String? = null,
    val buyer_accepts_marketing: Boolean? = null,
    val cart_token: String? = null,
    val checkout_id: Long? = null,
    val checkout_token: String? = null,
    val confirmed: Boolean? = null,
    val contact_email: String? = null,
    val created_at: String? = null,
    val currency: OrderCurrency? = null,
    val current_subtotal_price: String? = null,
    val current_total_discounts: String? = null,
    val current_total_price: String? = null,
    val current_total_tax: String? = null,
    val email: String? = null,
    val estimated_taxes: Boolean? = null,
    val financial_status: String? = null,
    val landing_site: String? = null,
    val landing_site_ref: String? = null,
    val number: Long? = null,
    val order_number: Long? = null,
    val order_status_url: String? = null,
    val payment_gateway_names: List<String>? = null,
    val phone: String? = null,
    val po_number: String? = null,
    val presentment_currency: OrderCurrency? = null,
    val processed_at: String? = null,
    val reference: String? = null,
    val referring_site: String? = null,
    val source_identifier: String? = null,
    val source_name: String? = null,
    val subtotal_price: String? = null,
    val tags: String? = null,
    val taxExempt: Boolean? = null,
    val test: Boolean? = null,
    val token: String? = null,
    val total_discounts: String? = null,
    val total_line_items_price: String? = null,
    val total_outstanding: String? = null,
    val total_price: String? = null,
    val total_tax: String? = null,
    val total_tip_received: String? = null,
    val total_weight: Long? = null,
    val updated_at: String? = null,
    val billing_address: AddressOrder? = null,
    val shipping_address: AddressOrder? = null,
    val line_items: List<LineItemBody>? = null
): Serializable

data class AddressOrder (
    val first_name: String? = null,
    val address1: String? = null,
    val phone: String? = null,
    val city: String? = null,
    val zip: String? = null,
    val province: String? = null,
    val country: String? = null,
    val last_name: String? = null,
    val address2: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val name: String? = null,
    val country_code: String? = null,
    val province_code: String? = null,
    val id: Long? = null,
    val customer_id: Long? = null,
    val country_name: String? = null,
    val default: Boolean? = null,
    val line_items: List<DraftOrderResponse.DraftOrder.LineItem>? = null
)

enum class OrderCurrency(val value: String) {
    Usd("USD");
}