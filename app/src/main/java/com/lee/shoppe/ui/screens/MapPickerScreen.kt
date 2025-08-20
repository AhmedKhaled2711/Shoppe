package com.lee.shoppe.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@Composable
fun MapPickerScreen(
    onLocationPicked: (LatLng) -> Unit,
    onCancel: () -> Unit
) {
    val cairo = LatLng(30.0444, 31.2357)
    var pickedLocation by remember { mutableStateOf<LatLng?>(cairo) }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(cairo, 15f)
    }

    Box(Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            onMapClick = { latLng -> pickedLocation = latLng }
        ) {
            pickedLocation?.let {
                Marker(
                    state = MarkerState(position = it),
                    title = "Selected Location"
                )
            }
        }
        Row(
            Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(onClick = onCancel) { Text("Cancel") }
            Button(
                onClick = { pickedLocation?.let { onLocationPicked(it) } },
                enabled = pickedLocation != null
            ) { Text("Use this location") }
        }
    }
} 