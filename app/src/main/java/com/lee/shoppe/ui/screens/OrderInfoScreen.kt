package com.lee.shoppe.ui.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.fashionshop.Model.AddressOrder
import com.example.fashionshop.Model.LineItemBody
import com.example.fashionshop.Model.Order
import com.example.fashionshop.Model.OrderResponse
import com.lee.shoppe.data.model.CustomerData
import com.lee.shoppe.data.network.networking.NetworkState
import com.lee.shoppe.ui.components.ScreenHeader
import com.lee.shoppe.ui.theme.BlueLight
import com.lee.shoppe.ui.theme.BluePrimary
import com.lee.shoppe.ui.theme.HeaderColor
import com.lee.shoppe.ui.viewmodel.OrderInfoViewModel

@Composable
fun OrderInfoScreen(
    navController: NavController,
    orderId: Long,
    viewModel: OrderInfoViewModel = hiltViewModel()
) {
    val orderState by viewModel.order.collectAsState()

    LaunchedEffect(orderId) {
        viewModel.getOrder(orderId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Header
        ScreenHeader(
            title = "Order Details",
            onBackClick = { navController.popBackStack() },
            showBackButton = true
        )

        // Content
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (orderState) {
                is NetworkState.Loading -> LoadingView()
                is NetworkState.Success -> {
                    val response = (orderState as NetworkState.Success<OrderResponse>).data
                    if (response.order != null) {
                        //Log.d("Order" , response.order.toString() )
                        OrderContent(order = response.order)
                    } else {
                        ErrorView(
                            message = "Order not found",
                            onRetry = { viewModel.getOrder(orderId) }
                        )
                    }
                }
                is NetworkState.Failure -> ErrorView(
                    message = "Failed to load order details",
                    onRetry = { viewModel.getOrder(orderId) }
                )
                else -> {}
            }
        }
    }
}

@Composable
private fun OrderContent(order: Order) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 50.dp)
    ) {
        item { OrderDetailsSection(order) }
        
        item {
            // Order Items Header Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = BlueLight),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.ShoppingBag,
                        contentDescription = "Order Items",
                        tint = BluePrimary,
                        modifier = Modifier.size(24.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = "Order Items",
                        color = BluePrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.weight(1f)) // Pushes the Box to the end

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(BluePrimary.copy(alpha = 0.1f), shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = order.line_items?.size.toString(),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = HeaderColor
                        )
                    }
                }
            }

        }
        
        items(order.line_items ?: emptyList()) { item ->
            OrderItemRow(item)
        }
    }
}

@Composable
private fun OrderDetailsSection(order: Order) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF8F9FA)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Text(
                text = "Order Information",
                color = HeaderColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            // Contact Email
            OrderDetailRow(
                icon = Icons.Filled.Email,
                label = "Contact Email",
                value = order.email ?: order.contact_email ?: "N/A"
            )

            // Phone Number
            OrderDetailRow(
                icon = Icons.Filled.Phone,
                label = "Phone Number",
                value = order.phone ?: order.billing_address?.phone ?: "N/A"
            )

            // Payment Method
            OrderDetailRow(
                icon = Icons.Filled.ShoppingBag,
                label = "Payment Method",
                value = order.payment_gateway_names?.firstOrNull() ?: order.referring_site ?: "N/A"
            )

            // Address
            OrderDetailRow(
                icon = Icons.Filled.LocationOn,
                label = "Delivery Address",
                value = formatAddress(order.billing_address),
                isMultiline = true
            )

            Divider(color = Color.Gray.copy(alpha = 0.3f))

            // Total Price
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
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
                    text = "Total Amount",
                    color = HeaderColor,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = order.current_total_price ?: order.total_price ?: "0.00",
                        color = BluePrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = currencySymbol,
                        color = BluePrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun OrderDetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    isMultiline: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = if (isMultiline) Alignment.Top else Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = BluePrimary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                color = Color.Gray,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                color = Color.Black,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

private fun formatAddress(address: AddressOrder?): String {
    if (address == null) return "N/A"
    return listOfNotNull(
        address.address1,
        address.address2,
        //address.city,
        //address.province,
        //address.zip,
        //address.country
    ).joinToString("\n")
}

@Composable
private fun OrderItemRow(item: LineItemBody) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val property = item.properties.firstOrNull { it.name.contains("ProductImage") }
            val imageUrl = property?.name?.substringAfter("src=")?.substringBefore(")")
//            // Extract image URL from properties
//            val imageUrl = item.properties?.firstOrNull { it.name?.contains("ProductImage") == true }?.value
//                ?.substringAfter("src=")
//                ?.substringBefore(")")

            // Product Image
            if (!imageUrl.isNullOrEmpty()) {
                Card(
                    modifier = Modifier.size(80.dp),
                    shape = RoundedCornerShape(8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(imageUrl),
                        contentDescription = "Product image",
                        modifier = Modifier.fillMaxSize()
                            .background(Color.White)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
            }

            // Product Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title?.substringAfter("|")?.trim() ?: "Unknown Product",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Qty: ${item.quantity ?: 1}",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            val context = LocalContext.current
            val customerData = CustomerData.getInstance(context)
            val currencySymbol = when (val currency = customerData.currency) {
                "USD" -> "$"
                "EGY" -> "EGP"
                "EUR" -> "€"
                "GBP" -> "£"
                else -> currency
            }
            // Price
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "${item.price ?: "0.00"} $currencySymbol",
                    color = BluePrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
private fun LoadingView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    color = BluePrimary,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Loading order details...",
                    color = HeaderColor,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun ErrorView(message: String, onRetry: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.padding(32.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(32.dp)
            ) {
                // Error Icon
                Icon(
                    imageVector = Icons.Filled.ShoppingBag,
                    contentDescription = "Error",
                    tint = Color.Red,
                    modifier = Modifier.size(48.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Oops!",
                    color = HeaderColor,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = message,
                    color = Color.Gray,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = onRetry,
                    colors = androidx.compose.material.ButtonDefaults.buttonColors(
                        backgroundColor = BluePrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(48.dp)
                ) {
                    Text(
                        text = "Try Again",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}