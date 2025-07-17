package com.lee.shoppe.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.lee.shoppe.ui.theme.BlueLight
import com.lee.shoppe.ui.theme.BluePrimary
import com.lee.shoppe.ui.theme.HeaderColor

@Composable
fun PaymentScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        Icon(
            imageVector = Icons.Filled.CreditCard,
            contentDescription = "Payment Method",
            tint = BluePrimary,
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(40.dp))
                .background(BlueLight, RoundedCornerShape(40.dp))
                .padding(12.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("Payment: **** **** **** 1234", fontSize = 18.sp, color = HeaderColor)
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = { /* TODO: Edit/Add payment */ },
            colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
        ) {
            Text("Edit/Add Payment", color = Color.White)
        }
    }
} 