//package com.lee.shoppe.ui.components
//
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Favorite
//import androidx.compose.material.icons.filled.FavoriteBorder
//import androidx.compose.material.icons.filled.Star
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.layout.ContentScale
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.style.TextOverflow
//import androidx.compose.ui.unit.dp
//import com.lee.shoppe.R
//import com.lee.shoppe.data.model.Product
//import com.lee.shoppe.ui.utils.NetworkImage
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun ProductCard(
//    product: Product,
//    isFavorite: Boolean,
//    onFavoriteClick: () -> Unit,
//    onProductClick: () -> Unit,
//    modifier: Modifier = Modifier
//) {
//    var isFavoriteState by remember { mutableStateOf(isFavorite) }
//
//    // Update local state when prop changes
//    LaunchedEffect(isFavorite) {
//        isFavoriteState = isFavorite
//    }
//
//    Card(
//        onClick = onProductClick,
//        modifier = modifier
//            .fillMaxWidth()
//            .aspectRatio(0.7f),
//        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
//        shape = MaterialTheme.shapes.medium
//    ) {
//        Column(
//            modifier = Modifier.fillMaxSize()
//        ) {
//            // Product image with favorite button
//            Box(
//                modifier = Modifier
//                    .weight(1f)
//                    .fillMaxWidth()
//            ) {
//                // Product image with placeholder
//                NetworkImage(
//                    url = product.image?.src ?: "",
//                    contentDescription = product.title,
//                    modifier = Modifier.fillMaxSize(),
//                    contentScale = ContentScale.Crop
//                )
//
//                // Favorite button
//                IconButton(
//                    onClick = {
//                        isFavoriteState = !isFavoriteState
//                        onFavoriteClick()
//                    },
//                    modifier = Modifier
//                        .align(Alignment.TopEnd)
//                        .padding(8.dp)
//                        .background(
//                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
//                            shape = CircleShape
//                        )
//                ) {
//                    Icon(
//                        imageVector = if (isFavoriteState) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
//                        contentDescription = if (isFavoriteState) "Remove from favorites" else "Add to favorites",
//                        tint = if (isFavoriteState) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
//                    )
//                }
//
//                // Price tag
//                Surface(
//                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
//                    shape = MaterialTheme.shapes.small,
//                    modifier = Modifier
//                        .align(Alignment.BottomStart)
//                        .padding(8.dp)
//                ) {
//                    Text(
//                        text = "$${product.variants?.firstOrNull()?.price ?: "0.00"}",
//                        color = MaterialTheme.colorScheme.onPrimary,
//                        style = MaterialTheme.typography.labelLarge,
//                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
//                    )
//                }
//            }
//
//            // Product info
//            Column(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(8.dp)
//            ) {
//                // Product title
//                Text(
//                    text = product.title ?: "",
//                    style = MaterialTheme.typography.titleSmall,
//                    maxLines = 1,
//                    overflow = TextOverflow.Ellipsis,
//                    modifier = Modifier.fillMaxWidth()
//                )
//
//                // Rating and reviews
//                Row(
//                    verticalAlignment = Alignment.CenterVertically,
//                    modifier = Modifier.padding(top = 4.dp)
//                ) {
//                    // Star rating
//                    Icon(
//                        imageVector = Icons.Default.Star,
//                        contentDescription = "Rating",
//                        tint = MaterialTheme.colorScheme.primary,
//                        modifier = Modifier.size(16.dp)
//                    )
//
//                    Text(
//                        text = "${4.5}", // Placeholder rating
//                        style = MaterialTheme.typography.labelMedium,
//                        color = MaterialTheme.colorScheme.onSurfaceVariant,
//                        modifier = Modifier.padding(start = 2.dp, end = 4.dp)
//                    )
//
//                    // Review count
//                    Text(
//                        text = "(0)", // Placeholder review count
//                        style = MaterialTheme.typography.labelSmall,
//                        color = MaterialTheme.colorScheme.outline
//                    )
//                }
//
//                // Add to cart button
//                Button(
//                    onClick = { /* TODO: Add to cart */ },
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(top = 8.dp),
//                    shape = MaterialTheme.shapes.small,
//                    colors = ButtonDefaults.buttonColors(
//                        containerColor = MaterialTheme.colorScheme.primary,
//                        contentColor = MaterialTheme.colorScheme.onPrimary
//                    ),
//                    contentPadding = PaddingValues(vertical = 8.dp)
//                ) {
//                    Text(
//                        text = "Add to Cart",
//                        style = MaterialTheme.typography.labelLarge
//                    )
//                }
//            }
//        }
//    }
//}
