package com.lee.shoppe.ui.screens

import android.location.Geocoder
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.icons.Icons
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
import androidx.compose.runtime.getValue
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
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.lee.shoppe.data.model.Address
import com.lee.shoppe.data.model.CustomerData
import com.lee.shoppe.data.network.networking.NetworkState
import com.lee.shoppe.ui.components.ScreenHeader
import com.lee.shoppe.ui.theme.BluePrimary
import com.lee.shoppe.ui.viewmodel.AddressViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditAddressScreen(navController: NavController, viewModel: AddressViewModel = hiltViewModel(), navBackStackEntry: NavBackStackEntry? = null) {
    val context = LocalContext.current
    val customerId = CustomerData.getInstance(context).id
    val navBackStackEntry = navController.currentBackStackEntry
    val isEdit = navBackStackEntry?.arguments?.containsKey("id") == true
    val addressId = navBackStackEntry?.arguments?.getLong("id", -1L).takeIf { it != -1L }

    // State for form fields with better null handling
    val address1 = rememberSaveable { mutableStateOf("") }
    val address2 = rememberSaveable { mutableStateOf("") }
    val city = rememberSaveable { mutableStateOf("") }
    val zip = rememberSaveable { mutableStateOf("") }
    val country = rememberSaveable { mutableStateOf("") }
    val name = rememberSaveable { mutableStateOf("") }
    val phone = rememberSaveable { mutableStateOf("") }
    val isDefault = rememberSaveable { mutableStateOf(false) }
    
    // Collect single address state
    val addressState by viewModel.singleAddress.collectAsState()
    
    // Track loading state
    val isLoading = remember { mutableStateOf(true) }
    
    // Handle navigation after save
    val actionState by viewModel.actionState.collectAsState()
    LaunchedEffect(actionState) {
        when (actionState) {
            is NetworkState.Success -> {
                // Navigate back to AddressListScreen when save is successful
                navController.popBackStack()
            }
            is NetworkState.Failure -> {
                // Handle error if needed
                Log.e("AddEditAddressScreen", "Error saving address")
            }
            else -> {}
        }
    }

    // Load address data when in edit mode
    LaunchedEffect(isEdit, addressId) {
        Log.d("AddEditAddressScreen", "LaunchedEffect triggered. isEdit: $isEdit, addressId: $addressId")
        if (isEdit && addressId != null) {
            Log.d("AddEditAddressScreen", "Fetching address with ID: $addressId")
            isLoading.value = true
            viewModel.fetchAddress(customerId, addressId)
        } else {
            Log.d("AddEditAddressScreen", "Not in edit mode or no address ID")
            isLoading.value = false
        }
    }

    // Handle address data when it's loaded
    LaunchedEffect(addressState) {
        Log.d("AddEditAddressScreen", "addressState changed: $addressState")
        if (isEdit && addressId != null) {
            when (val state = addressState) {
                is NetworkState.Success -> {
                    val address = state.data
                    Log.d("AddEditAddressScreen", "Address loaded successfully: $address")
                    address1.value = address.address1 ?: ""
                    address2.value = address.address2 ?: ""
                    city.value = address.city ?: ""
                    zip.value = address.zip ?: ""
                    country.value = address.country ?: ""
                    name.value = address.name ?: ""
                    phone.value = address.phone ?: ""
                    isDefault.value = address.default ?: false
                    isLoading.value = false
                }
                is NetworkState.Loading -> {
                    Log.d("AddEditAddressScreen", "Loading address...")
                    isLoading.value = true
                }
                is NetworkState.Failure -> {
                    Log.e("AddEditAddressScreen", "Error loading address", state.error)
                    // Handle error
                    isLoading.value = false
                    // You might want to show an error message to the user here
                }
                is NetworkState.Idle -> {
                    Log.d("AddEditAddressScreen", "Address state is Idle")
                }
                else -> {
                    Log.d("AddEditAddressScreen", "Unexpected state: $state")
                }
            }
        }
    }

    // Remove Place Picker launcher for address1 and address2

    val navBackStackEntryMapVar = navController.currentBackStackEntryAsState().value
    val pickedLocation = navBackStackEntryMapVar?.savedStateHandle?.get<LatLng>("picked_location")
    val pickedLocation2 = navBackStackEntryMapVar?.savedStateHandle?.get<LatLng>("picked_location2")
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

    //val isEditAddressLoading = isEdit && addressId != null && ((addressesState.value as? NetworkState.Success<List<Address>>)?.data?.find { it.id == addressId } == null)

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
            // Header
            ScreenHeader(
                title = if (isEdit) "Edit Address" else "Add Address",
                onBackClick = { navController.popBackStack() }
            )
            Spacer(modifier = Modifier.height(16.dp))
            if (isLoading.value) {
                // Show loading indicator while waiting for address data
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = BluePrimary)
                }
            } else {
                // Address Form
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .weight(1f),
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
                }
            }
        }
    }
} 