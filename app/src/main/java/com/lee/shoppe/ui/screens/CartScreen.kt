package com.lee.shoppe.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.lee.shoppe.R
import com.lee.shoppe.data.model.CustomerData
import com.lee.shoppe.data.model.DraftOrderResponse
import com.lee.shoppe.data.network.networking.NetworkState
import com.lee.shoppe.ui.components.LoadingWithMessages
import com.lee.shoppe.ui.theme.*
import com.lee.shoppe.ui.viewmodel.CartViewModel
import coil.compose.AsyncImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import android.util.Log
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.window.Dialog
import androidx.compose.material.icons.filled.Error
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import com.airbnb.lottie.LottieComposition
import kotlinx.coroutines.flow.count

@Composable
fun CartScreen(
    navController: NavController,
    cartViewModel: CartViewModel = hiltViewModel(),
    onCheckout: () -> Unit = {}
) {
    val context = LocalContext.current
    val cartProductsState by cartViewModel.cartProducts.collectAsState()
    val customerData = CustomerData.getInstance(context)
    val lottieComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.cart_empty))
    val guestLottieComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.l))
    val networkLottieComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.boy))
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // State for confirmation dialogs
    var showDeleteAllDialog by remember { mutableStateOf(false) }
    var showDeleteItemDialog by remember { mutableStateOf(false) }
    var pendingDeleteProductId by remember { mutableStateOf<Long?>(null) }

    // Compute real cart item count for header
    val cartItemCount = when (cartProductsState) {
        is NetworkState.Success -> {
            val cartData = (cartProductsState as NetworkState.Success<DraftOrderResponse>).data
            cartData.draft_order.line_items.count {
                it.title != "dummy" && it.product_id != null && it.sku != null
            }
        }
        else -> 0
    }

    // Load cart products when screen is first composed
    LaunchedEffect(Unit) {
        val shouldLoadCart = (customerData.isLogged || customerData.isGuestWithPreservedData) && 
                           customerData.cartListId > 0
        
        if (shouldLoadCart) {
            Log.d("CartScreen", "Loading cart with ID: ${customerData.cartListId} (isGuestWithPreservedData: ${customerData.isGuestWithPreservedData})")
            cartViewModel.getCartProducts(customerData.cartListId)
        } else if (customerData.isLogged) {
            // Create cart list if user is logged in but doesn't have a cart list
            Log.d("CartScreen", "Creating new cart list for logged in user")
            cartViewModel.checkAndCreateCartListIfNeeded { newListId ->
                customerData.cartListId = newListId
                cartViewModel.getCartProducts(newListId)
            }
        } else {
            Log.d("CartScreen", "No cart to load - not logged in and no guest cart")
        }
    }

    // Show Snackbar for HTTP 429 or too many requests
    LaunchedEffect(cartProductsState) {
        if (cartProductsState is NetworkState.Failure) {
            val error = (cartProductsState as NetworkState.Failure).error
            val message = if (error.message?.contains("Too many requests") == true || (error is retrofit2.HttpException && error.code() == 429)) {
                "You're making requests too quickly. Please wait a moment and try again."
            } else {
                error.message ?: "An error occurred. Please try again."
            }
            coroutineScope.launch {
                snackbarHostState.showSnackbar(message)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Professional Header
            CartHeader(
                cartItemCount = cartItemCount,
                onDeleteAllClick = {
                    if (customerData.isLogged && customerData.cartListId > 0) {
                        showDeleteAllDialog = true
                    }
                }
            )
            // SnackbarHost for feedback
            SnackbarHost(hostState = snackbarHostState)
            // Main Content
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                when {
                    // Guest User State
                    !customerData.isLogged -> {
                        GuestUserState(
                            lottieComposition = guestLottieComposition,
                            onLoginClick = { navController.navigate("login") }
                        )
                    }
                    // Network Error State
                    cartProductsState is NetworkState.Failure -> {
                        NetworkErrorState(
                            lottieComposition = networkLottieComposition
                        )
                    }
                    // Loading State
                    cartProductsState is NetworkState.Loading -> {
                        LoadingState()
                    }
                    // Success State
                    cartProductsState is NetworkState.Success -> {
                        val cartData = (cartProductsState as NetworkState.Success<DraftOrderResponse>).data
                        val lineItems = cartData.draft_order.line_items
                        // Filter out dummy items (items with title "dummy" or null product_id)
                        val realLineItems = lineItems.filter { 
                            it.title != "dummy" && it.product_id != null && it.sku != null 
                        }
                        if (realLineItems.isEmpty()) {
                            // Empty cart state
                            EmptyCartState(lottieComposition = lottieComposition)
                        } else {
                            // Cart with items
                            CartContent(
                                lineItems = realLineItems,
                                onRemoveItem = { productId ->
                                    pendingDeleteProductId = productId
                                    showDeleteItemDialog = true
                                },
                                onUpdateQuantity = { productId, quantity ->
                                    if (customerData.isLogged && customerData.cartListId > 0) {
                                        cartViewModel.updateProductQuantity(productId, quantity, customerData.cartListId)
                                    }
                                },
                                snackbarHostState = snackbarHostState,
                                coroutineScope = coroutineScope,
                                navController = navController,
                                customerEmail = customerData.email,
                                currency = customerData.currency,
                                titlesList = realLineItems.map { it.title ?: "" },
                                onCheckout = onCheckout
                            )
                        }
                    }
                    else -> {
                        // Idle state
                        IdleState()
                    }
                }
            }
        }
        // Confirm delete all dialog
        DeleteCartDialog(
            show = showDeleteAllDialog,
            title = "You are going to clear your cart",
            subtitle = "You won't be able to restore your data",
            confirmText = "Clear All",
            onCancel = { showDeleteAllDialog = false },
            onConfirm = {
                showDeleteAllDialog = false
                if (customerData.isLogged && customerData.cartListId > 0) {
                    cartViewModel.clearCart(customerData.cartListId)
                }
            }
        )
        // Confirm delete item dialog
        DeleteCartDialog(
            show = showDeleteItemDialog && pendingDeleteProductId != null,
            title = "You are going to remove this product",
            subtitle = "You won't be able to restore this item",
            confirmText = "Remove",
            onCancel = { showDeleteItemDialog = false; pendingDeleteProductId = null },
            onConfirm = {
                showDeleteItemDialog = false
                val productId = pendingDeleteProductId
                if (productId != null && customerData.isLogged && customerData.cartListId > 0) {
                    cartViewModel.removeProductFromCart(productId, customerData.cartListId)
                }
                pendingDeleteProductId = null
            }
        )
    }
}

@Composable
fun CartHeader(
    cartItemCount: Int,
    onDeleteAllClick: () -> Unit
) {
    Surface(
        shadowElevation = 4.dp,
        color = Color.White,
        shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.width(8.dp))
            // Title and Quantity
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = stringResource(R.string.cart),
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = HeaderColor
                )
                Spacer(modifier = Modifier.width(8.dp))
                if (cartItemCount > 0) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(BlueLight, shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = cartItemCount.toString(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = HeaderColor
                        )
                    }
                }
            }
            // Show delete icon only when cart is not empty
            if (cartItemCount > 0) {
                IconButton(
                    onClick = onDeleteAllClick,
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.delete_24),
                        contentDescription = "Delete all",
                        tint = Color(0xFFE57373),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}


@Composable
fun GuestUserState(
    lottieComposition: LottieComposition?,
    onLoginClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (lottieComposition != null) {
            LottieAnimation(
                composition = lottieComposition,
                iterations = LottieConstants.IterateForever,
                modifier = Modifier.size(200.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.guest_profile),
            color = Dark,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = stringResource(R.string.guest_profile2),
            color = Color.Gray,
            fontSize = 16.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = onLoginClick,
            modifier = Modifier
                .width(200.dp)
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = BluePrimary
            ),
            shape = RoundedCornerShape(16)
        ) {
            Text(
                text = stringResource(R.string.login),
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun EmptyCartState(lottieComposition: LottieComposition?) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (lottieComposition != null) {
            LottieAnimation(
                composition = lottieComposition,
                iterations = LottieConstants.IterateForever,
                modifier = Modifier.size(200.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.cart),
            color = Dark,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Your cart is empty",
            color = Color.Gray,
            fontSize = 16.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun NetworkErrorState(lottieComposition: LottieComposition?) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (lottieComposition != null) {
            LottieAnimation(
                composition = lottieComposition,
                iterations = LottieConstants.IterateForever,
                modifier = Modifier.size(200.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.network_message_main),
            color = Dark,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = stringResource(R.string.network_message_first),
            color = Color.Gray,
            fontSize = 16.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = stringResource(R.string.network_message_second),
            color = Color.Gray,
            fontSize = 16.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun LoadingState() {
    LoadingWithMessages(
        modifier = Modifier.fillMaxSize(),
        mainMessage = "Loading Your Cart",
        secondaryMessage = "Please wait while we fetch your items...",
        loadingIndicatorColor = BluePrimary,
        spacing = 16.dp,
        messageSpacing = 8.dp
    )
}

@Composable
fun IdleState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No cart data available",
            color = Color.Gray,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun CartContent(
    lineItems: List<DraftOrderResponse.DraftOrder.LineItem>,
    onRemoveItem: (Long) -> Unit,
    onUpdateQuantity: (Long, Int) -> Unit,
    snackbarHostState: SnackbarHostState,
    coroutineScope: CoroutineScope,
    navController: NavController? = null, // Add navController for navigation
    customerEmail: String = "",
    currency: String = "",
    titlesList: List<String> = emptyList(),
    onCheckout: () -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Cart items
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(lineItems) { item ->
                CartItemCard(
                    item = item,
                    onRemove = { 
                        val productId = item.sku?.split("*")?.getOrNull(0)?.toLongOrNull()
                        Log.d("CartItemCard", "onRemove clicked, sku=${item.sku}, extracted productId=$productId, variant_id=${item.variant_id}, product_id=${item.product_id}")
                        if (productId == null) {
                            item.variant_id?.let { onRemoveItem(it) }
                        } else {
                            onRemoveItem(productId)
                        }
                    },
                    onUpdateQuantity = { quantity ->
                        val productId = item.sku?.split("*")?.getOrNull(0)?.toLongOrNull()
                        Log.d("CartItemCard", "onQuantityChange clicked, sku=${item.sku}, extracted productId=$productId, variant_id=${item.variant_id}, product_id=${item.product_id}, new quantity=$quantity")
                        if (productId == null) {
                            item.variant_id?.let { onUpdateQuantity(it, quantity) }
                        } else {
                            onUpdateQuantity(productId, quantity)
                        }
                    },
                    snackbarHostState = snackbarHostState,
                    coroutineScope = coroutineScope
                )
            }
        }
        // Cart summary and checkout
        CartSummaryBottom(
            lineItems = lineItems,
            onCheckout = onCheckout
        )
    }
}

@Composable
fun CartItemCard(
    item: DraftOrderResponse.DraftOrder.LineItem,
    onRemove: () -> Unit,
    onUpdateQuantity: (Int) -> Unit,
    snackbarHostState: SnackbarHostState,
    coroutineScope: CoroutineScope,
    maxQuantity: Int = Int.MAX_VALUE
) {
    val property = item.properties.firstOrNull { it.name.contains("ProductImage") }
    val imageUrl = property?.name?.substringAfter("src=")?.substringBefore(")")
    val quantity = item.quantity ?: 1
    val itemPrice = item.price?.toDoubleOrNull() ?: 0.0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Product image + delete icon
            Box(
                modifier = Modifier
                    .width(129.dp)
                    .height(109.dp),
                contentAlignment = Alignment.BottomStart
            ) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = item.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.LightGray)
                )
                // Delete icon
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier
                        .padding(6.dp)
                        .size(30.dp)
                        .background(Color.White, shape = CircleShape)
                        .border(1.dp, Color(0xFFE57373), CircleShape)
                        .align(Alignment.BottomStart),
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.delete_24),
                        contentDescription = "Remove item",
                        tint = Color(0xFFE57373),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(0.dp))

            // Product controls
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp, top = 2.dp),
                verticalArrangement = Arrangement.Top
            ) {
                // Product title at the top, aligned with image
                Text(
                    text = item.title
                        ?.split("|")
                        ?.getOrNull(1)
                        ?.trim()
                        ?: "Unknown Product",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = Color.Black,
                    textAlign = TextAlign.Start,
                    maxLines = 1,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))

                val context = LocalContext.current
                val customerData = CustomerData.getInstance(context)
                val currencySymbol = when (val currency = customerData.currency) {
                    "USD" -> "$"
                    "EGY" -> "EGP"
                    "EUR" -> "€"
                    "GBP" -> "£"
                    else -> currency
                }

                Text(
                    text = "${item.price ?: "0.00"} $currencySymbol",
                    color = BluePrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Price + Quantity controls
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    // − Decrease Button
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(2.2.dp, Color(0xFF004BFE), CircleShape)
                            .clickable(enabled = quantity > 1) {
                                if (quantity > 1) {
                                    onUpdateQuantity(quantity - 1)
                                } else {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Cannot decrease below 1 quantity")
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "−",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            color = Color(0xFF004BFE)
                        )
                    }

                    // Quantity Display
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(36.dp)
                            .background(BlueLight, RoundedCornerShape(6.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$quantity",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.Black
                        )
                    }

                    // + Increase Button
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(2.2.dp, Color(0xFF004BFE), CircleShape)
                            .clickable(enabled = quantity < maxQuantity) {
                                if (quantity < maxQuantity) {
                                    onUpdateQuantity(quantity + 1)
                                } else {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Cannot increase above $maxQuantity quantity")
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "+",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            color = Color(0xFF004BFE)
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun CartSummaryBottom(
    lineItems: List<DraftOrderResponse.DraftOrder.LineItem>,
    onCheckout: () -> Unit = {}
) {
    val total = lineItems.sumOf { (it.price?.toDoubleOrNull() ?: 0.0) * (it.quantity ?: 1) }
    val context = LocalContext.current
    val customerData = CustomerData.getInstance(context)
    val currencySymbol = when (val currency = customerData.currency) {
        "USD" -> "$"
        "EGY" -> "EGP"
        "EUR" -> "€"
        "GBP" -> "£"
        else -> currency
    }
    Surface(
        color = Color(0xFFF5F5F5),
        shadowElevation = 8.dp,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Total section
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Total ",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    color = Color.Black
                )
                Text(
                    text = String.format("%.2f ", total),
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = BluePrimary
                )
                Text(
                    text = currencySymbol,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color.Black
                )
            }
            // Checkout button with rounded corners
            Button(
                onClick = onCheckout,
                modifier = Modifier
                    .height(44.dp)
                    .width(140.dp),
                shape = RoundedCornerShape(12.dp), // Change radius as needed
                colors = ButtonDefaults.buttonColors(
                    containerColor = BluePrimary
                )
            ) {
                Text(
                    text = "Checkout",
                    color = Color(0xFFF3F3F3),
                    fontWeight = FontWeight.Medium,
                    fontSize = 18.sp
                )
            }

        }
    }
} 

@Composable
fun DeleteCartDialog(
    show: Boolean,
    title: String,
    subtitle: String,
    confirmText: String,
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    if (show) {
        Dialog(onDismissRequest = onCancel) {
            Box(
                contentAlignment = Alignment.TopCenter,
                modifier = Modifier.fillMaxWidth()
            ) {
                Card(
                    modifier = Modifier
                        .padding(top = 32.dp)
                        .fillMaxWidth()
                        .shadow(8.dp, RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(top = 40.dp, start = 24.dp, end = 24.dp, bottom = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.Black,
                            modifier = Modifier.padding(top = 8.dp),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = subtitle,
                            color = Color.Gray,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
                        ) {
                            Button(
                                onClick = onCancel,
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Cancel", color = Color.White)
                            }
                            Button(
                                onClick = onConfirm,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(confirmText, color = Color.White)
                            }
                        }
                    }
                }

                // Circular icon overlapping the card
                Box(
                    modifier = Modifier
                        .offset(y = (-1).dp)
                        .size(84.dp)
                        .background(Color.White, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(55.dp)
                            .background(Color(0xFFFFEBEB), shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            tint = Color(0xFFF1AEAE),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }
    }
} 