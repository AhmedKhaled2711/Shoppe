package com.lee.shoppe.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
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
    val lottieComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.favorite_empty))

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
            FavoriteHeader(favoriteProducts.size)
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
                    //EmptyFavoriteState()
                }
                is NetworkState.Success -> {
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