package com.lee.shoppe.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import android.util.Log
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.lee.shoppe.R
import com.lee.shoppe.data.model.CustomerData
import com.lee.shoppe.data.model.Product
import com.lee.shoppe.data.model.ProductImage
import com.lee.shoppe.data.model.Variant
import com.lee.shoppe.data.network.networking.NetworkState
import com.lee.shoppe.ui.components.LoadingWithMessages
import com.lee.shoppe.ui.components.animations.StaggeredAnimatedItem
import com.lee.shoppe.ui.screens.dialogBox.EmptyState
import com.lee.shoppe.ui.theme.*
import com.lee.shoppe.ui.utils.isNetworkConnected
import com.lee.shoppe.ui.viewmodel.FavViewModel
import kotlinx.coroutines.launch

@Composable
fun FavoriteHeader(
    favoriteItemCount: Int,
) {
    Surface(
        shadowElevation = 4.dp,
        color = Color.White,
        shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Spacer(modifier = Modifier.width(8.dp))

            // Title and Quantity
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = stringResource(R.string.favorites),
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = HeaderColor
                )
                Spacer(modifier = Modifier.width(8.dp))
                if (favoriteItemCount > 0) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(BlueLight, shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = favoriteItemCount.toString(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = HeaderColor
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
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
    var isRefreshing by remember { mutableStateOf(false) }
    
    // Pull to refresh state
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = {
            if (isNetworkConnected && (customerData.isLogged || customerData.isGuestWithPreservedData) && customerData.favListId > 0) {
                isRefreshing = true
                favViewModel.getFavProducts(customerData.favListId, forceRefresh = true)
            } else {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        message = context.getString(R.string.no_internet_connection),
                        duration = SnackbarDuration.Short
                    )
                }
            }
        }
    )

    // Collect state from ViewModel
    val favProductsState by favViewModel.product.collectAsState()
    val lottieComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.favorite_empty))

    // Load favorites when screen is first shown or when favListId changes
    LaunchedEffect(Unit, customerData.favListId) {
        if (isNetworkConnected && (customerData.isLogged || customerData.isGuestWithPreservedData)) {
            if (customerData.favListId > 0) {
                Log.d("FavoriteScreen", "Loading favorites for list ID: ${customerData.favListId}")
                favViewModel.getFavProducts(customerData.favListId, forceRefresh = false)
            } else {
                // Create a new favorites list if one doesn't exist
                Log.d("FavoriteScreen", "No favorites list found, creating a new one...")
                favViewModel.checkAndCreateFavListIfNeeded { newFavListId ->
                    Log.d("FavoriteScreen", "New favorites list created with ID: $newFavListId")
                    customerData.favListId = newFavListId
                    // Load the new favorites list
                    favViewModel.getFavProducts(newFavListId, forceRefresh = false)
                }
            }
        } else {
            Log.e("FavoriteScreen", "Cannot load favorites. isNetworkConnected: $isNetworkConnected, isLogged: ${customerData.isLogged}, isGuestWithPreservedData: ${customerData.isGuestWithPreservedData}, favListId: ${customerData.favListId}")
            favoriteProducts = emptyList()
        }
    }

    // Process state changes
    LaunchedEffect(favProductsState) {
        Log.d("FavoriteScreen", "favProductsState changed: ${favProductsState.javaClass.simpleName}")

        when (val state = favProductsState) {
            is NetworkState.Success -> {
                Log.d("FavoriteScreen", "Successfully loaded favorites. Items: ${state.data.draft_order.line_items.size}")

                val products = state.data.draft_order.line_items
                    .drop(1) // Skip the first dummy item
                    .mapNotNull { lineItem ->
                        try {
                            lineItem.sku?.split("*")?.takeIf { it.size >= 2 }?.let { parts ->
                                val productId = parts[0].toLongOrNull()
                                Log.d("FavoriteScreen", "Processing product: $productId, SKU: ${lineItem.sku}")

                                productId?.let { id ->
                                    Product(
                                        id = id,
                                        title = lineItem.title ?: "Product",
                                        image = ProductImage(src = parts[1]),
                                        variants = listOf(
                                            Variant(price = lineItem.price ?: "0.0")
                                        )
                                    )
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("FavoriteScreen", "Error processing product: ${e.message}")
                            null
                        }
                    }

                Log.d("FavoriteScreen", "Successfully processed ${products.size} favorite products")
                favoriteProducts = products
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
            FavoriteHeader(favoriteProducts.size)
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .pullRefresh(pullRefreshState)
        ) {
            // Pull to refresh indicator
            PullRefreshIndicator(
                refreshing = isRefreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
                backgroundColor = Color.White,
                contentColor = BluePrimary
            )
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
                is NetworkState.Failure -> {
                    isRefreshing = false // Hide refresh indicator on error
                    val error = (favProductsState as NetworkState.Failure).error
                    Log.e("FavoriteScreen", "Error loading favorites: ${error.message}")
                    if (favoriteProducts.isEmpty()) {
                        EmptyState(
                            lottieComposition,
                            stringResource(R.string.error_loading_favorites),
                            stringResource(R.string.please_try_again_later)
                        )
                    }
                }
                NetworkState.Idle -> {
                    if (customerData.favListId <= 0) {
                        EmptyState(
                            lottieComposition,
                            stringResource(R.string.no_favorites_yet),
                            stringResource(R.string.sign_in_to_see_favorites)
                        )
                    } else {
                        // Show loading state when initializing
                        LoadingWithMessages(
                            modifier = Modifier.fillMaxSize(),
                            mainMessage = stringResource(R.string.loading_favorites),
                            secondaryMessage = stringResource(R.string.please_wait_loading_favorites),
                            loadingIndicatorColor = BluePrimary,
                            spacing = 16.dp,
                            messageSpacing = 8.dp
                        )
                    }
                }
                is NetworkState.Success -> {
                    isRefreshing = false // Hide refresh indicator when data is loaded
                    if (favoriteProducts.isEmpty()) {
                        EmptyState(
                            lottieComposition,
                            stringResource(R.string.no_favorites_yet),
                            stringResource(R.string.your_favorites_will_appear_here)
                        )
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(favoriteProducts) { product ->
                                StaggeredAnimatedItem(
                                    index = favoriteProducts.indexOf(product),
                                    delayPerItemMs = 50
                                ) {
                                    ProductCard(
                                        product = product,
                                        onFavoriteClick = {
                                            productToDelete = product
                                            showDeleteDialog = true
                                        },
                                        onCardClick = {
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
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        DeleteCartDialog(
            show = true,
            title = stringResource(R.string.remove_from_favorites),
            subtitle = stringResource(R.string.are_you_sure_remove_favorite),
            confirmText = stringResource(R.string.remove),
            onCancel = { showDeleteDialog = false },
            onConfirm = {
                productToDelete?.let { product ->
                    if (customerData.isLogged || customerData.isGuestWithPreservedData) {
                        favViewModel.deleteFavProduct(
                            id = product.id ?: 0,
                            listId = customerData.favListId
                        )
                        // Remove from local list
                        favoriteProducts = favoriteProducts.filter { it.id != product.id }
                    }
                    showDeleteDialog = false
                    productToDelete = null
                }
            }
        )
    }
}