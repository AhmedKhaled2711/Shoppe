package com.lee.shoppe.ui.navigation

import MapPickerScreen
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.lee.shoppe.data.model.BottomNavItem
import com.lee.shoppe.data.model.CustomerData
import com.lee.shoppe.data.model.DraftOrderResponse
import com.lee.shoppe.data.network.caching.SharedPreferenceManager
import com.lee.shoppe.data.network.networking.NetworkState
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
import com.lee.shoppe.ui.screens.OrdersScreen
import com.lee.shoppe.ui.screens.PaymentSheetScreen
import com.lee.shoppe.ui.screens.ProductDetailsScreen
import com.lee.shoppe.ui.screens.ProductsScreen
import com.lee.shoppe.ui.screens.ProfileDetailsScreen
import com.lee.shoppe.ui.screens.ProfileScreen
import com.lee.shoppe.ui.screens.ReviewScreen
import com.lee.shoppe.ui.screens.SignupScreen
import com.lee.shoppe.ui.screens.StartScreen
import com.lee.shoppe.ui.screens.TermsAndConditionsScreen
import com.lee.shoppe.ui.theme.BlueLight
import com.lee.shoppe.ui.theme.BluePrimary
import com.lee.shoppe.ui.theme.BlueSecondary
import com.lee.shoppe.ui.viewmodel.CartViewModel
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

private fun handleNavigation(
    navController: NavHostController,
    currentRoute: String?,
    targetRoute: String
) {
    if (currentRoute != targetRoute) {
        navController.navigate(targetRoute) {
            popUpTo(Screen.Home.route) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ECommerceNavHost(
    navController: NavHostController,
    cartViewModel: CartViewModel = hiltViewModel()
) {
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
        BottomNavItem("Cart", Icons.Filled.ShoppingCart, Screen.Cart.route, showBadge = true),
        BottomNavItem("Favorite", Icons.Filled.Favorite, Screen.Favorite.route),
        BottomNavItem("Profile", Icons.Filled.Person, Screen.Profile.route)
    )

    Scaffold(
        bottomBar = {
            if (currentRoute in bottomBarRoutes) {
                // Bottom Navigation Bar with FAB
                Box(
                    modifier = Modifier
                        .height(70.dp)
                        .background(Color.White)
                ) {
                    // Bottom bar content
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        // Navigation items (4 items total - we'll handle the FAB separately)
                        val navBarItems = listOf(
                            navItems[0], // Home
                            navItems[1], // Category
                            navItems[3], // Favorite
                            navItems[4]  // Profile
                        )
                        
                        navBarItems.forEachIndexed { index, item ->
                            val isSelected = currentRoute == item.route
                            val iconTint = if (isSelected) Color(0xFF004CFF) else Color(0xFF9E9E9E)
                            
                            // Add extra space for the FAB in the middle
                            if (index == 2) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                            
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clickable {
                                        handleNavigation(navController, currentRoute, item.route)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.label,
                                        tint = iconTint,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    if (isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .size(4.dp)
                                                .background(Color(0xFF004CFF), shape = CircleShape)
                                        )
                                    } else {
                                        Spacer(modifier = Modifier.height(4.dp))
                                    }
                                }
                            }
                        }
                    }
                    
                    // FAB for Cart in the center with badge
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .offset(y = (-16).dp)
                    ) {
                        val isCartSelected = currentRoute == navItems[2].route
                        val cartProductsState by cartViewModel.cartProducts.collectAsState()
                        val customerData = CustomerData.getInstance(context)
                        val cartListId = customerData.cartListId
                        
                        // Load cart products if list ID is valid and user is logged in or has guest data
                        LaunchedEffect(cartListId, customerData.isLogged, customerData.isGuestWithPreservedData) {
                            if ((customerData.isLogged || customerData.isGuestWithPreservedData) && cartListId > 0) {
                                cartViewModel.getCartProducts(cartListId)
                            }
                        }
                        
                        // Calculate cart item count - this will automatically update when cartProductsState changes
                        val cartItemCount = when (cartProductsState) {
                            is NetworkState.Success -> {
                                val cartData = (cartProductsState as NetworkState.Success<DraftOrderResponse>).data
                                cartData.draft_order.line_items.count {
                                    it.title != "dummy" && it.product_id != null && it.sku != null
                                }
                            }
                            is NetworkState.Failure -> 0
                            is NetworkState.Loading -> 0
                            is NetworkState.Idle -> 0
                        }
                        
                        // Force recomposition when cart state changes
                        LaunchedEffect(cartProductsState) {
                            // This will trigger a recomposition whenever cartProductsState changes
                            // The cart badge will update when products are added or removed
                        }
                        
                        // Cart FAB with Badge
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clickable {
                                    handleNavigation(navController, currentRoute, navItems[2].route)
                                }
                        ) {
                            // FAB Background
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .background(
                                        color = if (isCartSelected) BluePrimary else BlueSecondary,
                                        shape = CircleShape
                                    )
                            ) {
                                // Cart Icon
                                Icon(
                                    imageVector = Icons.Filled.ShoppingCart,
                                    contentDescription = "Cart",
                                    tint = Color.White,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .align(Alignment.Center)
                                )

                                // Badge
                                if (cartItemCount > 0) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .offset(x = 4.dp, y = (-4).dp)
                                            .size(20.dp)
                                            .background(Color.Red, CircleShape)
                                            .padding(2.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (cartItemCount > 9) "9+" else "$cartItemCount",
                                            color = Color.White,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 10.sp,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.wrapContentSize()
                                        )
                                    }
                                }
                            }
                        }
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
                CartScreen(
                    navController = navController,
                    cartViewModel = cartViewModel,
                    onCheckout = {
                        navController.navigate(Screen.ChooseAddress.route)
                    }
                )
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
            composable("orders?forceRefresh={forceRefresh}",
                arguments = listOf(navArgument("forceRefresh") { 
                    type = NavType.BoolType
                    defaultValue = false 
                })
            ) { backStackEntry ->
                val forceRefresh = backStackEntry.arguments?.getBoolean("forceRefresh") ?: false
                OrdersScreen(
                    navController = navController,
                    userId = sharedPrefs.retrieve(SharedPreferenceManager.Key.ID, "0").toLong(),
                    onOrderClick = { order ->
                        navController.navigate("order_info/${order.id}")
                    },
                    onAddressEditClick = {
                        navController.navigate("address_list")
                    },
                    forceRefresh = forceRefresh
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



