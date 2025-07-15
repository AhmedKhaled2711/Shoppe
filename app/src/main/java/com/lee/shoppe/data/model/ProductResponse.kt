package com.lee.shoppe.data.model

import java.io.Serializable

data class ProductResponse(
    val products: List<Product>? = null,
    val product: ProductDetails? = null
)

data class Product(
    val id: Long? = null,
    val title: String? = null,
    val image: ProductImage? = null,
    val variants: List<Variant>? = null,
    val tags: String? = null,
    val product_type: String? = null
) : Serializable

data class Variant(
    val price: String? = null,
    val inventory_quantity:Int = 1,
    val id: Long? = null,
    val title: String? = null,
    val option1: String? = null,
    val option2: String? = null
)

data class ProductImage(
    val src: String? = null
)

data class ProductDetails(
    val id: Long? = null,
    val title: String? = null,
    val body_html: String? = null,
    val image: ProductImage? = null,
    val images: List<ProductImage>? = null,
    val variants: List<Variant>? = null,
    val vendor: String? = null
) : Serializable