package com.lee.shoppe.ui.screens

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.lee.shoppe.R
import com.lee.shoppe.data.model.CustomerData
import com.lee.shoppe.ui.screens.dialogBox.NetworkErrorBox
import com.lee.shoppe.ui.components.LanguageSelectionDialog
import com.lee.shoppe.ui.theme.BlueLight
import com.lee.shoppe.ui.theme.BluePrimary
import com.lee.shoppe.ui.theme.Dark
import com.lee.shoppe.ui.theme.HeaderColor
import com.lee.shoppe.ui.theme.RedAccent
import com.lee.shoppe.ui.utils.isNetworkConnected
import com.lee.shoppe.utils.LanguageUtils
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
                text = stringResource(R.string.app_name),
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
                            contentDescription = stringResource(R.string.user_avatar),
                            tint = BluePrimary,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color.White, CircleShape)
                                .padding(8.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = customerData.name.ifBlank { stringResource(R.string.default_name) },
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = HeaderColor
                        )
                        Text(
                            text = customerData.email.ifBlank { stringResource(R.string.default_email) },
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
                        text = stringResource(R.string.welcome_guest),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = HeaderColor
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { navController.navigate("login") },
                        colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                    ) {
                        Text(stringResource(R.string.login), color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
            if (customerData.isLogged) {
                // Personal Section
                SectionHeader(stringResource(R.string.personal))
//                ProfileListItem("Profile") {
//                    navController.navigate("profile_details")
//                }
//                DividerLine()
                Spacer(modifier = Modifier.height(10.dp))
                ProfileListItem(stringResource(R.string.manage_addresses)) {
                    navController.navigate("address_list")
                }
                DividerLine()
                Spacer(modifier = Modifier.height(10.dp))
                ProfileListItem(stringResource(R.string.orders)) {
                    navController.navigate("orders?forceRefresh=true")
                }
                DividerLine()
//                Spacer(modifier = Modifier.height(10.dp))
//                ProfileListItem("Payment methods") {
//                    navController.navigate("payment")
//                }
//                DividerLine()

                Spacer(modifier = Modifier.height(24.dp))
                // Shop Section
                SectionHeader(stringResource(R.string.shop))
//                ProfileListItem("Country", "EGY") {
//                    showCurrencyDialog.value = true
//                }
//                DividerLine()
//                Spacer(modifier = Modifier.height(10.dp))
                ProfileListItem(stringResource(R.string.currency), customerData.currency.ifBlank { "$ USD" }) {
                    showCurrencyDialog.value = true
                }
                DividerLine()
//                Spacer(modifier = Modifier.height(10.dp))
//                ProfileListItem("Sizes", "UK") {
//                    coroutineScope.launch { snackbarHostState.showSnackbar("Sizes coming soon") }
//                }
//                DividerLine()
                Spacer(modifier = Modifier.height(10.dp))
                ProfileListItem(stringResource(R.string.terms_and_conditions)) {
                    navController.navigate("terms_and_conditions")
                }
                DividerLine()
                Spacer(modifier = Modifier.height(24.dp))
                // Account Section
                SectionHeader(stringResource(R.string.account))
                val currentLanguage = when (LanguageUtils.getLanguage(context)) {
                    "ar" -> stringResource(R.string.language_arabic)
                    else -> stringResource(R.string.language_english)
                }
                ProfileListItem(stringResource(R.string.language), currentLanguage) {
                    showLanguageDialog.value = true
                }
                DividerLine()
                Spacer(modifier = Modifier.height(10.dp))
                ProfileListItem(stringResource(R.string.about_shoppe)) {
                    navController.navigate("about")
                }
                DividerLine()
                Spacer(modifier = Modifier.height(24.dp))
                // Logout
                Button(
                    onClick = { showLogoutDialog.value = true },
                    colors = ButtonDefaults.buttonColors(containerColor = BlueLight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.logout), color = BluePrimary, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                // Delete Account
                Text(
                    text = stringResource(R.string.delete_my_account),
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
                    text = stringResource(R.string.app_name),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = BluePrimary
                )
                Text(
                    text = stringResource(R.string.version_format, "1.0", "Jul", 2025),
                    color = Color.Gray,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }

    // Professional Logout Dialog using DeleteCartDialog
    DeleteCartDialog(
        show = showLogoutDialog.value,
        title = stringResource(R.string.logout_confirmation_title),
        subtitle = stringResource(R.string.logout_confirmation_message, customerData.name.ifBlank { stringResource(R.string.default_name) }),
        confirmText = stringResource(R.string.logout),
        onCancel = { showLogoutDialog.value = false },
        onConfirm = {
            customerData.logOut()
            showLogoutDialog.value = false
            navController.navigate("login") {
                popUpTo(0) { inclusive = true }
            }
        }
    )
    val account_deletion_coming_soon =  stringResource(R.string.account_deletion_coming_soon)
    // Delete Account Dialog
    DeleteCartDialog(
        show = showDeleteDialog.value,
        title = stringResource(R.string.delete_account_title),
        subtitle = stringResource(R.string.delete_account_confirmation),
        confirmText = stringResource(R.string.delete),
        onCancel = { showDeleteDialog.value = false },
        onConfirm = {
            // TODO: Implement delete logic
            showDeleteDialog.value = false
            coroutineScope.launch { 
                snackbarHostState.showSnackbar(account_deletion_coming_soon)
            }
        }
    )
    // Language Dialog
    if (showLanguageDialog.value) {
        LanguageSelectionDialog(
            onDismiss = { showLanguageDialog.value = false },
            onLanguageSelected = { languageCode ->
                LanguageUtils.setLocale(context, languageCode)
                (context as? Activity)?.let { activity ->
                    activity.intent.putExtra("lang_changed", true)
                    activity.finish()
                    activity.startActivity(activity.intent)
                    activity.overridePendingTransition(0, 0)
                }
            },
            currentLanguage = LanguageUtils.getLanguage(context)
        )
    }
    // Currency Dialog (static)
    if (showCurrencyDialog.value) {
        AlertDialog(
            onDismissRequest = { showCurrencyDialog.value = false },
            title = { Text(stringResource(R.string.choose_currency)) },
            text = { Text(stringResource(R.string.currency_selection_coming_soon)) },
            confirmButton = {
                Button(onClick = { showCurrencyDialog.value = false }) { Text(stringResource(R.string.ok)) }
            }
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 20.sp,
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
            //.padding(vertical = 10.dp)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 18.sp,
            color = Color.Black,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.weight(1f)
        )
        if (value != null) {
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                modifier = Modifier.padding(end = 8.dp)
            )
        }
        Icon(
            painter = painterResource(id = R.drawable.chevron_right_24),
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