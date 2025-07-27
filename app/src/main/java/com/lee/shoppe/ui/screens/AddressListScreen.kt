package com.lee.shoppe.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.lee.shoppe.R
import com.lee.shoppe.data.model.CustomerData
import com.lee.shoppe.data.network.networking.NetworkState
import com.lee.shoppe.ui.components.ScreenHeader
import com.lee.shoppe.ui.screens.dialogBox.EmptyState
import com.lee.shoppe.ui.theme.BlueLight
import com.lee.shoppe.ui.theme.BluePrimary
import com.lee.shoppe.ui.theme.BlueSecondary
import com.lee.shoppe.ui.theme.HeaderColor
import com.lee.shoppe.ui.theme.PurpleGrey40
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
    val lottieComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.address_empty))

    LaunchedEffect(customerId) {
        viewModel.fetchAddresses(customerId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Header
        ScreenHeader(
            title = "My Addresses",
            onBackClick = { navController.popBackStack() },
            showBackButton = true
        )

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
                    Text("Failed to load addresses", color = RedAccent)
                }
            }
            is NetworkState.Success -> {
                // Sort addresses to show default address first
                val addresses = state.data.sortedByDescending { it.default ?: false }
                if (addresses.isEmpty()) {
//                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
//                        Text("No addresses found.", color = BlueSecondary)
//                    }
                    EmptyState(lottieComposition , "No Addresses Yet" , "You haven't added any addresses. Start by adding one now to speed up checkout!" )
                } else {
                    Column(modifier = Modifier.padding(horizontal = 8.dp)) {
                        addresses.forEachIndexed { idx, address ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .shadow(4.dp, RoundedCornerShape(16.dp)),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (address.default) BlueLight else Color.White
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            address.address1,
                                            fontWeight = FontWeight.Bold,
                                            color = HeaderColor,
                                            fontSize = 18.sp
                                        )
                                        Text(address.name, color = BlueSecondary, fontSize = 14.sp)
                                        Text(address.phone, color = BlueSecondary, fontSize = 14.sp)
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(
                                            onClick = { viewModel.setDefaultAddress(customerId, address.id) },
                                            enabled = !address.default,
                                            modifier = Modifier.size(40.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (address.default) Icons.Default.Star else Icons.Outlined.Star,
                                                contentDescription = if (address.default) "Default Address" else "Set as Default",
                                                tint = if (address.default) BluePrimary else PurpleGrey40
                                            )
                                        }
                                        IconButton(
                                            onClick = { navController.navigate("add_edit_address?id=${address.id}") },
                                            enabled = true,
                                            modifier = Modifier.size(40.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Edit Address",
                                                tint = BluePrimary
                                            )
                                        }
                                        IconButton(
                                            onClick = {
                                                if (!address.default) {
                                                    pendingDeleteAddressId = address.id
                                                    showDeleteDialog = true
                                                }
                                            },
                                            modifier = Modifier.size(40.dp)
                                        ) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.delete_24),
                                                contentDescription = if (address.default) "Cannot delete default address" else "Delete Address",
                                                tint = if (address.default) PurpleGrey40 else RedAccent
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            is NetworkState.Idle -> {}
        }
        // Floating Action Button for Add Address
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomEnd
        ) {
            FloatingActionButton(
                onClick = { navController.navigate("add_edit_address") },
                containerColor = BluePrimary,
                contentColor = Color.White,
                elevation = FloatingActionButtonDefaults.elevation(8.dp),
                modifier = Modifier.padding(24.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Address")
            }
        }
    }

    // Delete confirmation dialog
    DeleteCartDialog(
        show = showDeleteDialog && pendingDeleteAddressId != null,
        title = "Delete Address",
        subtitle = "Are you sure you want to delete this address? This action cannot be undone.",
        confirmText = "Delete",
        onCancel = { showDeleteDialog = false; pendingDeleteAddressId = null },
        onConfirm = {
            showDeleteDialog = false
            val addressId = pendingDeleteAddressId
            if (addressId != null) {
                viewModel.deleteAddress(customerId, addressId)
            }
            pendingDeleteAddressId = null
        }
    )
}