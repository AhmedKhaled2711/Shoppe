package com.lee.shoppe.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.lee.shoppe.R
import com.lee.shoppe.ui.theme.BluePrimary
import com.lee.shoppe.ui.theme.BlueSecondary

@Composable
fun OrderSuccessScreen(
    onTimeout: () -> Unit,
    message: String = "Your order has been placed successfully!",
    timeoutMillis: Long = 2500L
) {
    // Lottie animation composition
    val composition by rememberLottieComposition(
        spec = LottieCompositionSpec.RawRes(R.raw.sucess_opration)
    )
    
    // Animation progress
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever,
        isPlaying = true,
        speed = 1f,
        restartOnPlay = false
    )
    
    // Auto-dismiss after timeout
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(timeoutMillis)
        onTimeout()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Lottie Animation
            LottieAnimation(
                composition = composition,
                progress = { progress },
                modifier = Modifier
                    .size(200.dp)
                    .padding(16.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Order Confirmed!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = BluePrimary,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = BlueSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )
        }
    }
}
