package com.lee.shoppe.ui.screens

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.lee.shoppe.R
import com.lee.shoppe.data.model.CustomerData
import com.lee.shoppe.data.model.DraftOrderResponse
import com.lee.shoppe.data.model.Product
import com.lee.shoppe.data.model.ProductImage
import com.lee.shoppe.data.model.Variant
import com.lee.shoppe.data.network.networking.NetworkState
import com.lee.shoppe.ui.utils.isNetworkConnected
import com.lee.shoppe.ui.viewmodel.FavViewModel
import kotlin.random.Random
import com.lee.shoppe.ui.screens.dialogBox.NetworkErrorBox
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import kotlinx.coroutines.launch
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.rememberCoroutineScope

@Composable
fun FavoriteScreen(
    navController: NavController,
    favViewModel: FavViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val customerData = CustomerData.getInstance(context)
    val isNetworkConnected = isNetworkConnected(context)
    
    val favProductsState by favViewModel.product.collectAsState()

    
    // State to track favorite status for each product
    val favoriteStates by remember { mutableStateOf(mutableMapOf<Long, Boolean>()) }
    
    // List to store favorite products
    var favoriteProducts by remember { mutableStateOf<List<Product>>(emptyList()) }
    
    // State for showing delete confirmation dialog
    var showDeleteDialog by remember { mutableStateOf(false) }
    var productToDelete by remember { mutableStateOf<Product?>(null) }

    // Load favorite products when component mounts
    LaunchedEffect(Unit) {
        if (isNetworkConnected) {
            if (customerData.isLogged) {
                if (customerData.favListId > 0) {
                    favViewModel.getFavProducts(customerData.favListId)
                } else {
                    // Handle case where favListId is not set
                    android.util.Log.w("FavoriteScreen", "favListId is 0 or not set")
                }
            }
        }
    }
    
    // Process favorite products when data is loaded
    LaunchedEffect(favProductsState) {
        android.util.Log.d("FavoriteScreen", "State changed: ${favProductsState::class.simpleName}")
        when (favProductsState) {
            is NetworkState.Loading -> {
                Log.d("FavoriteScreen", "Loading state - clearing products")
                favoriteProducts = emptyList()
            }
            is NetworkState.Success -> {
                val draftOrderResponse = (favProductsState as NetworkState.Success<DraftOrderResponse>).data
                val lineItems = draftOrderResponse.draft_order.line_items
                Log.d("FavoriteScreen", "Success: lineItems count = ${lineItems.size}")
                
                val products = mutableListOf<Product>()
                lineItems.drop(1).forEach { lineItem ->
                    Log.d("FavoriteScreen", "Processing lineItem: ${lineItem.sku}")
                    if (lineItem.sku != null && lineItem.sku != "dummy") {
                        val skuParts = lineItem.sku.split("*")
                        Log.d("FavoriteScreen", "SKU parts: $skuParts")
                        if (skuParts.size >= 2) {
                            val productId = skuParts[0].toLongOrNull()
                            val imageSrc = skuParts[1]
                            
                            if (productId != null) {
                                val product = Product(
                                    id = productId,
                                    title = lineItem.title ?: "Product",
                                    image = ProductImage(src = imageSrc),
                                    variants = listOf(
                                        Variant(
                                            price = lineItem.price ?: "0.0"
                                        )
                                    )
                                )
                                products.add(product)
                                favoriteStates[productId] = true
                                Log.d("FavoriteScreen", "Added product: ${product.title}")
                            }
                        }
                    }
                }
                favoriteProducts = products
                Log.d("FavoriteScreen", "Final processed ${products.size} favorite products")
            }
            is NetworkState.Failure -> {
                Log.e("FavoriteScreen", "Failure: ${(favProductsState as NetworkState.Failure).error.message}")
                favoriteProducts = emptyList()
            }

            NetworkState.Idle -> { /* No content */ }
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Show Snackbar for HTTP 429 or too many requests
    LaunchedEffect(favProductsState) {
        if (favProductsState is NetworkState.Failure) {
            val error = (favProductsState as NetworkState.Failure).error
            val message = if (error.message?.contains("Too many requests") == true || (error is retrofit2.HttpException && error.code() == 429)) {
                "You're making requests too quickly. Please wait a moment and try again."
            } else {
                error.message ?: "Failed to load favorites."
            }
            coroutineScope.launch {
                snackbarHostState.showSnackbar(message)
            }
        }
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
                    .background(Color.White)
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.Black
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "My Favorites",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    modifier = Modifier.weight(1f)
                )
            }
            //Spacer(modifier = Modifier.height(8.dp))
            when {
                // Network not connected
                !isNetworkConnected -> {
                    NetworkErrorBox(show = true)
                }
                
                // User not logged in
                !customerData.isLogged -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FavoriteBorder,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Login to View Favorites",
                            color = Color.Black,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Please login to see your favorite products",
                            color = Color.Gray,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = {
                                // Navigate to login screen
                                navController.navigate("login")
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2196F3)
                            ),
                            modifier = Modifier
                                .height(48.dp)
                                .width(200.dp)
                        ) {
                            Text(
                                text = "Login",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                
                // Loading state
                favProductsState is NetworkState.Loading -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            color = Color(0xFF2196F3)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Loading favorites...",
                            color = Color.Black,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "favListId: ${customerData.favListId}",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                }
                
                // Success state
                favProductsState is NetworkState.Success -> {
                    val draftOrderResponse = (favProductsState as NetworkState.Success<DraftOrderResponse>).data
                    val lineItems = draftOrderResponse.draft_order.line_items
                    
                    // Check if favListId is valid
                    if (customerData.favListId <= 0) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FavoriteBorder,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Favorites List Not Set",
                                color = Color.Black,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Please try adding some products to favorites first",
                                color = Color.Gray,
                                fontSize = 16.sp,
                                modifier = Modifier.padding(horizontal = 32.dp)
                            )
                        }
                    } else {
                        if (favoriteProducts.isEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(50), // Oval shape
                                    color = Color.White,        // Gray background
                                    tonalElevation = 2.dp,          // Elevation (for Material 3)
                                    shadowElevation = 2.dp,         // Optional: for Material 2
                                    modifier = Modifier.size(100.dp) // Size of the oval container
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.favorites_empty),
                                        contentDescription = null,
                                        contentScale = ContentScale.Fit, // Adjust depending on your image type
                                        modifier = Modifier
                                            .padding(20.dp)
                                            .fillMaxSize()
                                    )
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "No favorite products yet",
                                    color = Color.Black,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center

                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Start adding products to your favorites!",
                                    color = Color.Gray,
                                    fontSize = 16.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 32.dp)
                                )
                            }
                        } else {
                            Column {
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(2),
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(favoriteProducts) { product ->
                                        FavoriteProductCard(
                                            product = product,
                                            onFavoriteClick = { 
                                                // Show delete confirmation dialog
                                                productToDelete = product
                                                showDeleteDialog = true
                                            },
                                            onCardClick = { 
                                                // Navigate to product details
                                                navController.navigate("product_details/${product.id}")
                                            },
                                            isFavorite = true
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                
                // Failure state
                favProductsState is NetworkState.Failure -> {
                    val error = (favProductsState as NetworkState.Failure).error
                    val errorMessage = when {
                        error.message?.contains("404") == true -> "Favorites list not found. Please try adding some products to favorites first."
                        error.message?.contains("401") == true -> "Authentication failed. Please login again."
                        error.message?.contains("403") == true -> "Access denied. Please check your permissions."
                        else -> error.message ?: "Unknown error occurred"
                    }
                    
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.StarOutline,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.Red
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Failed to load favorites",
                            color = Color.Black,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMessage,
                            color = Color.Gray,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { 
                                if (customerData.isLogged && customerData.favListId > 0) {
                                    favViewModel.getFavProducts(customerData.favListId)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2196F3)
                            ),
                            modifier = Modifier
                                .height(48.dp)
                                .width(200.dp)
                        ) {
                            Text(
                                text = "Retry",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }

    // Delete confirmation dialog
    DeleteCartDialog(
        show = showDeleteDialog && productToDelete != null,
        title = "Remove from Favorites",
        subtitle = "Are you sure you want to remove this product from your favorites?",
        confirmText = "Remove",
        onCancel = { showDeleteDialog = false; productToDelete = null },
        onConfirm = {
            productToDelete?.let { product ->
                favViewModel.deleteFavProduct(product.id ?: 0, customerData.favListId)
                favoriteProducts = favoriteProducts.filter { it.id != product.id }
                favoriteStates[product.id ?: 0] = false
            }
            showDeleteDialog = false
            productToDelete = null
        }
    )
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
            .fillMaxWidth()
            .clickable { onCardClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column {
            // Product Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            ) {
                AsyncImage(
                    model = product.image?.src,
                    contentDescription = product.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    error = painterResource(id = R.drawable.broken_image)
                )
                
                // Favorite Button
                IconButton(
                    onClick = { onFavoriteClick() },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(60.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) Color(0xFF0057FF) else Color.Black,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            
            // Product Details
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                // Title
                Text(
                    text = product.title ?: "Product",
                    color = Color.Black,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Rating
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val randomRating = remember { Random.nextFloat() * 5 }
                    repeat(5) { index ->
                        Icon(
                            imageVector = if (index < randomRating.toInt()) Icons.Default.Star else Icons.Default.StarOutline,
                            contentDescription = null,
                            tint = if (index < randomRating.toInt()) Color(0xFFFFD700) else Color(0xFFE0E0E0),
                            modifier = Modifier.size(12.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "(${String.format("%.1f", randomRating)})",
                        color = Color.Gray,
                        fontSize = 10.sp
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Price
                Text(
                    text = product.variants?.get(0)?.price ?: "0.0",
                    color = Color.Red,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
} 