package com.lee.shoppe.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lee.shoppe.data.model.Review
import com.lee.shoppe.data.network.networking.NetworkState
import com.lee.shoppe.ui.viewmodel.ProductInfoViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.semantics.Role.Companion.Image

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreen(
    viewModel: ProductInfoViewModel = hiltViewModel(),
    onBack: (() -> Unit)? = null
) {
    val reviewState by viewModel.reviews.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.getReviews()
    }

    Box {
        Column(modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
        ) {
            // Custom Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    if (onBack != null) {
                        onBack()
                    }
                }) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Reviews",
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    modifier = Modifier.weight(1f)
                )
            }

            // Content
            when (reviewState) {
                is NetworkState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is NetworkState.Success -> {
                    val reviews = (reviewState as NetworkState.Success<List<Review>>).data
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White)
                            .padding(horizontal = 5.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(reviews) { review ->
                            ReviewItem(review)
                        }
                    }
                }
                is NetworkState.Failure -> {
                    val error = (reviewState as NetworkState.Failure).error.message
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Failed to load reviews: $error")
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
fun ReviewItem(review: Review) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        //elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(Modifier.padding(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = review.imageResId),
                    contentDescription = "${review.name}'s photo",
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(review.name, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                repeat(5) { i ->
                    Icon(
                        imageVector = if (i < review.rate.toInt()) Icons.Default.Star else Icons.Default.StarOutline,
                        contentDescription = null,
                        tint = if (i < review.rate.toInt()) Color(0xFFECA61B) else Color(0xFFE0E0E0),
                        modifier = Modifier.size(25.dp)
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(review.description)
        }
    }
} 