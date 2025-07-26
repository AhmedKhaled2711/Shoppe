package com.lee.shoppe.ui.navigation

import MapPickerScreen
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import com.lee.shoppe.ui.screens.OrdersScreen
import com.lee.shoppe.data.model.BottomNavItem
import com.lee.shoppe.data.model.CustomerData
import com.lee.shoppe.data.network.caching.SharedPreferenceManager
import com.lee.shoppe.ui.screens.AboutScreen
import com.lee.shoppe.ui.screens.AddEditAddressScreen
import com.lee.shoppe.ui.screens.AddressListScreen
import com.lee.shoppe.ui.screens.CartScreen
import com.lee.shoppe.ui.screens.CategoryScreen
import com.lee.shoppe.ui.screens.ChooseAddressScreen
import com.lee.shoppe.ui.screens.FavoriteScreen
import com.lee.shoppe.ui.screens.HomeScreen
import com.lee.shoppe.ui.screens.LoginScreen
import com.lee.shoppe.ui.screens.OnboardingScreen
import com.lee.shoppe.ui.screens.OrderDetailsScreen
import com.lee.shoppe.ui.screens.OrderInfoScreen
import com.lee.shoppe.ui.screens.PaymentSheetScreen
import com.lee.shoppe.ui.screens.ProductDetailsScreen
import com.lee.shoppe.ui.screens.ProductsScreen
import com.lee.shoppe.ui.screens.ProfileDetailsScreen
import com.lee.shoppe.ui.screens.ProfileScreen
import com.lee.shoppe.ui.screens.ReviewScreen
import com.lee.shoppe.ui.screens.TermsAndConditionsScreen
import com.lee.shoppe.ui.screens.SignupScreen
import com.lee.shoppe.ui.screens.StartScreen

@Composable
fun ECommerceNavHost(navController: NavHostController) {
    val context = LocalContext.current
    val sharedPrefs = remember { SharedPreferenceManager(context) }
    val onboardingShown = sharedPrefs.retrieve(SharedPreferenceManager.Key.ONBOARDING_SHOWN, "false") == "true"
    val isLoggedIn = sharedPrefs.retrieve(SharedPreferenceManager.Key.IS_LOGGED_IN, "false") == "true"

    val startDestination = when {
        !onboardingShown -> Screen.Onboarding.route
        isLoggedIn -> Screen.Home.route
        else -> Screen.Start.route
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val bottomBarRoutes = listOf(
        Screen.Home.route,
        Screen.Category.route,
        Screen.Cart.route,
        Screen.Favorite.route,
        Screen.Profile.route
    )
    val navItems = listOf(
        BottomNavItem("Home", Icons.Filled.Home, Screen.Home.route),
        BottomNavItem("Category", Icons.AutoMirrored.Filled.List, Screen.Category.route),
        BottomNavItem("Cart", Icons.Filled.ShoppingCart, Screen.Cart.route),
        BottomNavItem("Favorite", Icons.Filled.Favorite, Screen.Favorite.route),
        BottomNavItem("Profile", Icons.Filled.Person, Screen.Profile.route)
    )

    Scaffold(
        bottomBar = {
            if (currentRoute in bottomBarRoutes) {
                NavigationBar(
                    containerColor = Color.White ,
                    modifier = Modifier.height(65.dp)
                ) {
                    navItems.forEach { item ->
                        NavigationBarItem(
                            selected = currentRoute == item.route,
                            onClick = {
                                if (currentRoute != item.route) {
                                    navController.navigate(item.route) {
                                        popUpTo(Screen.Home.route)
                                        launchSingleTop = true
                                    }
                                }
                            },
                            icon = {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.label,
                                        tint = if (currentRoute == item.route) Color.Black else Color(0xFF004CFF),
                                        modifier = Modifier.size(34.dp)
                                    )
                                    if (currentRoute == item.route) {
                                        Box(
                                            Modifier
                                                .padding(top = 2.dp)
                                                .height(3.dp)
                                                .width(24.dp)
                                                .background(Color.Black, shape = RoundedCornerShape(2.dp))
                                        )
                                    } else {
                                        Spacer(modifier = Modifier.height(5.dp))
                                    }
                                }
                            },
                            alwaysShowLabel = false,
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = Color.Transparent
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(navController, startDestination = startDestination, modifier = Modifier.padding(innerPadding)) {
            composable(Screen.Onboarding.route) {
                OnboardingScreen(onFinish = {
                    sharedPrefs.save(SharedPreferenceManager.Key.ONBOARDING_SHOWN, "true")
                    navController.navigate(Screen.Start.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                })
            }
            composable(Screen.Start.route) {
                StartScreen(
                    onStartClick = {
                        navController.navigate(Screen.Signup.route)
                    },
                    onSignInClick = {
                        navController.navigate(Screen.Login.route)
                    }
                )
            }
            composable(Screen.Login.route) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onSignup = {
                        navController.navigate(Screen.Signup.route)
                    },
                    onSkip = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Signup.route) {
                SignupScreen(
                    onCreateClick = {
                        navController.navigate(Screen.Login.route)
                    },
                    onSignInClick = {
                        navController.navigate(Screen.Login.route)
                    }
                )
            }
            composable(Screen.Home.route) {
                HomeScreen(navController)
            }
            composable(Screen.Category.route) {
                CategoryScreen(navController)
            }
            composable(Screen.Cart.route) {
                CartScreen(navController = navController, onCheckout = {
                    navController.navigate(Screen.ChooseAddress.route)
                })
            }
            composable("address_list") {
                AddressListScreen(navController)
            }
            composable(
                route = "add_edit_address?id={id}",
                arguments = listOf(navArgument("id") { type = NavType.LongType })
            ) { backStackEntry ->
                AddEditAddressScreen(navController, navBackStackEntry = backStackEntry)
            }
            composable(
                route = "add_edit_address",
                arguments = emptyList()
            ) { backStackEntry ->
                AddEditAddressScreen(navController, navBackStackEntry = backStackEntry)
            }
            composable(
                route = "map_picker?for={for}",
                arguments = listOf(navArgument("for") { type = NavType.StringType; defaultValue = "address1" })
            ) { backStackEntry ->
                val forField = backStackEntry.arguments?.getString("for") ?: "address1"
                MapPickerScreen(
                    onLocationPicked = { latLng ->
                        navController.previousBackStackEntry?.savedStateHandle?.set(
                            if (forField == "address2") "picked_location2" else "picked_location",
                            latLng
                        )
                        navController.popBackStack()
                    },
                    onCancel = { navController.popBackStack() }
                )
            }
            composable(Screen.Favorite.route) {
                FavoriteScreen(navController)
            }
            composable(Screen.Profile.route) {
                ProfileScreen(navController)
            }
            composable("about") {
                AboutScreen(navController)
            }
            composable("terms_and_conditions") {
                TermsAndConditionsScreen(navController)
            }
            composable(
                route = "products/{brandTitle}",
                arguments = listOf(navArgument("brandTitle") { type = NavType.StringType })
            ) { backStackEntry ->
                val brandTitle = backStackEntry.arguments?.getString("brandTitle")
                ProductsScreen(brandTitle = brandTitle, navController = navController)
            }
            composable(
                route = "product_details/{productId}",
                arguments = listOf(navArgument("productId") { type = NavType.LongType })
            ) { backStackEntry ->
                val productId = backStackEntry.arguments?.getLong("productId") ?: 0L
                ProductDetailsScreen(productId = productId, navController = navController)
            }
            composable("reviews") {
                ReviewScreen(onBack = { navController.popBackStack() })
            }
            composable("profile_details") {
                ProfileDetailsScreen(navController)
            }
            composable("orders") {
                OrdersScreen(
                    navController = navController,
                    userId = sharedPrefs.retrieve(SharedPreferenceManager.Key.ID, "0").toLong(),
                    onOrderClick = { order ->
                        navController.navigate("order_info/${order.id}")
                    },
                    onAddressEditClick = {
                        navController.navigate("address_list")
                    }
                )
            }
            composable(
                route = "order_info/{orderId}",
                arguments = listOf(navArgument("orderId") { type = NavType.LongType })
            ) { backStackEntry ->
                val orderId = backStackEntry.arguments?.getLong("orderId") ?: 0L
                OrderInfoScreen(navController = navController, orderId = orderId)
            }
            composable(Screen.ChooseAddress.route) {
                val context = LocalContext.current
                val customerId = CustomerData.getInstance(context).id
                ChooseAddressScreen(
                    customerId = customerId,
                    navController = navController
                )
            }
            composable(
                route = "order_details/{addressId}",
                arguments = listOf(navArgument("addressId") { type = NavType.LongType })
            ) { backStackEntry ->
                val addressId = backStackEntry.arguments?.getLong("addressId") ?: 0L
                OrderDetailsScreen(
                    addressId = addressId,
                    navController = navController
                )
            }
            composable(
                route = "payment_sheet?paymentUrl={paymentUrl}&discountValue={discountValue}",
                arguments = listOf(
                    navArgument("paymentUrl") { type = NavType.StringType },
                    navArgument("discountValue") { type = NavType.StringType; defaultValue = "0.0" }
                )
            ) { backStackEntry ->
                val encodedPaymentUrl = backStackEntry.arguments?.getString("paymentUrl") ?: ""
                val paymentUrl = URLDecoder.decode(encodedPaymentUrl, StandardCharsets.UTF_8.toString())
                val discountValue = backStackEntry.arguments?.getString("discountValue")?.toDoubleOrNull() ?: 0.0
                PaymentSheetScreen(
                    paymentUrl = paymentUrl,
                    discountValue = discountValue,
                    onDismiss = {
                        navController.popBackStack()
                    },
                    navController = navController
                )
            }

        }

    }
}



