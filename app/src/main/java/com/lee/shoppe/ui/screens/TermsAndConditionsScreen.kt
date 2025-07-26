package com.lee.shoppe.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.lee.shoppe.ui.theme.BlueLight
import com.lee.shoppe.ui.theme.BluePrimary
import com.lee.shoppe.ui.theme.HeaderColor

@Composable
fun TermsAndConditionsScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Header
        Surface(
            shadowElevation = 4.dp,
            color = Color.White,
            shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = HeaderColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Terms & Conditions",
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = HeaderColor
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp)
        ) {
            item { IntroductionSection() }
            item { AcceptanceSection() }
            item { UserAccountSection() }
            item { ProductsAndServicesSection() }
            item { OrdersAndPaymentSection() }
            item { ShippingAndDeliverySection() }
            item { ReturnsAndRefundsSection() }
            item { IntellectualPropertySection() }
            item { PrivacySection() }
            item { LimitationOfLiabilitySection() }
            item { ModificationsSection() }
            item { ContactSection() }
        }
    }
}

@Composable
private fun IntroductionSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Introduction",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = BluePrimary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "Welcome to Shoppe! These Terms and Conditions (\"Terms\") govern your use of our mobile application and services. By accessing or using Shoppe, you agree to be bound by these Terms.",
                fontSize = 14.sp,
                color = Color.Black,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun AcceptanceSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Acceptance of Terms",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = BluePrimary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "By creating an account or using our services, you acknowledge that you have read, understood, and agree to these Terms. If you do not agree, please discontinue use of our application.",
                fontSize = 14.sp,
                color = Color.Black,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun UserAccountSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "User Account",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = BluePrimary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "• You are responsible for maintaining the confidentiality of your account credentials\n" +
                        "• You must provide accurate and complete information when creating your account\n" +
                        "• You are responsible for all activities that occur under your account\n" +
                        "• You must notify us immediately of any unauthorized use of your account",
                fontSize = 14.sp,
                color = Color.Black,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun ProductsAndServicesSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Products and Services",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = BluePrimary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "• All products are subject to availability\n" +
                        "• Product descriptions and prices are accurate at the time of listing\n" +
                        "• We reserve the right to modify or discontinue products without notice\n" +
                        "• Product images are for illustration purposes and may vary from actual products",
                fontSize = 14.sp,
                color = Color.Black,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun OrdersAndPaymentSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Orders and Payment",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = BluePrimary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "• All orders are subject to acceptance and availability\n" +
                        "• Payment must be completed at the time of order\n" +
                        "• We accept various payment methods including credit cards and digital wallets\n" +
                        "• Prices include applicable taxes unless otherwise stated",
                fontSize = 14.sp,
                color = Color.Black,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun ShippingAndDeliverySection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Shipping and Delivery",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = BluePrimary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "• Delivery times are estimates and may vary\n" +
                        "• Shipping costs are calculated based on location and order value\n" +
                        "• Risk of loss passes to you upon delivery\n" +
                        "• You must inspect items upon delivery and report any issues immediately",
                fontSize = 14.sp,
                color = Color.Black,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun ReturnsAndRefundsSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Returns and Refunds",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = BluePrimary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "• Returns must be initiated within 30 days of delivery\n" +
                        "• Items must be in original condition with tags attached\n" +
                        "• Refunds will be processed to the original payment method\n" +
                        "• Return shipping costs may apply unless the item is defective",
                fontSize = 14.sp,
                color = Color.Black,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun IntellectualPropertySection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Intellectual Property",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = BluePrimary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "All content, trademarks, and intellectual property on Shoppe are owned by us or our licensors. You may not use, reproduce, or distribute any content without our written permission.",
                fontSize = 14.sp,
                color = Color.Black,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun PrivacySection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Privacy",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = BluePrimary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "Your privacy is important to us. Please review our Privacy Policy to understand how we collect, use, and protect your personal information.",
                fontSize = 14.sp,
                color = Color.Black,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun LimitationOfLiabilitySection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Limitation of Liability",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = BluePrimary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "To the fullest extent permitted by law, Shoppe shall not be liable for any indirect, incidental, special, or consequential damages arising from your use of our services.",
                fontSize = 14.sp,
                color = Color.Black,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun ModificationsSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Modifications",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = BluePrimary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "We reserve the right to modify these Terms at any time. Changes will be effective immediately upon posting. Your continued use of the app constitutes acceptance of the modified Terms.",
                fontSize = 14.sp,
                color = Color.Black,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun ContactSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = BlueLight)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Contact Us",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = BluePrimary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "If you have any questions about these Terms and Conditions, please contact us:",
                fontSize = 14.sp,
                color = Color.Black,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "Email: support@shoppe.com\nPhone: +1 (555) 123-4567",
                fontSize = 14.sp,
                color = HeaderColor,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Last updated: July 2025",
                fontSize = 12.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}
