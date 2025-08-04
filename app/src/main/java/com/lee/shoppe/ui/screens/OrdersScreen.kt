package com.lee.shoppe.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import com.lee.shoppe.ui.components.ScreenHeader
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.fashionshop.Model.Order
import com.lee.shoppe.R
import com.lee.shoppe.data.network.networking.NetworkState
import com.lee.shoppe.ui.components.LoadingWithMessages
import com.lee.shoppe.ui.screens.dialogBox.EmptyState
import com.lee.shoppe.ui.theme.BlueLight
import com.lee.shoppe.ui.theme.BluePrimary
import com.lee.shoppe.ui.theme.HeaderColor
import com.lee.shoppe.ui.viewmodel.OrdersViewModel

@Composable
fun OrdersScreen(
    navController: NavController,
    viewModel: OrdersViewModel = hiltViewModel(),
    userId: Long,
    onOrderClick: (Order) -> Unit,
    onAddressEditClick: () -> Unit,
    forceRefresh: Boolean = false
) {
    val ordersState by viewModel.orders.collectAsState()
    val lottieComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.order_empty))

    // Trigger data fetch on first composition
    LaunchedEffect(forceRefresh) {
        viewModel.getOrders(userId, forceRefresh)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {

        // Header
        ScreenHeader(
            title = "My Orders",
            onBackClick = { navController.popBackStack() },
            showBackButton = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        when (ordersState) {
            is NetworkState.Loading -> {
                LoadingWithMessages(
                    modifier = Modifier.fillMaxSize(),
                    mainMessage = "Loading Your Orders",
                    secondaryMessage = "Please wait while we fetch your order history...",
                    loadingIndicatorColor = BluePrimary,
                    spacing = 16.dp,
                    messageSpacing = 8.dp
                )
            }

            is NetworkState.Success -> {
                val orderResponse = (ordersState as NetworkState.Success).data
                val orders = orderResponse.orders
                if (orders.isNullOrEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No orders found.", color = Color.Gray)
                    }
                    EmptyState(
                        lottieComposition,
                        "No Orders Yet",
                        "You haven’t placed any orders. Start shopping and track them here!"
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(orders.size) { index ->
                            OrderCard(
                                order = orders[index],
                                onClick = { onOrderClick(orders[index]) }
                            )
                        }
                    }
                }
            }

            is NetworkState.Failure -> {
                val error = (ordersState as NetworkState.Failure).error.message ?: "Unknown error"
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Failed to load orders:\n$error", color = Color.Red)
                }
            }

            NetworkState.Idle -> TODO()
        }
    }
}


@Composable
fun OrderCard(order: Order, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F7F7)),
        modifier = Modifier.fillMaxWidth().padding(start = 16.dp , end = 16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Order #${order.number}", fontWeight = FontWeight.Bold, color = BluePrimary)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Date: ${order.created_at?.substringBefore("T")}", fontSize = 14.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Payment Method: ${order.referring_site}", fontSize = 14.sp, color = Color.Gray)
        }
    }
}
