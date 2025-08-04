package com.lee.shoppe.ui.screens

import android.util.Log
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
import com.lee.shoppe.ui.components.ScreenHeader
import com.lee.shoppe.ui.navigation.Screen
import com.lee.shoppe.ui.theme.BluePrimary
import com.lee.shoppe.ui.theme.HeaderColor
import com.lee.shoppe.ui.utils.PaymentConstants
import com.lee.shoppe.ui.viewmodel.CartAddressViewModel
import com.lee.shoppe.ui.viewmodel.CartViewModel
import com.lee.shoppe.ui.viewmodel.OrderDetailsViewModel
import com.lee.shoppe.ui.viewmodel.PaymentViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

// Payment method constants
private const val PAYMENT_METHOD_VISA = "Visa"
private const val PAYMENT_METHOD_CASH = "Cash"

@OptIn(ExperimentalMaterial3Api::class)
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


    // State variables with improved naming and organization
    var couponText by remember { mutableStateOf("") }
    var discountPercent by remember { mutableStateOf(0.0) }
    var isCouponValid by remember { mutableStateOf(false) }
    var showPaymentDialog by remember { mutableStateOf(false) }
    var selectedPaymentMethod by remember { mutableStateOf("") }
    var paymentUrl by remember { mutableStateOf("") }
    var discountValueBody by remember { mutableStateOf("") }
    
    // Derived states for better performance
    val validateButtonColor = if (isCouponValid) Color(0xFF4CAF50) else BluePrimary
    
    // String resources
    val validateButtonText = if (isCouponValid) {
        stringResource(R.string.valid)
    } else {
        stringResource(R.string.validate)
    }

    // Load data on entry
    LaunchedEffect(customerData.cartListId) {
        if (customerData.cartListId > 0) cartViewModel.getCartProducts(customerData.cartListId)
        addressViewModel.getCustomerData(customerData.id , forceRefresh = true)
        orderDetailsViewModel.getAdsCode()
    }

    // Calculate cart values with null safety
    val lineItems = (cartState as? NetworkState.Success)?.data?.draft_order?.line_items?.drop(1) ?: emptyList()
    val subtotal = lineItems.sumOf { (it.price?.toDoubleOrNull() ?: 0.0) * (it.quantity ?: 1) }
    val currency = "EGP" // Always use EGP as the currency code
    val discountAmount = (subtotal * (discountPercent / 100)).coerceAtLeast(0.0)
    val total = (subtotal - discountAmount).coerceAtLeast(0.0)
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
                if (method == PAYMENT_METHOD_VISA) {
                    // Show loading state before API call
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Preparing secure payment...")
                    }
                    // Trigger Visa payment
                    paymentViewModel.getPaymentProducts(
                        successUrl = PaymentConstants.SUCCESS_URL,
                        cancelUrl = PaymentConstants.CANCEL_URL,
                        customerEmail = customerData.email,
                        currency = currency,
                        productName = "Your Order #${System.currentTimeMillis().toString().takeLast(6)}",
                        productDescription = "Complete your purchase",
                        unitAmountDecimal = (total * 100).toInt(),
                        quantity = 1,
                        mode = "payment",
                        paymentMethodType = "card"
                    )
                } else if (method == PAYMENT_METHOD_CASH) {
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
                            // Show success screen
                            navController.navigate(Screen.OrderSuccess.route) {
                                popUpTo(Screen.Home.route) { inclusive = true }
                                launchSingleTop = true
                            }
                        },
                        onError = { error ->
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Error placing order: $error")
                            }
                        }
                    )
                }
            }
        )
    }

    // Success screen is now handled via navigation

    // Main content with improved visual hierarchy and spacing
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // Header with elevation and back button
        Surface(
            tonalElevation = 3.dp,
            shadowElevation = 1.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            ScreenHeader(
                title = "Order Summary",
                onBackClick = { navController.popBackStack() },
                showBackButton = true
            )
        }

        // Scrollable content with proper padding and spacing
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                        ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 2.dp,
                            pressedElevation = 1.dp
                        ),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Apply Coupon",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                // Coupon Input Field
                                OutlinedTextField(
                                    value = couponText,
                                    onValueChange = { couponText = it },
                                    label = { 
                                        Text(
                                            "Enter coupon code",
                                            style = MaterialTheme.typography.bodyMedium
                                        ) 
                                    },
                                    singleLine = true,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(56.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = BluePrimary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                        focusedLabelColor = BluePrimary,
                                        cursorColor = BluePrimary,
                                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    textStyle = MaterialTheme.typography.bodyLarge,
                                    enabled = !isCouponValid
                                )
                                
                                Spacer(modifier = Modifier.width(12.dp))
                                
                                // Apply/Valid Button
                                Button(
                                    onClick = {
                                        val codes = (discountCodesState as? NetworkState.Success)?.data?.price_rules ?: emptyList()
                                        val found = codes.find { it.title.equals(couponText, ignoreCase = true) }
                                        
                                        if (found != null) {
                                            // Valid coupon found
                                            discountPercent = found.value.toDoubleOrNull() ?: 0.0
                                            isCouponValid = true
                                            validateButtonColor = Color(0xFF4CAF50)
                                            discountValueBody = (subtotal * (discountPercent / 100)).toString()
                                            
                                            // Show success message
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar("Coupon applied successfully!")
                                            }
                                        } else {
                                            // Invalid coupon
                                            discountPercent = 0.0
                                            isCouponValid = false
                                            validateButtonColor = BluePrimary
                                            discountValueBody = "0"
                                            
                                            // Show error message
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar("Invalid coupon code")
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .height(56.dp)
                                        .width(120.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isCouponValid) Color(0xFF4CAF50) else BluePrimary,
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    enabled = couponText.isNotEmpty() && !isCouponValid || isCouponValid
                                ) {
                                    if (isCouponValid) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.CheckCircle,
                                                contentDescription = "Valid",
                                                tint = Color.White,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                "Applied",
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    } else {
                                        Text(
                                            "Apply",
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    // Order Summary Card with improved visual hierarchy
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                        ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 2.dp,
                            pressedElevation = 1.dp
                        ),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Order Summary",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            
                            Divider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            
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

        // Sticky Checkout Button with improved visual feedback
        Spacer(modifier = Modifier.height(16.dp))
        
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Order Total Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Total",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$currency ${String.format("%.2f", total)}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = BluePrimary
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Checkout Button with loading state
                Button(
                    onClick = { showPaymentDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BluePrimary,
                        contentColor = Color.White,
                        disabledContainerColor = BluePrimary.copy(alpha = 0.5f)
                    ),
                    enabled = selectedAddress != null && lineItems.isNotEmpty()
                ) {
                    if (paymentState is NetworkState.Loading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Proceed to Payment",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                }
                
                // Helper text for disabled state
                if (selectedAddress == null) {
                    Text(
                        text = "Please select a shipping address",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .align(Alignment.CenterHorizontally)
                    )
                } else if (lineItems.isEmpty()) {
                    Text(
                        text = "Your cart is empty",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .align(Alignment.CenterHorizontally)
                    )
                }
            }
        }

        // Bottom spacing for safe area
        Spacer(modifier = Modifier.height(16.dp))
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
                onClick = { onPaymentSelected(PAYMENT_METHOD_VISA) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Cash Payment Option
            PaymentOptionCard(
                icon = Icons.Filled.Money,
                title = "Cash on Delivery",
                subtitle = "Pay with cash when your order arrives",
                onClick = { onPaymentSelected(PAYMENT_METHOD_CASH) }
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
    // Helper function for consistent price display
    @Composable
    fun PriceRow(
        label: String,
        value: String,
        isHighlighted: Boolean = false,
        showDiscountBadge: Boolean = false
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Label with optional discount badge
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isHighlighted) MaterialTheme.colorScheme.onSurface 
                           else MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                if (showDiscountBadge && discountPercent > 0) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .background(
                                color = Color(0xFFFFF8E1),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "-${discountPercent.toInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFFF8F00),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            // Price value
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Normal
                ),
                color = if (isHighlighted) MaterialTheme.colorScheme.primary 
                       else MaterialTheme.colorScheme.onSurface
            )
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Subtotal
        PriceRow(
            label = stringResource(R.string.subtotal),
            value = String.format("$currency %.2f", subtotal)
        )

        // Discount (only show if there is a discount)
        if (discountPercent > 0) {
            PriceRow(
                label = stringResource(R.string.discount),
                value = "-${String.format("$currency %.2f", subtotal * (discountPercent / 100))}",
                showDiscountBadge = true
            )
        }

        // Divider with padding
        Spacer(modifier = Modifier.height(4.dp))
        Divider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
            thickness = 0.5.dp,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        // Total
        PriceRow(
            label = stringResource(R.string.total),
            value = String.format("$currency %.2f", total),
            isHighlighted = true
        )
        
        // Estimated delivery (informational text)
        Text(
            text = "* ${stringResource(R.string.estimated_delivery)}: 3-5 ${stringResource(R.string.business_days)}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp)
        )
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

    // Ensure discount value is not negative
    val safeDiscountValue = try {
        val discount = discountValue.toDoubleOrNull() ?: 0.0
        maxOf(0.0, discount).toString()
    } catch (e: NumberFormatException) {
        "0.0"
    }

    val orderBody = mapOf(
        "order" to OrderBody(
            billing_address = addressBody,
            customer = customer,
            line_items = lineItem,
            total_tax = 13.5,
            currency = "EGP",
            total_discounts = safeDiscountValue,
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