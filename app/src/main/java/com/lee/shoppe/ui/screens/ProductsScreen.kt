package com.lee.shoppe.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.lee.shoppe.R
import com.lee.shoppe.data.model.CustomerData
import com.lee.shoppe.data.model.Product
import com.lee.shoppe.data.model.ProductResponse
import com.lee.shoppe.data.network.networking.NetworkState
import com.lee.shoppe.ui.viewmodel.ProductsViewModel
import com.lee.shoppe.ui.viewmodel.FavViewModel
import kotlin.random.Random
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.background
import okhttp3.internal.wait
import kotlinx.coroutines.launch
import retrofit2.HttpException

@Composable
fun ProductsScreen(
    brandTitle: String?,
    navController: NavController,
    viewModel: ProductsViewModel = hiltViewModel(),
    favViewModel: FavViewModel = hiltViewModel()
) {
    val productsState by viewModel.products.collectAsState()
    val favState by favViewModel.product.collectAsState()
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
    var showFilterDialog by remember { mutableStateOf(false) }
    val customerData = CustomerData.getInstance(context)
    
    // State to track favorite status for each product
    var favoriteStates by remember { mutableStateOf(mutableMapOf<Long, Boolean>()) }
    
    // Force recomposition when favorite states change
    val favoriteStatesSnapshot by remember { derivedStateOf { favoriteStates.toMap() } }

    var showRemoveDialog by remember { mutableStateOf(false) }
    var productToRemove by remember { mutableStateOf<Product?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Show Snackbar for HTTP 429 or too many requests
    LaunchedEffect(productsState) {
        if (productsState is NetworkState.Failure) {
            val error = (productsState as NetworkState.Failure).error
            val message = if (error.message?.contains("Too many requests") == true || (error is retrofit2.HttpException && error.code() == 429)) {
                "You're making requests too quickly. Please wait a moment and try again."
            } else {
                error.message ?: "An error occurred. Please try again."
            }
            coroutineScope.launch {
                snackbarHostState.showSnackbar(message)
            }
        }
    }

    LaunchedEffect(brandTitle) {
        viewModel.getProducts(brandTitle ?: "")
    }
    
    // Listen to FavViewModel state changes
    LaunchedEffect(favState) {
        when (favState) {
            is NetworkState.Success -> {
                println("FavViewModel operation completed successfully")
                // Optionally show success message
                android.widget.Toast.makeText(context, "Favorite updated successfully", android.widget.Toast.LENGTH_SHORT).show()
            }
            is NetworkState.Failure -> {
                println("FavViewModel operation failed: ${(favState as NetworkState.Failure).error.message}")
                // Show error message
                android.widget.Toast.makeText(context, "Failed to update favorite: ${(favState as NetworkState.Failure).error.message}", android.widget.Toast.LENGTH_SHORT).show()
            }
            else -> {}
        }
    }
    
    // Function to check if a product is favorite
    fun checkFavoriteStatus(productId: Long) {
        if (customerData.isLogged) {
            if (customerData.favListId > 0) {
                favViewModel.isFavProduct(
                    id = productId,
                    listId = customerData.favListId,
                    favTrue = {
                        println("Product $productId is favorite")
                        favoriteStates = favoriteStates.toMutableMap().apply { put(productId, true) }
                    },
                    favFalse = {
                        println("Product $productId is not favorite")
                        favoriteStates = favoriteStates.toMutableMap().apply { put(productId, false) }
                    }
                )
            } else {
                println("favListId not set yet, defaulting to not favorite for product $productId")
                favoriteStates = favoriteStates.toMutableMap().apply { put(productId, false) }
            }
        }
    }
    
    // Function to toggle favorite status
    fun toggleFavorite(product: Product) {
        val productId = product.id ?: 0
        val currentState = favoriteStates[productId] ?: false
        
        println("Toggle favorite called for product $productId, current state: $currentState, isLogged: ${customerData.isLogged}, favListId: ${customerData.favListId}")
        
        if (customerData.isLogged) {
            if (currentState) {
                // Remove from favorites: show dialog
                productToRemove = product
                showRemoveDialog = true
            } else {
                // Add to favorites
                println("Adding product $productId to favorites")
                favViewModel.insertFavProduct(
                    product = product, 
                    listId = customerData.favListId,
                    onFavListCreated = { newFavListId ->
                        println("New favorites list created with ID: $newFavListId")
                        customerData.favListId = newFavListId
                        // Update the favorite state
                        favoriteStates = favoriteStates.toMutableMap().apply { put(productId, true) }
                    }
                )
                favoriteStates = favoriteStates.toMutableMap().apply { put(productId, true) }
            }
        } else {
            // Show login prompt or navigate to login
            println("User not logged in, cannot toggle favorite")
            // You can implement this based on your app's requirements
        }
    }

    // Remove Scaffold and TopAppBar, add custom header Row at the top of the Column
    Column(
        modifier = Modifier
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
                text = brandTitle?.let { "Products for $it" } ?: "Products",
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { showFilterDialog = true }) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = "Filter",
                    tint = Color.Black
                )
            }
        }

            // Search Bar
            SearchBar(
                searchQuery = searchQuery,
                onSearchQueryChange = { 
                    searchQuery = it
                    viewModel.emitSearch(it.text.lowercase())
                }
            )

            // Products Grid
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                when (productsState) {
                    is NetworkState.Loading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = Color(0xFF2196F3)
                        )
                    }
                    is NetworkState.Success -> {
                        val productResponse = (productsState as NetworkState.Success<ProductResponse>).data
                        val products = productResponse.products ?: emptyList()
                        
                        Column {
                            // Check favorite status for all products when they are loaded
                            LaunchedEffect(products) {
                                if (customerData.isLogged) {
                                    products.forEach { product ->
                                        product.id?.let { checkFavoriteStatus(it) }
                                    }
                                }
                            }
                            
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(products) { product ->
                                    val productId = product.id ?: 0
                                    val isFav = favoriteStatesSnapshot[productId] ?: false
                                    
                                    println("Rendering product $productId with favorite state: $isFav")
                                    
                                    ProductCard(
                                        product = product,
                                        onFavoriteClick = { 
                                            println("Favorite button clicked for product $productId, current state: $isFav")
                                            toggleFavorite(product)
                                        },
                                        onCardClick = { 
                                            // Navigate to product details
                                            navController.navigate("product_details/${product.id}")
                                        },
                                        isFavorite = isFav
                                    )
                                }
                            }
                        }
                    }
                    is NetworkState.Failure -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Failed to load products",
                                color = Color.Black,
                                fontSize = 18.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { viewModel.getProducts(brandTitle ?: "") }
                            ) {
                                Text("Retry")
                            }
                        }
                    }

                    NetworkState.Idle -> TODO()
                }
            }
        }

    // Filter Dialog
    if (showFilterDialog) {
        FilterDialog(
            onDismiss = { showFilterDialog = false },
            onFilterApply = { fromPrice, toPrice ->
                viewModel.filterProductsByPrice(fromPrice, toPrice)
                showFilterDialog = false
            }
        )
    }

    if (showRemoveDialog && productToRemove != null) {
        DeleteCartDialog(
            show = true,
            title = "Remove from Favorites",
            subtitle = "Are you sure you want to remove this product from your favorites?",
            confirmText = "Remove",
            onCancel = { showRemoveDialog = false; productToRemove = null },
            onConfirm = {
                productToRemove?.let { product ->
                    favViewModel.deleteFavProduct(product.id ?: 0, customerData.favListId)
                    favoriteStates = favoriteStates.toMutableMap().apply { put(product.id ?: 0, false) }
                }
                showRemoveDialog = false
                productToRemove = null
            }
        )
    }
}


@Composable
fun SearchBar(
    searchQuery: TextFieldValue,
    onSearchQueryChange: (TextFieldValue) -> Unit
) {
    val primaryColor = Color(0xFF0057FF)
    val shape = RoundedCornerShape(20.dp)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        shape = shape,
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F8F8))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = Color.Gray,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(modifier = Modifier.weight(1f)) {
                BasicTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        color = Color.Black,
                        fontSize = 16.sp
                    ),
                    singleLine = true,
                    decorationBox = { innerTextField ->
                        if (searchQuery.text.isEmpty()) {
                            Text(
                                text = "Search products...",
                                color = Color.Gray,
                                fontSize = 16.sp
                            )
                        }
                        innerTextField()
                    }
                )
            }
            if (searchQuery.text.isNotEmpty()) {
                IconButton(
                    onClick = { onSearchQueryChange(TextFieldValue("")) },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear search",
                        tint = primaryColor
                    )
                }
            }
        }
    }
}

@Composable
fun FilterDialog(
    onDismiss: () -> Unit,
    onFilterApply: (Float?, Float?) -> Unit
) {
    var fromPrice by remember { mutableStateOf("") }
    var toPrice by remember { mutableStateOf("") }
    val primaryColor = Color(0xFF0057FF)
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {

            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "Filter by Price",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor
                )
                Spacer(modifier = Modifier.height(20.dp))
                OutlinedTextField(
                    value = fromPrice,
                    onValueChange = { fromPrice = it },
                    label = { Text("From Price") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    ),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = toPrice,
                    onValueChange = { toPrice = it },
                    label = { Text("To Price") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    ),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        border = BorderStroke(1.dp, primaryColor),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel", color = primaryColor, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            val from = fromPrice.toFloatOrNull()
                            val to = toPrice.toFloatOrNull()
                            onFilterApply(from, to)
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                    ) {
                        Text("Apply", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ProductCard(
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
                    .height(180.dp)
            ) {
                AsyncImage(
                    model = product.image?.src,
                    contentDescription = product.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    error = painterResource(id = R.drawable.broken_image)
                )

                // Favorite button - always show but handle login state
                IconButton(
                    onClick = onFavoriteClick,
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

            // Product details
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                // Title
                product.title?.let {
                    Text(
                        text = it,
                        color = Color.Black,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Rating
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val randomRatings = remember { FloatArray(10) { Random.nextFloat() * 5 } }
                    val randomRating = remember { randomRatings.random() }
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

                Spacer(modifier = Modifier.height(8.dp))

                // Price section
                val currency = CustomerData.getInstance(LocalContext.current).currency
                val originalPrice = product.variants?.get(0)?.price?.toDoubleOrNull() ?: 0.0
                val conversionRate: Double = 1.0 // Default rate

                // Determine the final display price
                val displayPrice = if (currency == "USD") {
                    // Convert to USD using rate
                    val converted = originalPrice / conversionRate
                    String.format("%.2f", converted)
                } else {
                    product.variants?.get(0)?.price ?: "0.0"
                }

                // Get the appropriate currency symbol
                val currencySymbol = when (currency) {
                    "USD" -> "$"
                    "EGY" -> "EGP"
                    "EUR" -> "€"
                    "GBP" -> "£"
                    else -> currency
                }

                Row(
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = "$displayPrice $currencySymbol",
                        color = Color.Red,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                /**
                 * //Old price and discount (if available)
                 *                 if (product.variants?.get(0)?.price != null && product.variants. != null) {
                 *                     Row(
                 *                         verticalAlignment = Alignment.CenterVertically
                 *                     ) {
                 *                         Text(
                 *                             text = "$${product.oldPrice}",
                 *                             color = Color.Gray,
                 *                             fontSize = 14.sp,
                 *                             modifier = Modifier.padding(end = 8.dp)
                 *                         )
                 *                         Text(
                 *                             text = "${product.discountPercentage}% off",
                 *                             color = Color.Red,
                 *                             fontSize = 16.sp,
                 *                             fontWeight = FontWeight.Bold
                 *                         )
                 *                     }
                 *                 }
                 *
                 */

            }
        }
    }
}