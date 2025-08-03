package com.lee.shoppe.ui.screens

import android.location.Geocoder
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.google.android.gms.maps.model.LatLng
import com.lee.shoppe.data.model.CustomerData
import com.lee.shoppe.data.network.networking.NetworkState
import com.lee.shoppe.ui.theme.BluePrimary
import com.lee.shoppe.ui.viewmodel.AddressViewModel
import java.util.Locale
import java.net.URLEncoder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditAddressScreen(
    navController: NavController, 
    viewModel: AddressViewModel = hiltViewModel(), 
    navBackStackEntry: NavBackStackEntry? = null
) {
    val context = LocalContext.current
    val customerId = CustomerData.getInstance(context).id
    val actionState = viewModel.actionState.collectAsState()
    
    // Get the address ID from the navigation arguments
    val addressId = navBackStackEntry?.arguments?.getLong("id", -1L).takeIf { it != -1L }
    val isEdit = addressId != null
    
    Log.d("AddEditAddressScreen", "Screen created - isEdit: $isEdit, addressId: $addressId")

    // Form fields with validation
    val name = rememberSaveable { mutableStateOf("") }
    val nameError = remember { mutableStateOf<String?>(null) }
    
    val phone = rememberSaveable { mutableStateOf("") }
    val phoneError = remember { mutableStateOf<String?>(null) }
    
    val address1 = rememberSaveable { mutableStateOf("") }
    val address1Error = remember { mutableStateOf<String?>(null) }
    
    val address2 = rememberSaveable { mutableStateOf("") }
    val address2Error = remember { mutableStateOf<String?>(null) }
    
    val city = rememberSaveable { mutableStateOf("") }
    val zip = rememberSaveable { mutableStateOf("") }
    val country = rememberSaveable { mutableStateOf("") }
    val isDefault = rememberSaveable { mutableStateOf(false) }
    
    // Track if form has been submitted to show validation errors
    val isFormSubmitted = remember { mutableStateOf(false) }
    
    // Validate form fields
    fun validateForm(): Boolean {
        var isValid = true
        
        // Reset errors
        nameError.value = null
        phoneError.value = null
        address1Error.value = null
        address2Error.value = null
        
        // Validate name
        if (name.value.isBlank()) {
            nameError.value = "Name is required"
            isValid = false
        }
        
        // Validate phone
        if (phone.value.isBlank()) {
            phoneError.value = "Phone number is required"
            isValid = false
        } else if (!android.util.Patterns.PHONE.matcher(phone.value).matches()) {
            phoneError.value = "Enter a valid phone number"
            isValid = false
        }
        
        // Validate address1
        if (address1.value.isBlank()) {
            address1Error.value = "Address line 1 is required"
            isValid = false
        }
        
        return isValid
    }
    
    // Save address function with validation
    fun saveAddress() {
        if (!validateForm()) {
            return
        }
        
        val address = com.lee.shoppe.data.model.Address(
            id = if (isEdit) addressId ?: 0L else 0L,
            customer_id = customerId,
            first_name = name.value.trim().split(" ").firstOrNull() ?: "",
            last_name = name.value.trim().split(" ").drop(1).joinToString(" "),
            name = name.value.trim(),
            company = "",
            address1 = address1.value.trim(),
            address2 = address2.value.trim(),
            city = city.value.trim(),
            province = "",
            country = country.value.trim(),
            zip = zip.value.trim(),
            phone = phone.value.trim(),
            default = isDefault.value,
            country_code = "",
            country_name = country.value.trim(),
            province_code = ""
        )
        
        if (isEdit && addressId != null) {
            viewModel.editAddress(customerId, addressId, address)
        } else {
            viewModel.addAddress(customerId, address)
        }
        
        // Navigate back to AddressListScreen with refresh flag
        navController.previousBackStackEntry?.savedStateHandle?.set("refresh", true)
        navController.navigate("address_list") {
            // Clear the back stack to prevent going back to the edit screen
            popUpTo("address_list") { inclusive = true }
            // Clear the back stack up to the address list
            launchSingleTop = true
        }
    }
    
    // Single address state for editing - collect as state with lifecycle-aware collection
    val singleAddressState by viewModel.singleAddress.collectAsState()
    
    // Track if we've handled the initial address load
    val hasHandledInitialLoad = remember { mutableStateOf(false) }
    
    // Fetch address data when in edit mode
    LaunchedEffect(isEdit, addressId) {
        Log.d("AddEditAddressScreen", "LaunchedEffect - isEdit: $isEdit, addressId: $addressId")
        
        // Always reset the state when these values change
        hasHandledInitialLoad.value = false
        
        if (isEdit && addressId != null) {
            Log.d("AddEditAddressScreen", "Fetching address $addressId for editing")
            viewModel.fetchAddress(customerId, addressId, forceRefresh = true)
        } else {
            // Reset form fields when adding a new address
            Log.d("AddEditAddressScreen", "Resetting form for new address")
            address1.value = ""
            address2.value = ""
            city.value = ""
            zip.value = ""
            country.value = ""
            name.value = ""
            phone.value = ""
            isDefault.value = false
            viewModel.resetSingleAddressState()
            hasHandledInitialLoad.value = true
        }
    }
    
    // Update form fields when single address is loaded
    LaunchedEffect(singleAddressState) {
        Log.d("AddEditAddressScreen", "Single address state changed: $singleAddressState")
        
        when (val state = singleAddressState) {
            is NetworkState.Success -> {
                if (!hasHandledInitialLoad.value) {
                    val address = state.data
                    Log.d("AddEditAddressScreen", "Address loaded successfully: $address")
                    
                    // Update all fields at once to avoid partial updates
                    name.value = address.name ?: ""
                    phone.value = address.phone ?: ""
                    address1.value = address.address1 ?: ""
                    address2.value = address.address2 ?: ""
                    city.value = address.city ?: ""
                    zip.value = address.zip ?: ""
                    country.value = address.country ?: ""
                    isDefault.value = address.default
                    
                    hasHandledInitialLoad.value = true
                    Log.d("AddEditAddressScreen", "Form fields updated with address data")
                }
            }
            is NetworkState.Loading -> {
                Log.d("AddEditAddressScreen", "Loading address data...")
            }
            is NetworkState.Failure -> {
                Log.e("AddEditAddressScreen", "Error loading address: ${state.error}", state.error)
                // Reset the error state after handling
                viewModel.resetSingleAddressState()
            }
            is NetworkState.Idle -> {
                Log.d("AddEditAddressScreen", "Idle state - no address loaded yet")
            }
        }
    }

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

    val isEditAddressLoading = isEdit && addressId != null && singleAddressState is NetworkState.Loading

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
                    // Name field (always visible and editable)
                    OutlinedTextField(
                        value = name.value,
                        onValueChange = { 
                            name.value = it
                            if (isFormSubmitted.value) {
                                nameError.value = if (it.isBlank()) "Name is required" else null
                            }
                        },
                        label = { Text("Full Name") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = if (nameError.value != null) Color.Red else BluePrimary,
                            cursorColor = BluePrimary,
                            focusedLabelColor = if (nameError.value != null) Color.Red else BluePrimary
                        ),
                        isError = nameError.value != null,
                        supportingText = {
                            if (nameError.value != null) {
                                Text(nameError.value!!, color = Color.Red)
                            } else null
                        },
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Phone field
                    OutlinedTextField(
                        value = phone.value,
                        onValueChange = { 
                            phone.value = it
                            if (isFormSubmitted.value) {
                                phoneError.value = when {
                                    it.isBlank() -> "Phone number is required"
                                    !android.util.Patterns.PHONE.matcher(it).matches() -> "Enter a valid phone number"
                                    else -> null
                                }
                            }
                        },
                        label = { Text("Phone Number") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = if (phoneError.value != null) Color.Red else BluePrimary,
                            cursorColor = BluePrimary,
                            focusedLabelColor = if (phoneError.value != null) Color.Red else BluePrimary
                        ),
                        isError = phoneError.value != null,
                        supportingText = {
                            if (phoneError.value != null) {
                                Text(phoneError.value!!, color = Color.Red)
                            } else null
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )
                    // Address 1 field (editable with optional map selection)
                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        OutlinedTextField(
                            value = address1.value,
                            onValueChange = { 
                                address1.value = it
                                if (isFormSubmitted.value) {
                                    address1Error.value = if (it.isBlank()) "Address line 1 is required" else null
                                }
                            },
                            label = { Text("Address Line 1") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedBorderColor = if (address1Error.value != null) Color.Red else BluePrimary,
                                cursorColor = BluePrimary,
                                focusedLabelColor = if (address1Error.value != null) Color.Red else BluePrimary
                            ),
                            isError = address1Error.value != null,
                            supportingText = {
                                if (address1Error.value != null) {
                                    Text(address1Error.value!!, color = Color.Red)
                                } else null
                            },
                            singleLine = true,
                            trailingIcon = {
                                IconButton(
                                    onClick = { 
                                        // Save form state before navigation
                                        navController.currentBackStackEntry?.savedStateHandle?.set("saved_name", name.value)
                                        navController.currentBackStackEntry?.savedStateHandle?.set("saved_phone", phone.value)
                                        navController.currentBackStackEntry?.savedStateHandle?.set("saved_address1", address1.value)
                                        navController.currentBackStackEntry?.savedStateHandle?.set("saved_address2", address2.value)
                                        navController.currentBackStackEntry?.savedStateHandle?.set("saved_city", city.value)
                                        navController.currentBackStackEntry?.savedStateHandle?.set("saved_country", country.value)
                                        navController.currentBackStackEntry?.savedStateHandle?.set("saved_zip", zip.value)                                        // Pass current address to map picker
                                        val currentAddress = "${address1.value}, ${city.value}, ${country.value} ${zip.value}".trim()
                                        navController.navigate("map_picker?for=address1&address=${URLEncoder.encode(currentAddress, "UTF-8")}")
                                    },
                                    modifier = Modifier
                                        .padding(end = 8.dp)
                                        .background(
                                            color = BluePrimary.copy(alpha = 0.1f),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .size(40.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = "Pick location from map",
                                        tint = BluePrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        )
                    }
                    
                    // Address 2 field (editable with optional map selection)
                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        OutlinedTextField(
                            value = address2.value,
                            onValueChange = { 
                                address2.value = it
                                if (isFormSubmitted.value) {
                                    address2Error.value = if (it.isBlank()) "Address line 2 is required" else null
                                }
                            },
                            label = { Text("Address Line 2") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedBorderColor = if (address2Error.value != null) Color.Red else BluePrimary,
                                cursorColor = BluePrimary,
                                focusedLabelColor = if (address2Error.value != null) Color.Red else BluePrimary
                            ),
                            isError = address2Error.value != null,
                            supportingText = {
                                if (address2Error.value != null) {
                                    Text(address2Error.value!!, color = Color.Red)
                                } else null
                            },
                            singleLine = true,
                            trailingIcon = {
                                IconButton(
                                    onClick = { 
                                        // Save form state before navigation
                                        navController.currentBackStackEntry?.savedStateHandle?.set("saved_name", name.value)
                                        navController.currentBackStackEntry?.savedStateHandle?.set("saved_phone", phone.value)
                                        navController.currentBackStackEntry?.savedStateHandle?.set("saved_address1", address1.value)
                                        navController.currentBackStackEntry?.savedStateHandle?.set("saved_address2", address2.value)
                                        navController.currentBackStackEntry?.savedStateHandle?.set("saved_city", city.value)
                                        navController.currentBackStackEntry?.savedStateHandle?.set("saved_country", country.value)
                                        navController.currentBackStackEntry?.savedStateHandle?.set("saved_zip", zip.value)                                        // Pass current address to map picker
                                        val currentAddress = "${address2.value}, ${city.value}, ${country.value} ${zip.value}".trim()
                                        navController.navigate("map_picker?for=address2&address=${URLEncoder.encode(currentAddress, "UTF-8")}")
                                    },
                                    modifier = Modifier
                                        .padding(end = 8.dp)
                                        .background(
                                            color = BluePrimary.copy(alpha = 0.1f),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .size(40.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = "Pick location from map",
                                        tint = BluePrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        )
                    }
                    
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
                    
                    // Save button - enabled only when all required fields are filled
                    val isFormValid = name.value.isNotBlank() && 
                                    phone.value.isNotBlank() && 
                                    address1.value.isNotBlank()
                    
                    Button(
                        onClick = { 
                            isFormSubmitted.value = true
                            if (validateForm()) {
                                saveAddress()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isFormValid) BluePrimary else Color.LightGray,
                            contentColor = Color.White
                        ),
                        enabled = isFormValid
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
                            // On success, set refresh flag and navigate back
                            LaunchedEffect(Unit) {
                                navController.previousBackStackEntry?.savedStateHandle?.set("refresh", true)
                                navController.popBackStack()
                            }
                        }
                        else -> {}
                    }
                }
            }
        }
    }

    // Restore form state after returning from map picker
    LaunchedEffect(Unit) {
        // Restore saved form fields
        navController.currentBackStackEntry?.savedStateHandle?.get<String>("saved_name")?.let { name.value = it }
        navController.currentBackStackEntry?.savedStateHandle?.get<String>("saved_phone")?.let { phone.value = it }
        navController.currentBackStackEntry?.savedStateHandle?.get<String>("saved_address1")?.let { address1.value = it }
        navController.currentBackStackEntry?.savedStateHandle?.get<String>("saved_address2")?.let { address2.value = it }
        navController.currentBackStackEntry?.savedStateHandle?.get<String>("saved_city")?.let { city.value = it }
        navController.currentBackStackEntry?.savedStateHandle?.get<String>("saved_country")?.let { country.value = it }
        navController.currentBackStackEntry?.savedStateHandle?.get<String>("saved_zip")?.let { zip.value = it }

        // Handle map selection result
        navController.currentBackStackEntry?.savedStateHandle?.get<Pair<String, String>>("selected_address")?.let { (addressType, address) ->
            when (addressType) {
                "address1" -> {
                    if (address.isNotBlank()) {
                        address1.value = address
                    }
                    address1Error.value = null
                }
                "address2" -> {
                    if (address.isNotBlank()) {
                        address2.value = address
                    }
                    address2Error.value = null
                }
            }
            // Clear the result to prevent handling it multiple times
            navController.currentBackStackEntry?.savedStateHandle?.remove<Pair<String, String>>("selected_address")
        }

        // Clear saved state after restoring
        listOf(
            "saved_name", "saved_phone", "saved_address1", "saved_address2",
            "saved_city", "saved_country", "saved_zip"
        ).forEach { key ->
            navController.currentBackStackEntry?.savedStateHandle?.remove<String>(key)
        }
    }
} 