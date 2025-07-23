package com.lee.shoppe.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lee.shoppe.data.model.Address
import com.lee.shoppe.ui.viewmodel.CartAddressViewModel
import com.lee.shoppe.data.network.networking.NetworkState
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChooseAddressScreen(
    customerId: Long,
    navController: NavController,
    viewModel: CartAddressViewModel = hiltViewModel(),
) {
    val products by viewModel.products.collectAsState()
    val isLoading = products is NetworkState.Loading
    val error = (products as? NetworkState.Failure)?.error?.localizedMessage
    val addressList = (products as? NetworkState.Success)?.data?.customer?.addresses ?: emptyList()

    var selectedAddressId by remember { mutableStateOf<Long?>(null) }

    // 🔁 Fetch data once when screen is composed
    LaunchedEffect(Unit) {
        viewModel.getAllcustomer(customerId)
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Choose Address") })
        },
        bottomBar = {
            Button(
                onClick = {
                    selectedAddressId?.let { id ->
                        navController.navigate("order_details?addressId=$id")
                    }
                },
                enabled = selectedAddressId != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("Continue to Payment")
            }
        }
    ) { padding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(padding)) {
            when {
                isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                error != null -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = error,
                            color = Color.Red,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }

                else -> {
                    LazyColumn(modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)) {
                        items(addressList) { address ->
                            AddressItem(
                                address = address,
                                isSelected = selectedAddressId == address.id,
                                onSelect = {
                                    selectedAddressId = address.id
                                    viewModel.sendeditAddressRequest(
                                        id = address.id,
                                        default = true,
                                        customerId = customerId
                                    )
                                }
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddressItem(
    address: Address,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val backgroundColor = if (address.default || isSelected) Color(0xFFEEF6FF) else Color.White
    val borderColor = if (address.default || isSelected) Color(0xFF2196F3) else Color.LightGray

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = BorderStroke(1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = address.country ?: "Unknown Country",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = address.address1)
            Text(text = address.address2?.toString() ?: "")
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Phone: ${address.phone ?: "N/A"}")
        }
    }
}
