package com.lee.shoppe.ui.screens

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.google.android.gms.maps.model.LatLng
import com.lee.shoppe.data.model.Address
import com.lee.shoppe.data.model.CustomerData
import com.lee.shoppe.data.network.networking.NetworkState
import com.lee.shoppe.ui.theme.BluePrimary
import com.lee.shoppe.ui.viewmodel.AddressViewModel
import android.location.Geocoder
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditAddressScreen(navController: NavController, viewModel: AddressViewModel = hiltViewModel(), navBackStackEntry: NavBackStackEntry? = null) {
    val context = LocalContext.current
    val customerId = CustomerData.getInstance(context).id
    val actionState = viewModel.actionState.collectAsState()
    val isEdit = navBackStackEntry?.arguments?.containsKey("id") == true
    val addressId = navBackStackEntry?.arguments?.getString("id")?.toLongOrNull()

    // For simplicity, not prefetching address for edit (would require more logic)
    val address1 = rememberSaveable { mutableStateOf("") }
    val address2 = rememberSaveable { mutableStateOf("") }
    val city = rememberSaveable { mutableStateOf("") }
    val zip = rememberSaveable { mutableStateOf("") }
    val country = rememberSaveable { mutableStateOf("") }
    val name = rememberSaveable { mutableStateOf("") }
    val phone = rememberSaveable { mutableStateOf("") }
    val isDefault = rememberSaveable { mutableStateOf(false) }
    val addressesState = viewModel.addresses.collectAsState()

    // Fetch addresses if needed (for edit mode)
    LaunchedEffect(isEdit, addressId, addressesState.value) {
        if (isEdit && addressId != null) {
            val addresses = (addressesState.value as? NetworkState.Success<List<Address>>)?.data
            if (addresses == null) {
                // Fetch addresses if not loaded
                viewModel.fetchAddresses(customerId)
            }
        }
    }
    // Prefill fields if editing
    LaunchedEffect(isEdit, addressId, addressesState.value) {
        if (isEdit && addressId != null) {
            val addresses = (addressesState.value as? NetworkState.Success<List<Address>>)?.data
            val address = addresses?.find { it.id == addressId }
            if (address != null) {
                address1.value = address.address1
                address2.value = address.address2?.toString() ?: ""
                city.value = address.city
                zip.value = address.zip
                country.value = address.country
                name.value = address.name
                phone.value = address.phone
                isDefault.value = address.default
            }
        }
    }


    // Remove Place Picker launcher for address1 and address2

    val navBackStackEntry = navController.currentBackStackEntryAsState().value
    val pickedLocation = navBackStackEntry?.savedStateHandle?.get<LatLng>("picked_location")
    val pickedLocation2 = navBackStackEntry?.savedStateHandle?.get<LatLng>("picked_location2")
    LaunchedEffect(pickedLocation) {
        pickedLocation?.let {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(it.latitude, it.longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                address1.value = listOfNotNull(
                    address.thoroughfare,
                    address.subThoroughfare,
                    address.locality,
                    address.adminArea,
                    address.countryName
                ).joinToString(", ")
            } else {
                address1.value = "${it.latitude}, ${it.longitude}"
            }
            navController.currentBackStackEntry?.savedStateHandle?.remove<LatLng>("picked_location")
        }
    }
    LaunchedEffect(pickedLocation2) {
        pickedLocation2?.let {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(it.latitude, it.longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                address2.value = listOfNotNull(
                    address.thoroughfare,
                    address.subThoroughfare,
                    address.locality,
                    address.adminArea,
                    address.countryName
                ).joinToString(", ")
            } else {
                address2.value = "${it.latitude}, ${it.longitude}"
            }
            navController.currentBackStackEntry?.savedStateHandle?.remove<LatLng>("picked_location2")
        }
    }

    val isEditAddressLoading = isEdit && addressId != null && ((addressesState.value as? NetworkState.Success<List<Address>>)?.data?.find { it.id == addressId } == null)

    val customSelectionColors = TextSelectionColors(
        handleColor = BluePrimary,
        backgroundColor = BluePrimary.copy(alpha = 0.4f)
    )

    CompositionLocalProvider(LocalTextSelectionColors provides customSelectionColors) {
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
                    text = if (isEdit) "Edit Address" else "Add Address",
                    color = Color.Black,
                    fontSize = 22.sp,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            if (isEditAddressLoading) {
                // Show loading indicator while waiting for address data
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = BluePrimary)
                }
            } else {
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
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = BluePrimary,
                            cursorColor = BluePrimary,
                            focusedLabelColor = BluePrimary
                        )
                    )
                    OutlinedTextField(
                        value = phone.value,
                        onValueChange = { phone.value = it },
                        label = { Text("Phone") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = BluePrimary,
                            cursorColor = BluePrimary,
                            focusedLabelColor = BluePrimary
                        )
                    )
                    OutlinedTextField(
                        value = address1.value,
                        onValueChange = { address1.value = it },
                        label = { Text("Address 1") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = BluePrimary,
                            cursorColor = BluePrimary,
                            focusedLabelColor = BluePrimary
                        ),
                        trailingIcon = {
                            IconButton(onClick = {
                                navController.navigate("map_picker")
                            }) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = "Pick location from map",
                                    tint = BluePrimary
                                )
                            }
                        }
                    )
                    OutlinedTextField(
                        value = address2.value,
                        onValueChange = { address2.value = it },
                        label = { Text("Address 2") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = BluePrimary,
                            cursorColor = BluePrimary,
                            focusedLabelColor = BluePrimary
                        ),
                        trailingIcon = {
                            IconButton(onClick = {
                                navController.navigate("map_picker?for=address2")
                            }) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = "Pick location from map",
                                    tint = BluePrimary
                                )
                            }
                        }
                    )
                    OutlinedTextField(
                        value = city.value,
                        onValueChange = { city.value = it },
                        label = { Text("City") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = BluePrimary,
                            cursorColor = BluePrimary,
                            focusedLabelColor = BluePrimary
                        )
                    )
                    OutlinedTextField(
                        value = zip.value,
                        onValueChange = { zip.value = it },
                        label = { Text("Zip Code") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = BluePrimary,
                            cursorColor = BluePrimary,
                            focusedLabelColor = BluePrimary
                        )
                    )
                    OutlinedTextField(
                        value = country.value,
                        onValueChange = { country.value = it },
                        label = { Text("Country") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = BluePrimary,
                            cursorColor = BluePrimary,
                            focusedLabelColor = BluePrimary
                        )
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
    }
} 