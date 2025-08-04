package com.lee.shoppe.ui.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.lee.shoppe.R
import com.lee.shoppe.data.model.Address
import com.lee.shoppe.data.model.CustomerData
import com.lee.shoppe.data.network.networking.NetworkState
import com.lee.shoppe.ui.components.ScreenHeader
import com.lee.shoppe.ui.theme.BlueLight
import com.lee.shoppe.ui.theme.BluePrimary
import com.lee.shoppe.ui.theme.BlueSecondary
import com.lee.shoppe.ui.theme.HeaderColor
import com.lee.shoppe.ui.theme.RedAccent
import com.lee.shoppe.ui.viewmodel.AddressViewModel

@Composable
fun AddressListScreen(navController: NavController, viewModel: AddressViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val customerId = CustomerData.getInstance(context).id
    val addressesState = viewModel.addresses.collectAsState()
    val actionState = viewModel.actionState.collectAsState()

    // State for delete confirmation dialog
    var showDeleteDialog by remember { mutableStateOf(false) }
    var pendingDeleteAddressId by remember { mutableStateOf<Long?>(null) }

    // Error message state
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Log address list changes
    LaunchedEffect(addressesState.value) {
        when (val state = addressesState.value) {
            is NetworkState.Success -> {
                Log.d("AddressListScreen", "Address list updated with ${state.data.size} addresses")
            }
            is NetworkState.Failure -> {
                Log.e("AddressListScreen", "Error loading addresses: ${state.error.message}")
            }
            is NetworkState.Loading -> {
                Log.d("AddressListScreen", "Loading addresses...")
            }
            else -> {}
        }
    }

    LaunchedEffect(customerId) {
        viewModel.fetchAddresses(customerId)
    }

    // Handle navigation result from AddEditAddressScreen
    LaunchedEffect(navController) {
        navController.currentBackStackEntry?.savedStateHandle?.get<Boolean>("refresh")?.let { shouldRefresh ->
            if (shouldRefresh) {
                Log.d("AddressListScreen", "Received refresh signal, refreshing addresses")
                viewModel.fetchAddresses(customerId, true)
                // Clear the result to prevent handling it multiple times
                navController.currentBackStackEntry?.savedStateHandle?.remove<Boolean>("refresh")
            }
        }
    }
    
    // Initial fetch
    LaunchedEffect(Unit) {
        viewModel.fetchAddresses(customerId, true)  // Force refresh on initial load
    }

    // Observe action state changes
    LaunchedEffect(actionState.value) {
        when (val state = actionState.value) {
            is NetworkState.Success -> {
                Log.d("AddressListScreen", "Action successful, resetting state")
                errorMessage = null
                // Don't fetch here to avoid race conditions
                viewModel.resetActionState()
            }
            is NetworkState.Failure -> {
                Log.e("AddressListScreen", "Action failed: ${state.error.message}")
                errorMessage = state.error.message ?: "An error occurred"
                viewModel.resetActionState()
            }
            else -> {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Main content with LazyColumn as the main container
        when (val state = addressesState.value) {
            is NetworkState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = BluePrimary)
                }
            }
            is NetworkState.Failure -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Failed to load addresses", color = RedAccent)
                }
            }
            is NetworkState.Success -> {
                val addresses = state.data
                if (addresses.isEmpty()) {
                    // Empty state
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "No Addresses",
                            modifier = Modifier.size(80.dp),
                            tint = BlueSecondary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "No Addresses Yet",
                            style = MaterialTheme.typography.titleMedium,
                            color = HeaderColor,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Add your first address to get started",
                            style = MaterialTheme.typography.bodyMedium,
                            color = BlueSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    // Sort addresses to show default first
                    val sortedAddresses = addresses.sortedByDescending { it.default }
                    
                    // Address list with header and content in LazyColumn
                    Box(modifier = Modifier.fillMaxSize()) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                top = 84.dp, // 72dp for header + 12dp extra space
                                bottom = 88.dp // Space for FAB
                            )
                        ) {
                            // Address items
                            itemsIndexed(sortedAddresses) { index, address ->
                                if (index == 1 && !sortedAddresses[0].default) {
                                    // Add space above the first non-default address if first address isn't default
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                                if (index > 0 && address.default) {
                                    // Add space above the default address if it's not the first item
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                                AddressCard(
                                    address = address,
                                    onSetDefault = { if (!address.default) viewModel.setDefaultAddress(customerId, address.id) },
                                    onEdit = { navController.navigate("add_edit_address?id=${address.id}") },
                                    onDelete = { 
                                        if (!address.default) {
                                            pendingDeleteAddressId = address.id
                                            showDeleteDialog = true 
                                        }
                                    }
                                )
                            }
                        }
                        
                        // Sticky header
                        ScreenHeader(
                            title = "My Addresses",
                            onBackClick = { navController.popBackStack() },
                            backgroundColor = Color.White,
                            titleColor = HeaderColor,
                            showBackButton = true,
                        )
                    }
                }
            }
            is NetworkState.Idle -> {}
        }
    }

        // Floating Action Button for Add Address (positioned absolutely)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            FloatingActionButton(
                onClick = { navController.navigate("add_edit_address") },
                containerColor = BluePrimary,
                contentColor = Color.White,
                elevation = FloatingActionButtonDefaults.elevation(8.dp),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add, 
                    contentDescription = "Add Address",
                    modifier = Modifier.size(24.dp)
                )
            }
        }


    // Delete confirmation dialog
    if (showDeleteDialog && pendingDeleteAddressId != null) {
        DeleteCartDialog(
            show = showDeleteDialog,
            title = "Delete Address",
            subtitle = "Are you sure you want to delete this address? This action cannot be undone.",
            confirmText = "Delete",
            isLoading = actionState.value is NetworkState.Loading,
            onCancel = {
                if (actionState.value !is NetworkState.Loading) {
                    showDeleteDialog = false
                    pendingDeleteAddressId = null
                }
            },
            onConfirm = {
                val addressId = pendingDeleteAddressId
                if (addressId != null) {
                    viewModel.deleteAddress(customerId, addressId)
                    showDeleteDialog = false
                }
            }
        )
    }
}

@Composable
private fun AddressCard(
    address: Address,
    onSetDefault: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp , horizontal = 8.dp)
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (address.default) BlueLight else Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    address.address1,
                    fontWeight = FontWeight.Bold,
                    color = HeaderColor,
                    fontSize = 16.sp
                )
                Text(address.name, color = BlueSecondary, fontSize = 14.sp)
                Text(address.phone, color = BlueSecondary, fontSize = 14.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Set as default button (star)
                IconButton(
                    onClick = onSetDefault,
                    enabled = !address.default,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = if (address.default) Icons.Default.Star else Icons.Outlined.Star,
                        contentDescription = if (address.default) "Default Address" else "Set as Default",
                        tint = if (address.default) BluePrimary else BlueLight
                    )
                }
                
                // Edit button
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Address",
                        tint = BluePrimary
                    )
                }
                
                // Delete button
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(40.dp),
                    enabled = !address.default
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.delete_24),
                        contentDescription = if (address.default) "Cannot delete default address" else "Delete Address",
                        modifier = Modifier.size(24.dp),
                        colorFilter = ColorFilter.tint(if (address.default) Color.Gray else RedAccent)
                    )
                }
            }
        }
    }
}