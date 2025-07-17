package com.lee.shoppe.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.lee.shoppe.data.model.CustomerData
import com.lee.shoppe.data.network.networking.NetworkState
import com.lee.shoppe.ui.theme.BlueLight
import com.lee.shoppe.ui.theme.BluePrimary
import com.lee.shoppe.ui.theme.HeaderColor
import com.lee.shoppe.ui.viewmodel.AddressViewModel

@Composable
fun AddressListScreen(navController: NavController, viewModel: AddressViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val customerId = CustomerData.getInstance(context).id
    val addressesState = viewModel.addresses.collectAsState()
    val actionState = viewModel.actionState.collectAsState()

    LaunchedEffect(customerId) {
        viewModel.fetchAddresses(customerId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color.Black,
                modifier = Modifier
                    .size(28.dp)
                    .clickable { navController.popBackStack() }
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "My Addresses",
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                modifier = Modifier.weight(1f)
            )
            // Add Address Button
            Button(
                onClick = { navController.navigate("add_edit_address") },
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Text("Add", color = Color.White, fontSize = 15.sp)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        // Content
        when (val state = addressesState.value) {
            is NetworkState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = BluePrimary)
                }
            }
            is NetworkState.Failure -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Failed to load addresses", color = Color.Red)
                }
            }
            is NetworkState.Success -> {
                val addresses = state.data
                if (addresses.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No addresses found.", color = Color.Gray)
                    }
                } else {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        addresses.forEachIndexed { idx, address ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(BlueLight)
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(address.address1, fontWeight = FontWeight.Bold, color = HeaderColor)
                                        Text(address.name, color = Color.Gray, fontSize = 14.sp)
                                        Text(address.phone, color = Color.Gray, fontSize = 14.sp)
                                    }
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Default",
                                        tint = if (address.default) BluePrimary else Color.Gray,
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clickable { viewModel.setDefaultAddress(customerId, address.id) }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit",
                                        tint = BluePrimary,
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clickable { navController.navigate("add_edit_address?id=${address.id}") }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = Color.Red,
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clickable { viewModel.deleteAddress(customerId, address.id) }
                                    )
                                }
                            }
                            Divider(color = Color(0xFFE0E0E0), thickness = 1.dp)
                        }
                    }
                }
            }
            else -> {}
        }
    }
} 