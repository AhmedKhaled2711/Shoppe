package com.lee.shoppe.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.*
import com.lee.shoppe.ui.components.LoadingWithMessages
import com.lee.shoppe.ui.components.ScreenHeader
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lee.shoppe.data.model.Address
import com.lee.shoppe.ui.theme.BluePrimary
import com.lee.shoppe.ui.theme.HeaderColor
import com.lee.shoppe.ui.viewmodel.CartAddressViewModel
import com.lee.shoppe.data.network.networking.NetworkState
import androidx.navigation.NavController

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

    // Initialize with default address if available
    var selectedAddressId by remember { mutableStateOf<Long?>(null) }
    
    // Set default address when data loads
    LaunchedEffect(addressList) {
        if (selectedAddressId == null && addressList.isNotEmpty()) {
            val defaultAddress = addressList.firstOrNull { it.default }
            selectedAddressId = defaultAddress?.id ?: addressList.first().id
        }
    }

    // 🔁 Fetch data once when screen is composed
    LaunchedEffect(Unit) {
        viewModel.getAllcustomer(customerId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Header
        ScreenHeader(
            title = "Choose Address",
            onBackClick = { navController.popBackStack() },
            showBackButton = true
        )
        //Spacer(modifier = Modifier.height(8.dp))
        
        // Content
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when {
                isLoading -> {
                    LoadingWithMessages(
                        modifier = Modifier.fillMaxSize(),
                        mainMessage = "Loading Addresses",
                        secondaryMessage = "Please wait while we fetch your saved addresses...",
                        loadingIndicatorColor = BluePrimary,
                        spacing = 16.dp,
                        messageSpacing = 8.dp
                    )
                }

                error != null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = error,
                            color = Color.Red
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        items(addressList) { address ->
                            AddressItem(
                                address = address,
                                isSelected = selectedAddressId == address.id,
                                onSelect = {
                                    selectedAddressId = address.id
                                    // Update default address in backend
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
        
        // Bottom Button
        Button(
            onClick = {
                selectedAddressId?.let { id ->
                    navController.navigate("order_details/$id")
                }
            },
            enabled = selectedAddressId != null,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            shape = RoundedCornerShape(12.dp), // Change radius as needed
            colors = ButtonDefaults.buttonColors(
                containerColor = BluePrimary
            )
        ) {
            Text(
                "Continue to Payment",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        }
    }
}

@Composable
fun AddressItem(
    address: Address,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val backgroundColor = if (isSelected) Color(0xFFEEF6FF) else Color.White
    val borderColor = if (isSelected) BluePrimary else Color.LightGray
    val borderWidth = if (isSelected) 2.dp else 1.dp

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = BorderStroke(borderWidth, borderColor),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Radio button indicator
            Icon(
                imageVector = if (isSelected) Icons.Filled.CheckCircle else Icons.Outlined.Circle,
                contentDescription = if (isSelected) "Selected" else "Not selected",
                tint = if (isSelected) BluePrimary else Color.Gray,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Address content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = address.name ?: "Address",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = if (isSelected) BluePrimary else Color.Black
                    )
                    
                    // Default address badge
                    if (address.default) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = BluePrimary.copy(alpha = 0.1f)
                            ),
                            modifier = Modifier.padding(0.dp)
                        ) {
                            Text(
                                text = "Default",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = BluePrimary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = address.address1,
                    fontSize = 14.sp,
                    color = Color.Gray
                )

//                if (address.address2.isNotBlank()) {
//                    Text(
//                        text = address.address2.toString(),
//                        fontSize = 14.sp,
//                        color = Color.Gray
//                    )
//                }
                
                Spacer(modifier = Modifier.height(2.dp))
                
                Text(
                    text = "${address.city}, ${address.country}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                
                if (!address.phone.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Phone: ${address.phone}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}
