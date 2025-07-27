package com.lee.shoppe.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lee.shoppe.ui.theme.HeaderColor

@Composable
fun ScreenHeader(
    title: String,
    onBackClick: () -> Unit,
    backgroundColor: Color = Color.White,
    titleColor: Color = HeaderColor,
    showBackButton: Boolean = true
) {
    Surface(
        shadowElevation = 4.dp,
        color = backgroundColor,
        shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundColor)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showBackButton) {
                // Back button
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = titleColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            } else {
                Spacer(modifier = Modifier.width(16.dp))
            }
            
            // Title
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = titleColor
                ),
                modifier = Modifier.weight(1f)
            )
        }
    }
}
