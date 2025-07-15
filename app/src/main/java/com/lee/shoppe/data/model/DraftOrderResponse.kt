package com.lee.shoppe.data.model

data class DraftOrderResponse(
    val draft_order: DraftOrder
) {
    data class DraftOrder(
        val id: Long = 0,
        val line_items: List<LineItem> = listOf(LineItem(null, quantity = 1)),
    ) {
        data class LineItem(
            val variant_id: Long?,
            var quantity: Int?,
            val id: Long? = null,
            val title: String? = "dummy",
            var price: String? = "1",
            val sku: String? = null,
            val product_id : Long? = null,
            val properties: List<Property> = listOf(Property("custom engraving", "Happy Birthday Mom!"))
        ) {
            data class Property(
                val name: String,
                val value: String
            )
        }




    }
}