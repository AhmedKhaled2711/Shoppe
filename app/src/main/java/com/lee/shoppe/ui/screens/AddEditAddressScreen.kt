package com.lee.shoppe.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.lee.shoppe.ui.theme.BluePrimary
import com.lee.shoppe.ui.theme.HeaderColor
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import com.lee.shoppe.data.model.CustomerData
import com.lee.shoppe.data.model.Address
import com.lee.shoppe.ui.viewmodel.AddressViewModel
import com.lee.shoppe.data.network.networking.NetworkState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.LaunchedEffect

@Composable
fun AddEditAddressScreen(navController: NavController, viewModel: AddressViewModel = hiltViewModel(), navBackStackEntry: NavBackStackEntry? = null) {
    val context = LocalContext.current
    val customerId = CustomerData.getInstance(context).id
    val actionState = viewModel.actionState.collectAsState()
    val isEdit = navBackStackEntry?.arguments?.containsKey("id") == true
    val addressId = navBackStackEntry?.arguments?.getString("id")?.toLongOrNull()

    // For simplicity, not prefetching address for edit (would require more logic)
    val address1 = remember { mutableStateOf("") }
    val address2 = remember { mutableStateOf("") }
    val city = remember { mutableStateOf("") }
    val zip = remember { mutableStateOf("") }
    val country = remember { mutableStateOf("") }
    val name = remember { mutableStateOf("") }
    val phone = remember { mutableStateOf("") }
    val isDefault = remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Header (unchanged)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            androidx.compose.material3.Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color.Black,
                modifier = Modifier
                    .size(28.dp)
                    .clickable { navController.popBackStack() }
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = if (isEdit) "Edit Address" else "Add Address",
                color = Color.Black,
                fontSize = 22.sp,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        // Address Form
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = name.value,
                onValueChange = { name.value = it },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            )
            OutlinedTextField(
                value = phone.value,
                onValueChange = { phone.value = it },
                label = { Text("Phone") },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            )
            OutlinedTextField(
                value = address1.value,
                onValueChange = { address1.value = it },
                label = { Text("Address 1") },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            )
            OutlinedTextField(
                value = address2.value,
                onValueChange = { address2.value = it },
                label = { Text("Address 2") },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            )
            OutlinedTextField(
                value = city.value,
                onValueChange = { city.value = it },
                label = { Text("City") },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            )
            OutlinedTextField(
                value = zip.value,
                onValueChange = { zip.value = it },
                label = { Text("Zip Code") },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            )
            OutlinedTextField(
                value = country.value,
                onValueChange = { country.value = it },
                label = { Text("Country") },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    val address = Address(
                        address1 = address1.value,
                        address2 = address2.value,
                        city = city.value,
                        company = "",
                        country = country.value,
                        country_code = "",
                        country_name = country.value,
                        customer_id = customerId,
                        default = isDefault.value,
                        first_name = name.value,
                        id = addressId ?: 0L,
                        last_name = "",
                        name = name.value,
                        phone = phone.value,
                        province = "",
                        province_code = "",
                        zip = zip.value
                    )
                    if (isEdit && addressId != null) {
                        viewModel.editAddress(customerId, addressId, address)
                    } else {
                        viewModel.addAddress(customerId, address)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text(if (isEdit) "Save Changes" else "Save Address", color = Color.White, fontSize = 16.sp)
            }
            when (val state = actionState.value) {
                is NetworkState.Loading -> {
                    Spacer(modifier = Modifier.height(16.dp))
                    CircularProgressIndicator(color = BluePrimary)
                }
                is NetworkState.Failure -> {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Failed: ${state.error.message}", color = Color.Red)
                }
                is NetworkState.Success -> {
                    // On success, navigate back
                    LaunchedEffect(Unit) { navController.popBackStack() }
                }
                else -> {}
            }
        }
    }
} 