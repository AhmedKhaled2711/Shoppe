package com.lee.shoppe.ui.navigation

sealed class Screen(val route: String) {

    data object Start : Screen("start")
    data object Login : Screen("login")
    data object Signup : Screen("signup")
    data object Home : Screen("home")
    data object Onboarding : Screen("onboarding")
    data object Category : Screen("category")
    data object Cart : Screen("cart")
    data object Favorite : Screen("favorite")
    data object Profile : Screen("profile")
    data object ProductDetails : Screen("product_details/{productId}")

    // Add new routes for payment/order flow
    data object Payment : Screen("payment")
    data object PaymentSheet : Screen("payment_sheet?paymentUrl={paymentUrl}&titlesList={titlesList}")
    data object OrderDetails : Screen("order_details?titlesList={titlesList}&paymentMethod={paymentMethod}")
    data object ChooseAddress : Screen("choose_address")




}
