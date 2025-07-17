package com.lee.shoppe.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.lee.shoppe.data.model.CustomerData
import com.lee.shoppe.ui.screens.dialogBox.NetworkErrorBox
import com.lee.shoppe.ui.theme.BlueLight
import com.lee.shoppe.ui.theme.BluePrimary
import com.lee.shoppe.ui.theme.HeaderColor
import com.lee.shoppe.ui.theme.RedAccent
import com.lee.shoppe.ui.theme.Dark
import com.lee.shoppe.ui.utils.isNetworkConnected
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(navController: NavController) {
    val context = LocalContext.current
    val customerData = CustomerData.getInstance(context)
    val isNetworkConnected = isNetworkConnected(context)
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val showLogoutDialog = remember { mutableStateOf(false) }
    val showDeleteDialog = remember { mutableStateOf(false) }
    val showLanguageDialog = remember { mutableStateOf(false) }
    val showCurrencyDialog = remember { mutableStateOf(false) }

    if (!isNetworkConnected) {
        Box(Modifier.fillMaxSize()) {
            NetworkErrorBox(show = true)
        }
        return
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .background(Color.White)
    ) {
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))
            // Header
            Text(
                text = "Shoppe",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = BluePrimary,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(24.dp))
            // User Info or Login
            if (customerData.isLogged) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BlueLight, RoundedCornerShape(16.dp))
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Filled.PersonOutline,
                            contentDescription = "User Avatar",
                            tint = BluePrimary,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color.White, CircleShape)
                                .padding(8.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = customerData.name.ifBlank { "Your Name" },
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = HeaderColor
                        )
                        Text(
                            text = customerData.email.ifBlank { "your.email@example.com" },
                            fontSize = 15.sp,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BlueLight, RoundedCornerShape(16.dp))
                        .padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.PersonOutline,
                        contentDescription = "User Avatar",
                        tint = BluePrimary,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color.White, CircleShape)
                            .padding(8.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Welcome, Guest!",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = HeaderColor
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { navController.navigate("login") },
                        colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                    ) {
                        Text("Login", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
            if (customerData.isLogged) {
                // Personal Section
                SectionHeader("Personal")
                ProfileListItem("Profile") {
                    navController.navigate("profile_details")
                }
                DividerLine()
                ProfileListItem("Manage Addresses") {
                    navController.navigate("address_list")
                }
                DividerLine()
                ProfileListItem("Shipping Address") {
                    navController.navigate("shipping_address")
                }
                DividerLine()
                ProfileListItem("Payment methods") {
                    navController.navigate("payment")
                }
                Spacer(modifier = Modifier.height(24.dp))
                // Shop Section
                SectionHeader("Shop")
                ProfileListItem("Country", customerData.currency.ifBlank { "USD" }) {
                    showCurrencyDialog.value = true
                }
                DividerLine()
                ProfileListItem("Currency", customerData.currency.ifBlank { "$ USD" }) {
                    showCurrencyDialog.value = true
                }
                DividerLine()
                ProfileListItem("Sizes", "UK") {
                    coroutineScope.launch { snackbarHostState.showSnackbar("Sizes coming soon") }
                }
                DividerLine()
                ProfileListItem("Terms and Conditions") {
                    coroutineScope.launch { snackbarHostState.showSnackbar("Terms and Conditions coming soon") }
                }
                Spacer(modifier = Modifier.height(24.dp))
                // Account Section
                SectionHeader("Account")
                ProfileListItem("Language", customerData.language.ifBlank { "English" }) {
                    showLanguageDialog.value = true
                }
                DividerLine()
                ProfileListItem("About Shoppe") {
                    coroutineScope.launch { snackbarHostState.showSnackbar("About coming soon") }
                }
                Spacer(modifier = Modifier.height(24.dp))
                // Logout
                Button(
                    onClick = { showLogoutDialog.value = true },
                    colors = ButtonDefaults.buttonColors(containerColor = BlueLight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Logout", color = BluePrimary, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                // Delete Account
                Text(
                    text = "Delete My Account",
                    color = RedAccent,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .clickable { showDeleteDialog.value = true }
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            // Footer
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Shoppe",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = BluePrimary
                )
                Text(
                    text = "Version 1.0 April, 2020",
                    color = Color.Gray,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }

    // Logout Dialog
    if (showLogoutDialog.value) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog.value = false },
            title = { Text("Logout") },
            text = { Text("Are you sure you want to logout?") },
            confirmButton = {
                Button(onClick = {
                    customerData.logOut()
                    showLogoutDialog.value = false
                    navController.navigate("login")
                }) { Text("Logout") }
            },
            dismissButton = {
                Button(onClick = { showLogoutDialog.value = false }) { Text("Cancel") }
            }
        )
    }
    // Delete Account Dialog
    if (showDeleteDialog.value) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog.value = false },
            title = { Text("Delete Account") },
            text = { Text("Are you sure you want to delete your account? This action cannot be undone.") },
            confirmButton = {
                Button(onClick = {
                    // TODO: Implement delete logic
                    showDeleteDialog.value = false
                    coroutineScope.launch { snackbarHostState.showSnackbar("Account deletion coming soon") }
                }, colors = ButtonDefaults.buttonColors(containerColor = RedAccent)) { Text("Delete", color = Color.White) }
            },
            dismissButton = {
                Button(onClick = { showDeleteDialog.value = false }) { Text("Cancel") }
            }
        )
    }
    // Language Dialog (static)
    if (showLanguageDialog.value) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog.value = false },
            title = { Text("Choose Language") },
            text = { Text("Language selection coming soon") },
            confirmButton = {
                Button(onClick = { showLanguageDialog.value = false }) { Text("OK") }
            }
        )
    }
    // Currency Dialog (static)
    if (showCurrencyDialog.value) {
        AlertDialog(
            onDismissRequest = { showCurrencyDialog.value = false },
            title = { Text("Choose Currency") },
            text = { Text("Currency selection coming soon") },
            confirmButton = {
                Button(onClick = { showCurrencyDialog.value = false }) { Text("OK") }
            }
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        fontWeight = FontWeight.Bold,
        fontSize = 17.sp,
        color = HeaderColor,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
private fun ProfileListItem(
    title: String,
    value: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            color = Dark,
            modifier = Modifier.weight(1f)
        )
        if (value != null) {
            Text(
                text = value,
                fontSize = 15.sp,
                color = Color.Gray,
                modifier = Modifier.padding(end = 8.dp)
            )
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Dark
        )
    }
}

@Composable
private fun DividerLine() {
    Divider(
        color = Color(0xFFE0E0E0),
        thickness = 1.dp,
        modifier = Modifier.padding(start = 0.dp)
    )
} 