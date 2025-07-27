package com.lee.shoppe.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.lee.shoppe.R
import com.lee.shoppe.data.model.CustomerData
import com.lee.shoppe.data.model.Product
import com.lee.shoppe.data.model.ProductImage
import com.lee.shoppe.data.model.Variant
import com.lee.shoppe.data.network.networking.NetworkState
import com.lee.shoppe.ui.components.LoadingWithMessages
import com.lee.shoppe.ui.components.ScreenHeader
import com.lee.shoppe.ui.theme.BluePrimary
import com.lee.shoppe.ui.theme.Dark
import com.lee.shoppe.ui.utils.isNetworkConnected
import com.lee.shoppe.ui.viewmodel.FavViewModel
import kotlinx.coroutines.launch
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteScreen(
    navController: NavController,
    favViewModel: FavViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val customerData = CustomerData.getInstance(context)
    val isNetworkConnected = isNetworkConnected(context)
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    
    // State
    var favoriteProducts by remember { mutableStateOf<List<Product>>(emptyList()) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var productToDelete by remember { mutableStateOf<Product?>(null) }
    
    // Collect state from ViewModel
    val favProductsState by favViewModel.product.collectAsState()

    // Load favorites when screen is first shown
    LaunchedEffect(Unit) {
        if (isNetworkConnected && (customerData.isLogged || customerData.isGuestWithPreservedData) && customerData.favListId > 0) {
            favViewModel.getFavProducts(customerData.favListId)
        }
    }

    // Process state changes
    LaunchedEffect(favProductsState) {
        when (val state = favProductsState) {
            is NetworkState.Success -> {
                state.data.draft_order.line_items
                    .drop(1) // Skip the first dummy item
                    .mapNotNull { lineItem ->
                        lineItem.sku?.split("*")?.takeIf { it.size >= 2 }
                            ?.let { parts ->
                                parts[0].toLongOrNull()?.let { productId ->
                                    Product(
                                        id = productId,
                                        title = lineItem.title ?: "Product",
                                        image = ProductImage(src = parts[1]),
                                        variants = listOf(
                                            Variant(price = lineItem.price ?: "0.0")
                                        )
                                    )
                                }
                            }
                    }
                    .let { favoriteProducts = it }
            }
            is NetworkState.Failure -> {
                val error = state.error
                val message = when {
                    error.message?.contains("Too many requests") == true || 
                    (error is retrofit2.HttpException && error.code() == 429) ->
                        context.getString(R.string.too_many_requests_error)
                    else -> error.message ?: context.getString(R.string.unknown_error)
                }
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(message)
                }
                favoriteProducts = emptyList()
            }
            NetworkState.Loading -> {
                // Loading state is handled by the UI
                favoriteProducts = emptyList()
            }
            NetworkState.Idle -> {
                // Initial state, do nothing
            }
        }
    }

    // Main UI
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            ScreenHeader(
                title = stringResource(R.string.favorites),
                onBackClick = { navController.navigateUp() }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (favProductsState) {
                is NetworkState.Loading -> {
                    LoadingWithMessages(
                        modifier = Modifier.fillMaxSize(),
                        mainMessage = stringResource(R.string.loading_favorites),
                        secondaryMessage = stringResource(R.string.please_wait_loading_favorites),
                        loadingIndicatorColor = BluePrimary,
                        spacing = 16.dp,
                        messageSpacing = 8.dp
                    )
                }
                is NetworkState.Failure, NetworkState.Idle -> {
                    EmptyFavoriteState()
                }
                is NetworkState.Success -> {
                    if (favoriteProducts.isEmpty()) {
                        EmptyFavoriteState()
                    } else {
                        FavoriteProductsGrid(
                            products = favoriteProducts,
                            onProductClick = { product ->
                                navController.navigate("product_details/${product.id}")
                            },
                            onFavoriteClick = { product ->
                                productToDelete = product
                                showDeleteDialog = true
                            }
                        )
                    }
                }
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.remove_from_favorites)) },
            text = { Text(stringResource(R.string.are_you_sure_remove_favorite)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        productToDelete?.let { product ->
                            // TODO: Implement remove from favorites
                            showDeleteDialog = false
                        }
                    }
                ) {
                    Text(stringResource(R.string.remove))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false }
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun FavoriteProductsGrid(
    products: List<Product>,
    onProductClick: (Product) -> Unit,
    onFavoriteClick: (Product) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(products) { product ->
            FavoriteProductCard(
                product = product,
                onFavoriteClick = { onFavoriteClick(product) },
                onCardClick = { onProductClick(product) },
                isFavorite = true
            )
        }
    }
}

@Composable
private fun EmptyFavoriteState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.FavoriteBorder,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = Color.LightGray
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.no_favorites_yet),
            style = MaterialTheme.typography.titleLarge,
            color = Dark,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.your_favorites_will_appear_here),
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun FavoriteProductCard(
    product: Product,
    onFavoriteClick: () -> Unit,
    onCardClick: () -> Unit,
    isFavorite: Boolean
) {
    Card(
        modifier = Modifier
            .width(190.dp)
            .height(320.dp)
            .clickable { onCardClick() },
        shape = RoundedCornerShape(10.dp),
        elevation = CardDefaults.cardElevation(5.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Image with favorite button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                AsyncImage(
                    model = product.image?.src,
                    contentDescription = product.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    error = painterResource(id = R.drawable.broken_image)
                )

                // Favorite button overlay
                IconButton(
                    onClick = { onFavoriteClick() },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(32.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Toggle favorite",
                        tint = if (isFavorite) Color(0xFF0057FF) else Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Product details
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    // Product title
                    Text(
                        text = product.title
                            ?.split("|")
                            ?.getOrNull(1)
                            ?.trim()
                            ?.let { title ->
                                val words = title.split(" ")
                                if (words.size > 3) {
                                    words.take(3).joinToString(" ") + "..."
                                } else {
                                    title
                                }
                            } ?: "Unknown Product",
                        color = Color.Black,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )



                    Spacer(modifier = Modifier.height(8.dp))

                    // Rating
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Ensure rating is between 3.0 and 5.0
                        val randomRating = remember { Random.nextDouble(3.0, 5.0).toFloat() }

                        repeat(5) { index ->
                            val fullStarThreshold = index + 1
                            Icon(
                                imageVector = if (index < randomRating.toInt()) Icons.Default.Star else Icons.Default.StarOutline,
                                contentDescription = null,
                                tint = if (index < randomRating.toInt()) Color(0xFFFFD700) else Color(0xFFE0E0E0),
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        Text(
                            text = "(${String.format("%.1f", randomRating)})",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                }

                // Price at bottom
                val price = product.variants?.firstOrNull()?.price ?: "0.0"
                val context = LocalContext.current
                val customerData = CustomerData.getInstance(context)
                val currencySymbol = when (val currency = customerData.currency) {
                    "USD" -> "$"
                    "EGY" -> "EGP"
                    "EUR" -> "€"
                    "GBP" -> "£"
                    else -> currency
                }

                Text(
                    text = "$price $currencySymbol",
                    color = BluePrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

            }
        }
    }
}