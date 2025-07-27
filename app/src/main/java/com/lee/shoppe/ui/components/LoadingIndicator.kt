package com.lee.shoppe.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.lee.shoppe.ui.theme.BluePrimary

/**
 * A reusable loading indicator that can be used across the app.
 *
 * @param modifier Modifier to be applied to the loading indicator container
 * @param color Color of the loading indicator (default: BluePrimary)
 * @param size Size of the loading indicator (default: 48dp)
 * @param isFullScreen Whether the loading indicator should take up the full screen (default: false)
 */
@Composable
fun LoadingIndicator(
    modifier: Modifier = Modifier,
    color: Color = BluePrimary,
    size: Int = 48,
    isFullScreen: Boolean = false
) {
    val containerModifier = if (isFullScreen) {
        Modifier.fillMaxSize()
    } else {
        Modifier
    }

    Box(
        modifier = containerModifier.then(modifier),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = color,
            modifier = Modifier.size(size.dp),
            strokeWidth = 3.dp
        )
    }
}

/**
 * A full-screen loading indicator with default styling.
 *
 * @param modifier Additional modifier to be applied to the loading indicator
 * @param color Color of the loading indicator (default: BluePrimary)
 */
@Composable
fun FullScreenLoading(
    modifier: Modifier = Modifier,
    color: Color = BluePrimary
) {
    LoadingIndicator(
        modifier = modifier,
        color = color,
        isFullScreen = true
    )
}
