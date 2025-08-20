package com.lee.shoppe.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.airbnb.lottie.LottieComposition
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.lee.shoppe.R
import com.lee.shoppe.data.model.CustomerData
import com.lee.shoppe.data.model.ProductResponse
import com.lee.shoppe.data.network.networking.NetworkState
import com.lee.shoppe.ui.components.LoadingWithMessages
import com.lee.shoppe.ui.components.animations.StaggeredAnimatedItem
import com.lee.shoppe.ui.theme.BlueLight
import com.lee.shoppe.ui.theme.BluePrimary
import com.lee.shoppe.ui.viewmodel.CategoryViewModel
import com.lee.shoppe.ui.viewmodel.FavViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(
    navController: NavController,
    categoryViewModel: CategoryViewModel = hiltViewModel(),
    favViewModel: FavViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val productsState by categoryViewModel.products.collectAsState()
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
    var showFilterSheet by remember { mutableStateOf(false) }
    var mainCategory by remember { mutableStateOf(" ") }
    var subCategory by remember { mutableStateOf("") }
    var favoriteStates by remember { mutableStateOf(mutableMapOf<Long, Boolean>()) }
    val favoriteStatesSnapshot by remember { derivedStateOf { favoriteStates.toMap() } }
    val lottieComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.box))
    val customerData = CustomerData.getInstance(context)
    val sheetState = rememberModalBottomSheetState()
    var showRemoveDialog by remember { mutableStateOf(false) }
    var productToRemove by remember { mutableStateOf<com.lee.shoppe.data.model.Product?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Load products on first composition
    LaunchedEffect(Unit) {
        categoryViewModel.getProducts("")
    }

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

    Box {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            // Main content with horizontal padding
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                SearchBarCategory(
                    searchQuery = searchQuery,
                    onSearchQueryChange = {
                        searchQuery = it
                        categoryViewModel.searchProducts(it.text)
                    },
                    onFilterClick = { showFilterSheet = true }
                )
                Spacer(modifier = Modifier.height(8.dp))
                CategoryToggleRow(
                    selected = mainCategory,
                    onCategorySelected = {
                        mainCategory = it
                        categoryViewModel.filterProducts(mainCategory, subCategory)
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
                // Product Grid or State
                Box(modifier = Modifier.weight(1f)) {
                    when (productsState) {
                        is NetworkState.Loading -> {
                            LoadingWithMessages(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.White),
                                mainMessage = stringResource(R.string.loading_products),
                                secondaryMessage = stringResource(R.string.please_wait),
                                loadingIndicatorColor = BluePrimary
                            )
                        }
                        is NetworkState.Success -> {
                            val products = (productsState as NetworkState.Success<ProductResponse>).data.products ?: emptyList()
                            // Check favorite status for all products when they are loaded
                            LaunchedEffect(products) {
                                if (customerData.isLogged && customerData.favListId > 0) {
                                    products.forEach { product ->
                                        product.id?.let { id ->
                                            favViewModel.isFavProduct(
                                                id = id,
                                                listId = customerData.favListId,
                                                favTrue = {
                                                    favoriteStates = favoriteStates.toMutableMap().apply { put(id, true) }
                                                },
                                                favFalse = {
                                                    favoriteStates = favoriteStates.toMutableMap().apply { put(id, false) }
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                            if (products.isEmpty()) {
                                EmptyStateLottie(lottieComposition)
                            } else {
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(2),
                                    contentPadding = PaddingValues(8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(products, key = { it.id ?: 0L }) { product ->
                                        val productId = product.id ?: 0L
                                        val isFav = favoriteStatesSnapshot[productId] ?: false
                                        val index = products.indexOfFirst { it.id == productId }
                                        
                                        StaggeredAnimatedItem(index = index) {
                                            ProductCard(
                                                product = product,
                                                onFavoriteClick = {
                                                    if (customerData.isLogged) {
                                                        if (isFav) {
                                                            productToRemove = product
                                                            showRemoveDialog = true
                                                        } else {
                                                            favViewModel.insertFavProduct(product, customerData.favListId)
                                                            favoriteStates = favoriteStates.toMutableMap().apply { put(productId, true) }
                                                        }
                                                    }
                                                },
                                                onCardClick = {
                                                    navController.navigate("product_details/${product.id}")
                                                },
                                                isFavorite = isFav,
                                               // modifier = Modifier
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        is NetworkState.Failure -> {
                            EmptyStateLottie(lottieComposition)
                        }
                        else -> {}
                    }
                }
            }
        }
        // Bottom Sheet Filter
        if (showFilterSheet) {
            ModalBottomSheet(
                onDismissRequest = { showFilterSheet = false },
                sheetState = sheetState,
                containerColor = Color.White
            ) {
                FilterSheetContent(
                    mainCategory = mainCategory,
                    subCategory = subCategory,
                    onCategoryChange = { mainCategory = it },
                    onSubCategoryChange = { subCategory = it },
                    onApply = { from, to ->
                        categoryViewModel.filterProducts(mainCategory, subCategory)
                        categoryViewModel.filterProductsByPrice(from, to)
                        showFilterSheet = false
                    },
                    onClose = { showFilterSheet = false }
                )
            }
        }
        // Remove from Favorite Dialog
        DeleteCartDialog(
            show = showRemoveDialog && productToRemove != null,
            title = stringResource(R.string.remove_from_favorites),
            subtitle = stringResource(R.string.confirm_remove_favorite),
            confirmText = stringResource(R.string.remove),
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
fun EmptyStateLottie(lottieComposition: LottieComposition?) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (lottieComposition != null) {
            LottieAnimation(
                composition = lottieComposition,
                iterations = LottieConstants.IterateForever,
                modifier = Modifier.size(200.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.product_not_found),
                color = Color.Black,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.no_matching_products),
                color = Color.Gray,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.adjust_search_filters),
                color = Color.Gray,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun SearchBarCategory(
    searchQuery: TextFieldValue,
    onSearchQueryChange: (TextFieldValue) -> Unit,
    onFilterClick: () -> Unit
) {
    val primaryColor = Color(0xFF0057FF)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(6.dp),
        colors = CardDefaults.cardColors(Color(0xFFF8F8F8))
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
                    textStyle = TextStyle(
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
            IconButton(
                onClick = onFilterClick,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_filter),
                    contentDescription = stringResource(R.string.filter_products),
                    tint = primaryColor
                )
            }
        }
    }
}

@Composable
fun CategoryToggleRow(
    selected: String,
    onCategorySelected: (String) -> Unit
) {
    val categories = listOf("All", "WOMEN", "KID", "MEN", "SALE")
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(categories) { category ->
            val isSelected = selected.equals(category, ignoreCase = true) || (category == "All" && selected == " ")
            Button(
                onClick = { onCategorySelected(if (category == "All") " " else category) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSelected) BluePrimary else Color.White,
                    contentColor = if (isSelected) BlueLight else Color.Black
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.height(38.dp)
            ) {
                Text(
                    text = if (category == stringResource(R.string.all).uppercase()) stringResource(R.string.all) else category,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun FilterSheetContent(
    mainCategory: String,
    subCategory: String,
    onCategoryChange: (String) -> Unit,
    onSubCategoryChange: (String) -> Unit,
    onApply: (Float?, Float?) -> Unit,
    onClose: () -> Unit
) {
    var fromPrice by remember { mutableStateOf("") }
    var toPrice by remember { mutableStateOf("") }
    val categories = listOf("All", "WOMEN", "KID", "MEN", "SALE")
    val subCategories = listOf(
        "",
        "SHOES",
        "ACCESSORIES",
        "T-SHIRTS"
    )
    val primaryColor = Color(0xFF0057FF)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        // Title
        Text(
            text = stringResource(R.string.filter_products),
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            color = primaryColor,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(20.dp))
        HorizontalDivider(thickness = 1.dp, color = Color(0xFFE0E0E0))
        Spacer(modifier = Modifier.height(16.dp))
        // Category Section
        Text(stringResource(R.string.category), fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Color.Black)
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(categories) { cat ->
                FilterChip(
                    selected = mainCategory == cat || (cat == "All" && mainCategory == " "),
                    onClick = { onCategoryChange(if (cat == "All") " " else cat) },
                    label = {
                        Text(
                            if (cat == stringResource(R.string.all).uppercase()) stringResource(R.string.all) else cat,
                            color = if (mainCategory == cat || (cat == stringResource(R.string.all).uppercase() && mainCategory == " ")) Color.White else Color.Black
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = primaryColor,
                        selectedLabelColor = Color.White,
                        containerColor = Color(0xFFF5F6FA),
                        labelColor = Color.Black
                    )
                )
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        HorizontalDivider(thickness = 1.dp, color = Color(0xFFE0E0E0))
        Spacer(modifier = Modifier.height(16.dp))
        // Subcategory Section
        Text(stringResource(R.string.subcategory), fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Color.Black)
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(subCategories) { sub ->
                FilterChip(
                    selected = subCategory == sub,
                    onClick = { onSubCategoryChange(sub) },
                    label = {
                        Text(sub.ifBlank { stringResource(R.string.all) }, color = if (subCategory == sub) Color.White else Color.Black)
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = primaryColor,
                        selectedLabelColor = Color.White,
                        containerColor = Color(0xFFF5F6FA),
                        labelColor = Color.Black
                    )
                )
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        HorizontalDivider(thickness = 1.dp, color = Color(0xFFE0E0E0))
        Spacer(modifier = Modifier.height(16.dp))
        // Price Range Section
        Text(stringResource(R.string.price_range), fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Color.Black)
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = fromPrice,
                onValueChange = { fromPrice = it },
                label = { Text(stringResource(R.string.from), color = Color.Gray) },
                modifier = Modifier.weight(1f),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                ),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = primaryColor,
                    unfocusedBorderColor = Color(0xFFE0E0E0)
                )
            )
            OutlinedTextField(
                value = toPrice,
                onValueChange = { toPrice = it },
                label = { Text(stringResource(R.string.to), color = Color.Gray) },
                modifier = Modifier.weight(1f),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                ),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = primaryColor,
                    unfocusedBorderColor = Color(0xFFE0E0E0)
                )
            )
        }
        Spacer(modifier = Modifier.height(32.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            OutlinedButton(
                onClick = onClose,
                border = BorderStroke(1.dp, primaryColor),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = primaryColor)
            ) {
                Text(stringResource(R.string.cancel), color = primaryColor, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Button(
                onClick = {
                    val from = fromPrice.toFloatOrNull()
                    val to = toPrice.toFloatOrNull()
                    onApply(from, to)
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
            ) {
                Text(stringResource(R.string.apply), color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}