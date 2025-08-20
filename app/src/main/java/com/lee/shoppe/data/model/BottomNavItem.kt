package com.lee.shoppe.data.model


data class BottomNavItem(
    val label: String,
    val icon: Int,
    val route: String,
    val showBadge: Boolean = false
)