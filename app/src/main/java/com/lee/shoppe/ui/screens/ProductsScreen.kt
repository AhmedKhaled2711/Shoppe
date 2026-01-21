package com.lee.shoppe.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.lee.shoppe.R
import com.lee.shoppe.data.model.CustomerData
import com.lee.shoppe.data.model.Product
import com.lee.shoppe.data.model.ProductResponse
import com.lee.shoppe.data.network.networking.NetworkState
import com.lee.shoppe.ui.components.ScreenHeader
import com.lee.shoppe.ui.components.GuestUserDialog
import com.lee.shoppe.ui.components.animations.StaggeredAnimatedItem
import com.lee.shoppe.ui.navigation.Screen
import com.lee.shoppe.ui.theme.BluePrimary
import com.lee.shoppe.ui.viewmodel.FavViewModel
import com.lee.shoppe.ui.viewmodel.ProductsViewModel
import kotlinx.coroutines.launch
import kotlin.random.Random

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
    var showGuestDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Show Snackbar for HTTP 429 or too many requests
    LaunchedEffect(productsState) {
        if (productsState is NetworkState.Failure) {
            val error = (productsState as NetworkState.Failure).error
            val message = if (error.message?.contains("Too many requests") == true || (error is retrofit2.HttpException && error.code() == 429)) {
                context.getString(R.string.too_many_requests)
            } else {
                error.message ?: context.getString(R.string.error_occurred)
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
                // Show success message
                android.widget.Toast.makeText(context, context.getString(R.string.favorite_updated), android.widget.Toast.LENGTH_SHORT).show()
            }
            is NetworkState.Failure -> {
                println("FavViewModel operation failed: ${(favState as NetworkState.Failure).error.message}")
                // Show error message
                android.widget.Toast.makeText(context, 
                    context.getString(R.string.favorite_update_failed, (favState as NetworkState.Failure).error.message ?: ""), 
                    android.widget.Toast.LENGTH_SHORT
                ).show()
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
            android.widget.Toast.makeText(context, context.getString(R.string.please_login_first), android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    // Remove Scaffold and TopAppBar, add custom header Row at the top of the Column
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Header
        ScreenHeader(
            title = brandTitle?.let { context.getString(R.string.products_for_brand, it) } ?: context.getString(R.string.products),
            onBackClick = { navController.navigateUp() },
            showBackButton = true
        )

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

                        // Use rememberLazyGridState for better scroll state management
                        val gridState = rememberLazyGridState()

                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(products) { product ->
                                val isFavorite = favoriteStatesSnapshot[product.id] ?: false

                                StaggeredAnimatedItem(
                                    index = products.indexOf(product),
                                    delayPerItemMs = 50
                                ) {
                                    ProductCard(
                                        product = product,
                                        onFavoriteClick = {
                                            if (customerData.isLogged) {
                                                if (isFavorite) {
                                                    productToRemove = product
                                                    showRemoveDialog = true
                                                } else {
                                                    favViewModel.insertFavProduct(product, customerData.favListId)
                                                    favoriteStates = favoriteStates.toMutableMap().apply {
                                                        put(product.id ?: 0L, true)
                                                    }
                                                }
                                            } else {
                                                showGuestDialog = true
                                            }
                                        },
                                        onCardClick = {
                                            navController.navigate(Screen.ProductDetails.route.replace("{productId}", product.id.toString()))
                                        },
                                        isFavorite = isFavorite
                                    )
                                }
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
                            text = context.getString(R.string.failed_to_load_products),
                            color = Color.Black,
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { viewModel.getProducts(brandTitle ?: "") }
                        ) {
                            Text(context.getString(R.string.retry))
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
            title = context.getString(R.string.remove_from_favorites),
            subtitle = context.getString(R.string.confirm_remove_favorite),
            confirmText = context.getString(R.string.remove),
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

    // Guest User Dialog
    if (showGuestDialog) {
        val guestLottieComposition by rememberLottieComposition(
            LottieCompositionSpec.RawRes(R.raw.l)
        )
        GuestUserDialog(
            onDismiss = { showGuestDialog = false },
            onLoginClick = {
                showGuestDialog = false
                navController.navigate("login")
            },
            lottieComposition = guestLottieComposition
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
                contentDescription = stringResource(R.string.search),
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
                                text = stringResource(R.string.search_products),
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
                        contentDescription = stringResource(R.string.clear_search),
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
                    text = stringResource(R.string.price_range),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = BluePrimary
                )

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = fromPrice,
                    onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) fromPrice = it },
                    label = { Text(stringResource(R.string.from)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BluePrimary,
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = BluePrimary,
                        cursorColor = BluePrimary
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = toPrice,
                    onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) toPrice = it },
                    label = { Text(stringResource(R.string.to)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BluePrimary,
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = BluePrimary,
                        cursorColor = BluePrimary
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        border = BorderStroke(1.dp, BluePrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.cancel),
                            color = BluePrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Button(
                        onClick = {
                            val from = fromPrice.toFloatOrNull()
                            val to = toPrice.toFloatOrNull()
                            onFilterApply(from, to)
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                    ) {
                        Text(
                            text = stringResource(R.string.apply_filters),
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
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
    // Memoize expensive calculations
    val productKey = remember(product.id) { "product_${product.id}" }
    val randomRating = remember(productKey) { Random.nextDouble(3.0, 5.0).toFloat() }
    val interactionSource = remember { MutableInteractionSource() }

    // Process product title once
    val displayTitle = remember(product.title) {
        product.title?.let { title ->
            val titleParts = title.split("|")
            val rawTitle = titleParts.getOrNull(1)?.trim() ?: ""
            val wordList = rawTitle.split(" ")
            if (wordList.size > 3) wordList.take(3).joinToString(" ") + "..." else rawTitle
        } ?: ""
    }
    val context = LocalContext.current
    // Process price information once
    val (displayPrice, currencySymbol) = remember(product.variants) {
        val currency = CustomerData.getInstance(context).currency
        val originalPrice = product.variants?.getOrNull(0)?.price?.toDoubleOrNull() ?: 0.0
        val conversionRate = 1.0 // Default rate

        val price = if (currency == "USD") {
            String.format("%.2f", originalPrice / conversionRate)
        } else {
            product.variants?.getOrNull(0)?.price ?: "0.0"
        }

        val symbol = when (currency) {
            "USD" -> "$"
            "EGY" -> "EGP"
            "EUR" -> "€"
            "GBP" -> "£"
            else -> currency
        }

        price to symbol
    }

    Card(
        modifier = Modifier
            .width(190.dp)
            .height(320.dp)
            .clickable(onClick = onCardClick),
        shape = RoundedCornerShape(10.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
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
                // Optimized Coil image loading with better caching and error handling
                val imageUrl = product.image?.src?.let { url ->
                    // Add any necessary URL transformations here
                    if (url.startsWith("//")) "https:$url" else url
                }

                val imagePainter = rememberAsyncImagePainter(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageUrl)
                        .crossfade(true)
                        .diskCacheKey(imageUrl) // Cache by URL
                        .memoryCacheKey(imageUrl) // Cache in memory
                        .build(),
                    contentScale = ContentScale.Crop,
                    onError = { error ->
                        // Log error if needed
                        println("Image loading failed: ${error.result.throwable.message}")
                    },
                    onSuccess = { success ->
                        // Image loaded successfully
                    },
                    placeholder = painterResource(id = R.drawable.broken_image),
                    error = painterResource(id = R.drawable.broken_image)
                )

                Image(
                    painter = imagePainter,
                    contentDescription = product.title ?: "Product image",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )

                // Favorite button with ripple effect
                IconButton(
                    onClick = onFavoriteClick,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(60.dp)
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
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
                // Title - using pre-processed displayTitle
                Text(
                    text = displayTitle,
                    color = Color.Black,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    minLines = 2,
                    modifier = Modifier.heightIn(min = 48.dp)
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Rating - using memoized randomRating
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(5) { index ->
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

                // Price section - using pre-processed displayPrice and currencySymbol
                Row(
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = "$displayPrice $currencySymbol",
                        color = BluePrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}