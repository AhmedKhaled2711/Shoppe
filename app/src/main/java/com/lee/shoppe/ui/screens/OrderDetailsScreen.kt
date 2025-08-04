package com.lee.shoppe.ui.screens

import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Money
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.fashionshop.Model.AddressBody
import com.example.fashionshop.Model.CustomerBody
import com.example.fashionshop.Model.DefaultAddressBody
import com.example.fashionshop.Model.LineItemBody
import com.example.fashionshop.Model.OrderBody
import com.lee.shoppe.R
import com.lee.shoppe.data.model.Address
import com.lee.shoppe.data.model.CheckoutSessionResponse
import com.lee.shoppe.data.model.CustomerData
import com.lee.shoppe.data.model.DraftOrderResponse
import com.lee.shoppe.data.network.networking.NetworkState
import com.lee.shoppe.ui.components.OrderSuccessScreen
import com.lee.shoppe.ui.components.ScreenHeader
import com.lee.shoppe.ui.navigation.Screen
import com.lee.shoppe.ui.theme.BluePrimary
import com.lee.shoppe.ui.theme.HeaderColor
import com.lee.shoppe.ui.viewmodel.CartAddressViewModel
import com.lee.shoppe.ui.viewmodel.CartViewModel
import com.lee.shoppe.ui.viewmodel.OrderDetailsViewModel
import com.lee.shoppe.ui.viewmodel.PaymentViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun OrderDetailsScreen(
    addressId: Long,
    navController: NavController,
    cartViewModel: CartViewModel = hiltViewModel(),
    orderDetailsViewModel: OrderDetailsViewModel = hiltViewModel(),
    addressViewModel: CartAddressViewModel = hiltViewModel(),
    paymentViewModel: PaymentViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val customerData = remember { CustomerData.getInstance(context) }
    val cartState by cartViewModel.cartProducts.collectAsState()
    val addressState by addressViewModel.products.collectAsState()
    val discountCodesState by orderDetailsViewModel.productCode.collectAsState()
    val paymentState by paymentViewModel.productPayment.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Animation state
    var showSuccessAnimation by remember { mutableStateOf(false) }
    val animationProgress by animateFloatAsState(
        targetValue = if (showSuccessAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1000),
        label = "successAnimation"
    )

    // State variables
    var couponText by remember { mutableStateOf("") }
    var discountPercent by remember { mutableStateOf(0.0) }
    var isCouponValid by remember { mutableStateOf(false) }
    var validateButtonColor by remember { mutableStateOf(Color.Gray) }
    var validateButtonText by remember { mutableStateOf((R.string.validate)) }
    var showPaymentDialog by remember { mutableStateOf(false) }
    var selectedPaymentMethod by remember { mutableStateOf("") }
    var showPaymentSheet by remember { mutableStateOf(false) }
    var paymentUrl by remember { mutableStateOf("") }
    var discountValueBody by remember { mutableStateOf("") }
    var showSuccessScreen by remember { mutableStateOf(false) }

    // Load data on entry
    LaunchedEffect(customerData.cartListId) {
        if (customerData.cartListId > 0) cartViewModel.getCartProducts(customerData.cartListId)
        addressViewModel.getCustomerData(customerData.id , forceRefresh = true)
        orderDetailsViewModel.getAdsCode()
    }

    // Calculate cart values
    val lineItems = (cartState as? NetworkState.Success)?.data?.draft_order?.line_items?.drop(1) ?: emptyList()
    val subtotal = lineItems.sumOf { (it.price?.toDoubleOrNull() ?: 0.0) * (it.quantity ?: 1) }
    // Always use EGP as the currency code
    val currency = "EGP"
    val discountAmount = subtotal * (discountPercent / 100)
    val total = subtotal - discountAmount
    val selectedAddress = (addressState as? NetworkState.Success)?.data?.customer?.addresses?.find { it.id == addressId }

    // Handle payment state changes
    LaunchedEffect(paymentState) {
        when (paymentState) {
            is NetworkState.Success -> {
                paymentUrl = (paymentState as NetworkState.Success<CheckoutSessionResponse>).data.url
                // Navigate to PaymentSheetScreen with properly encoded URL
                val encodedUrl = URLEncoder.encode(paymentUrl, StandardCharsets.UTF_8.toString())
                navController.navigate("payment_sheet?paymentUrl=${encodedUrl}&discountValue=${discountAmount}")
            }
            is NetworkState.Failure -> {
                // Show error message
                Log.e("OrderDetailsScreen", "Payment failed: ")
            }
            else -> {}
        }
    }


    // Payment method dialog
    if (showPaymentDialog) {
        PaymentMethodBottomSheet(
            onDismiss = { showPaymentDialog = false },
            onPaymentSelected = { method ->
                selectedPaymentMethod = method
                showPaymentDialog = false
                if (method == "Visa") {
                    // Trigger Visa payment
                    paymentViewModel.getPaymentProducts(
                        successUrl = "https://example.com/success",
                        cancelUrl = "https://example.com/cancel",
                        customerEmail = customerData.email,
                        currency = currency,
                        productName = "Your Order",
                        productDescription = "Please Write your Card Information",
                        unitAmountDecimal = (total * 100).toInt(),
                        quantity = 1,
                        mode = "payment",
                        paymentMethodType = "card"
                    )
                } else if (method == "Cash") {
                    showSuccessAnimation = true
                    placeOrder(
                        coroutineScope = coroutineScope,
                        orderDetailsViewModel = orderDetailsViewModel,
                        cartViewModel = cartViewModel,
                        customerData = customerData,
                        selectedAddress = selectedAddress,
                        lineItems = lineItems,
                        discountValue = discountAmount.toString(),
                        paymentMethod = method,
                        currency = currency,
                        onSuccess = {
                            // The success animation is already shown, let it handle navigation
                        },
                        onError = { errorMsg ->
                            showSuccessAnimation = false
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Error: $errorMsg")
                            }
                        }
                    )
                }
            }
        )
    }

    // Success animation overlay
    if (showSuccessAnimation) {
        OrderSuccessScreen(
            onTimeout = {
                // Navigate to home after showing success message
                navController.navigate(Screen.Home.route) {
                    // Pop everything up to and including the home screen
                    popUpTo(0) { inclusive = true }
                    // Prevent going back to the order screen
                    launchSingleTop = true
                }
            },
            message = "Your order has been placed successfully!\nThank you for your purchase.",
            timeoutMillis = 2500L
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Header
        ScreenHeader(
            title = stringResource(R.string.order_details),
            onBackClick = { navController.popBackStack() },
            showBackButton = true
        )

        // Content
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (cartState is NetworkState.Loading || addressState is NetworkState.Loading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = BluePrimary)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Address Card
                    selectedAddress?.let { address ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFF8F9FA)
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "Delivery Address",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = HeaderColor
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = address.name ?: "Address",
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "${address.address1}, ${address.city}, ${address.country}",
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                                if (!address.phone.isNullOrBlank()) {
                                    Text(
                                        text = "Phone: ${address.phone}",
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Coupon Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFF8F9FA)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Apply Coupon",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = HeaderColor
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedTextField(
                                    value = couponText,
                                    onValueChange = { couponText = it },
                                    label = { Text("Enter coupon code") },
                                    modifier = Modifier.weight(2f),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = BluePrimary,
                                        focusedLabelColor = BluePrimary
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = {
                                        val codes = (discountCodesState as? NetworkState.Success)?.data?.price_rules ?: emptyList()
                                        val found = codes.find { it.title == couponText }
                                        if (found != null) {
                                            discountPercent = found.value.toDoubleOrNull() ?: 0.0
                                            isCouponValid = true
                                            validateButtonColor = Color(0xFF4CAF50)
                                            discountValueBody = (subtotal * (discountPercent / 100)).toString()
                                        } else {
                                            discountPercent = 0.0
                                            isCouponValid = false
                                            validateButtonColor = Color.Gray
                                            discountValueBody = "0"
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(56.dp), // Match OutlinedTextField height
                                    colors = ButtonDefaults.buttonColors(containerColor = validateButtonColor),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    if (isCouponValid) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Check,
                                                contentDescription = "Valid",
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                "Valid",
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    } else {
                                        Text(
                                            "Apply",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    // Order Summary Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFF8F9FA)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Order Summary",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = HeaderColor
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            OrderSummarySection(
                                subtotal = subtotal,
                                discountPercent = discountPercent,
                                total = total,
                                currency = currency
                            )
                        }
                    }
                }
            }
        }

        // Bottom Button
        Button(
            onClick = {
                showPaymentDialog = true
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
            enabled = !showSuccessAnimation
        ) {
            Text(
                if (showSuccessAnimation) "Processing..." else stringResource(R.string.place_order),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }

    // Show success screen after order placement
    if (showSuccessScreen) {
        OrderSuccessScreen(
            onTimeout = {
                // Navigate to home after showing success message
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Cart.route) { inclusive = true }
                }
            },
            message = "Your order has been placed successfully!\nYou can track your order in the Orders section.",
            timeoutMillis = 2500L
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PaymentMethodBottomSheet(
    onDismiss: () -> Unit,
    onPaymentSelected: (String) -> Unit
) {
    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = bottomSheetState,
        containerColor = Color.White,
        contentColor = Color.Black,
        dragHandle = {
            // Custom drag handle
            Box(
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .background(
                        Color.Gray.copy(alpha = 0.3f),
                        RoundedCornerShape(2.dp)
                    )
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Header
            Text(
                text = "Choose Payment Method",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = HeaderColor,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            // Visa Payment Option
            PaymentOptionCard(
                icon = Icons.Filled.CreditCard,
                title = "Visa Card",
                subtitle = "Pay securely with your Visa card",
                onClick = { onPaymentSelected("Visa") }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Cash Payment Option
            PaymentOptionCard(
                icon = Icons.Filled.Money,
                title = "Cash on Delivery",
                subtitle = "Pay with cash when your order arrives",
                onClick = { onPaymentSelected("Cash") }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Cancel Button
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.Gray
                ),
                border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Cancel",
                    fontWeight = FontWeight.Medium
                )
            }

            // Bottom spacing for safe area
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun PaymentOptionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.2f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Payment Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        BluePrimary.copy(alpha = 0.1f),
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = BluePrimary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Payment Details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            // Arrow or selection indicator
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Select",
                tint = Color.Gray,
                modifier = Modifier
                    .size(20.dp)
                    .rotate(180f) // Make it point right
            )
        }
    }
}

@Composable
private fun OrderSummarySection(
    subtotal: Double,
    discountPercent: Double,
    total: Double,
    currency: String
) {
    // Subtotal
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(stringResource(R.string.subtotal), fontSize = 20.sp, fontWeight = FontWeight.Bold )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(String.format("%.2f", subtotal), fontSize = 20.sp, fontWeight = FontWeight.Bold , color = BluePrimary)
            Spacer(modifier = Modifier.width(4.dp))
            Text(currency, fontSize = 18.sp , fontWeight = FontWeight.Bold , color = BluePrimary)
        }
    }
    Spacer(modifier = Modifier.height(8.dp))

    // Discount
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(stringResource(R.string.discount), fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text("${discountPercent}%", fontSize = 20.sp, fontWeight = FontWeight.Bold , color = BluePrimary)
    }
    Divider(modifier = Modifier.padding(vertical = 8.dp))

    // Total
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(stringResource(R.string.total), fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(String.format("%.2f", total), fontSize = 20.sp, fontWeight = FontWeight.Bold , color = BluePrimary)
            Spacer(modifier = Modifier.width(4.dp))
            Text(currency, fontSize = 18.sp ,fontWeight = FontWeight.Bold, color = BluePrimary)
        }
    }
}

fun placeOrder(
    orderDetailsViewModel: OrderDetailsViewModel,
    cartViewModel: CartViewModel,
    customerData: CustomerData,
    selectedAddress: Address?,
    lineItems: List<DraftOrderResponse.DraftOrder.LineItem>,
    discountValue: String,
    paymentMethod: String,
    currency: String,
    onSuccess: () -> Unit,
    onError: (String) -> Unit,
    coroutineScope: CoroutineScope
) {
    if (selectedAddress == null || lineItems.isEmpty()) {
        onError("Missing address or cart items.")
        return
    }

    val addressBody = AddressBody(
        first_name = selectedAddress.first_name,
        address1 = selectedAddress.address1,
        phone = selectedAddress.phone,
        city = selectedAddress.city,
        zip = selectedAddress.zip,
        country = selectedAddress.country,
        last_name = selectedAddress.last_name,
        name = selectedAddress.name,
        country_code = selectedAddress.country_code,
    )

    val defaultAddress = DefaultAddressBody(
        first_name = selectedAddress.first_name,
        address1 = selectedAddress.address1,
        phone = selectedAddress.phone,
        city = selectedAddress.city,
        zip = selectedAddress.zip,
        country = selectedAddress.country,
        last_name = selectedAddress.last_name,
        name = selectedAddress.name,
        country_code = selectedAddress.country_code,
        default = selectedAddress.default // Use the default value from the selected address
    )

    // Split the full name into first and last names
    val nameParts = customerData.name.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }
    val firstName = nameParts.firstOrNull() ?: ""
    val lastName = nameParts.drop(1).takeIf { it.isNotEmpty() }?.joinToString(" ") ?: "Customer"
    
    val customer = CustomerBody(
        id = customerData.id,
        email = customerData.email,
        first_name = firstName,
        last_name = lastName,
        currency = "EGP",
        default_address = defaultAddress
    )

    val lineItem = lineItems.map { lineItem ->
        LineItemBody(
            variant_id = lineItem.variant_id,
            quantity = lineItem.quantity,
            id = lineItem.id,
            title = lineItem.title,
            price = lineItem.price,
            sku = lineItem.sku,
            properties = lineItem.properties.map {
                LineItemBody.Property(name = it.name, value = it.value)
            }
        )
    }

    val orderBody = mapOf(
        "order" to OrderBody(
            billing_address = addressBody,
            customer = customer,
            line_items = lineItem,
            total_tax = 13.5,
            currency = "EGP", // Force EGP as the currency code
            total_discounts = discountValue,
            referring_site = paymentMethod
        )
    )

    orderDetailsViewModel.createOrder(
        orderBody = orderBody,
        onSuccess = {
            // Clear the cart first
            cartViewModel.clearCart(customerData.cartListId)
            
            // Launch a coroutine in the screen's scope
            coroutineScope.launch {
                try {
                    // Force refresh customer data
                    val refreshedCustomer = orderDetailsViewModel.getSingleCustomer(customerData.id, true)
                    // Update local customer data with refreshed data if successful
                    refreshedCustomer?.let { customerData.updateFromCustomer(it) }
                } catch (e: Exception) {
                    // Log the error but don't fail the order placement
                    Log.e("OrderDetailsScreen", "Error refreshing customer data: ${e.message}")
                } finally {
                    // Always call onSuccess to proceed with the order success flow
                    onSuccess()
                }
            }
        },
        onError = { errorMsg ->
            onError(errorMsg)
        }
    )
    

}