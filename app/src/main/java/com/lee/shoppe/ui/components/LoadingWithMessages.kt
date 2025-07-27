package com.lee.shoppe.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lee.shoppe.R
import com.lee.shoppe.ui.theme.BlueLight

/**
 * A reusable loading component with a centered loading indicator and two customizable messages below it.
 *
 * @param modifier Modifier to be applied to the component
 * @param loadingIndicatorSize Size of the loading indicator (default: 48dp)
 * @param loadingIndicatorColor Color of the loading indicator (default: BluePrimary)
 * @param spacing Space between the loading indicator and the first message (default: 16dp)
 * @param messageSpacing Space between the two messages (default: 8dp)
 * @param mainMessage The main message to display (default: "Loading...")
 * @param secondaryMessage The secondary message to display (default: "Please wait")
 * @param mainMessageStyle Typography style for the main message
 * @param secondaryMessageStyle Typography style for the secondary message
 */
@Composable
fun LoadingWithMessages(
    modifier: Modifier = Modifier,
    loadingIndicatorSize: Int = 48,
    loadingIndicatorColor: Color = BlueLight,
    spacing: Dp = 16.dp,
    messageSpacing: Dp = 8.dp,
    mainMessage: String = stringResource(R.string.loading_main_message),
    secondaryMessage: String = stringResource(R.string.loading_secondary_message),
    mainMessageStyle: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.titleMedium.copy(
        color = Color.Black,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp
    ),
    secondaryMessageStyle: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodyMedium.copy(
        color = Color(0xFF444444),
        fontSize = 16.sp
    )
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Loading Indicator
        LoadingIndicator(
            size = loadingIndicatorSize,
            color = loadingIndicatorColor
        )
        
        Spacer(modifier = Modifier.height(spacing))
        
        // Main Message
        Text(
            text = mainMessage,
            style = mainMessageStyle,
            textAlign = TextAlign.Center
        )
        
        // Secondary Message (if provided)
        if (secondaryMessage.isNotBlank()) {
            Spacer(modifier = Modifier.height(messageSpacing))
            Text(
                text = secondaryMessage,
                style = secondaryMessageStyle,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * A full-screen loading component with a centered loading indicator and two messages below it.
 *
 * @param modifier Additional modifier to be applied to the component
 * @param mainMessage The main message to display
 * @param secondaryMessage The secondary message to display
 * @param loadingIndicatorColor Color of the loading indicator
 */
@Composable
fun FullScreenLoadingWithMessages(
    modifier: Modifier = Modifier,
    mainMessage: String = stringResource(R.string.loading_main_message),
    secondaryMessage: String = stringResource(R.string.loading_secondary_message),
    loadingIndicatorColor: Color = Color(0xFF2196F3)
) {
    LoadingWithMessages(
        modifier = modifier.fillMaxSize(),
        mainMessage = mainMessage,
        secondaryMessage = secondaryMessage,
        loadingIndicatorColor = loadingIndicatorColor,
        spacing = 24.dp,
        messageSpacing = 8.dp
    )
}
