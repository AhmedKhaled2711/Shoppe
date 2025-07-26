package com.lee.shoppe.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
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
import com.example.fashionshop.Model.Order
import com.lee.shoppe.data.network.networking.NetworkState
import com.lee.shoppe.ui.theme.BlueLight
import com.lee.shoppe.ui.theme.BluePrimary
import com.lee.shoppe.ui.theme.HeaderColor
import com.lee.shoppe.ui.viewmodel.OrdersViewModel

@Composable
fun OrdersScreen(
    navController: NavController,
    viewModel: OrdersViewModel =  hiltViewModel(),
    userId: Long,
    onOrderClick: (Order) -> Unit,
    onAddressEditClick: () -> Unit
) {
    val ordersState by viewModel.orders.collectAsState()

    // Trigger data fetch on first composition
    LaunchedEffect(Unit) {
        viewModel.getOrders(userId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {

        // Header - consistent with other screens
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 0.dp, vertical = 0.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                modifier = Modifier
                    .size(28.dp)
                    .clickable { navController.popBackStack() }
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "My Orders",
                color = HeaderColor,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        when (ordersState) {
            is NetworkState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = BluePrimary)
                }
            }

            is NetworkState.Success -> {
                val orderResponse = (ordersState as NetworkState.Success).data
                val orders = orderResponse.orders
                if (orders.isNullOrEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No orders found.", color = Color.Gray)
                    }
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
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Order #${order.order_number}", fontWeight = FontWeight.Bold, color = BluePrimary)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Date: ${order.created_at}", fontSize = 14.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Status: ${order.financial_status}", fontSize = 14.sp, color = Color.Gray)
        }
    }
}
