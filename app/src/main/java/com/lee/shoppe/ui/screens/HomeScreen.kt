package com.lee.shoppe.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.rememberPagerState
import com.lee.shoppe.R
import com.lee.shoppe.data.model.BrandResponse
import com.lee.shoppe.data.model.PriceRule
import com.lee.shoppe.data.model.PriceRuleX
import com.lee.shoppe.data.model.Product
import com.lee.shoppe.data.model.SmartCollection
import com.lee.shoppe.data.network.networking.NetworkState
import com.lee.shoppe.ui.components.LoadingWithMessages
import com.lee.shoppe.ui.screens.dialogBox.NetworkErrorBox
import com.lee.shoppe.ui.utils.isNetworkConnected
import com.lee.shoppe.ui.viewmodel.CategoryViewModel
import com.lee.shoppe.ui.viewmodel.HomeViewModel
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel(),
    categoryViewModel: CategoryViewModel = hiltViewModel(),
    ) {
    val brandsState by viewModel.brands.collectAsState()
    val priceRulesState by viewModel.priceRules.collectAsState()
    val scaffoldState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf<Pair<String, String>?>(null) }
    var isOffline by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
    val focusManager = LocalFocusManager.current
    
    // Observe products from CategoryViewModel
    val productsState by categoryViewModel.products.collectAsState()
    val products = when (val state = productsState) {
        is NetworkState.Success -> state.data.products?.take(20) ?: emptyList() // Take first 20 products
        else -> emptyList()
    }

    LaunchedEffect(Unit) {
        if (!isNetworkConnected(context)) {
            isOffline = true
        } else {
            isOffline = false
            viewModel.fetchDataIfNeeded()
        }
    }

    // Load products on first composition
    LaunchedEffect(Unit) {
        categoryViewModel.getProducts("")
    }

    val isLoading = brandsState is NetworkState.Loading || priceRulesState is NetworkState.Loading
    val showEmpty = brandsState is NetworkState.Failure && priceRulesState is NetworkState.Failure

    val brands = if (brandsState is NetworkState.Success) {
        (brandsState as NetworkState.Success<BrandResponse>).data.smart_collections ?: emptyList()
    } else {
        emptyList()
    }
    val sliderData = if (priceRulesState is NetworkState.Success) {
        (priceRulesState as NetworkState.Success<PriceRule>).data.price_rules
    } else {
        emptyList()
    }

    // Error dialog logic
    LaunchedEffect(brandsState) {
        if (brandsState is NetworkState.Failure) {
            val error = (brandsState as NetworkState.Failure).error
            val message = if (error.message?.contains("Too many requests") == true || (error is retrofit2.HttpException && error.code() == 429)) {
                context.getString(R.string.too_many_requests)
            } else {
                context.getString(R.string.failed_load_data)
            }
            coroutineScope.launch {
                scaffoldState.showSnackbar(message)
            }
        }
    }

    if (isOffline) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            NetworkErrorBox(show = true)
        }
    }
    else {
        Scaffold(
            snackbarHost = { SnackbarHost(scaffoldState) },
            containerColor = Color.White
        ) { _ ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
            ) {
                if (isLoading) {
                    LoadingWithMessages(
                        modifier = Modifier.fillMaxSize(),
                        mainMessage = stringResource(R.string.loading_main_message),
                        secondaryMessage = stringResource(R.string.loading_secondary_message),
                        loadingIndicatorColor = Color(0xFF2196F3)
                    )
                }
                else if (showEmpty) {
                    LoadingWithMessages(
                        modifier = Modifier.fillMaxSize(),
                        mainMessage = stringResource(R.string.network_message_main),
                        secondaryMessage = "${stringResource(R.string.network_message_first)}\n${
                            stringResource(
                                R.string.network_message_second
                            )
                        }",
                        loadingIndicatorColor = Color(0xFF2196F3),
                        spacing = 8.dp,
                        messageSpacing = 4.dp
                    )
                } else {
                    val listState = rememberLazyListState()
                    Box(modifier = Modifier.fillMaxSize()) {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.White)
                        ) {
                            // Header with search bar
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = stringResource(R.string.shoppe),
                                        color = Color(0xFF1A1A1A),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 28.sp,
                                        style = androidx.compose.ui.text.TextStyle(
                                            shadow = androidx.compose.ui.graphics.Shadow(
                                                color = Color(0x1A000000),
                                                offset = androidx.compose.ui.geometry.Offset(1f, 2f),
                                                blurRadius = 4f
                                            )
                                        )
                                    )
                                    /*
                                    Spacer(modifier = Modifier.width(12.dp))
                                    // Professional SearchBar
                                    Card(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp),
                                        shape = RoundedCornerShape(28.dp),
                                        elevation = CardDefaults.cardElevation(
                                            defaultElevation = 2.dp,
                                            pressedElevation = 1.dp,
                                            hoveredElevation = 3.dp,
                                            focusedElevation = 3.dp
                                        ),
                                        colors = CardDefaults.cardColors(
                                            containerColor = Color(0xFFF5F5F7)
                                        )
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(
                                                horizontal = 16.dp,
                                                vertical = 4.dp
                                            )
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
                                                    onValueChange = { searchQuery = it },
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(vertical = 8.dp),
                                                    textStyle = androidx.compose.ui.text.TextStyle(
                                                        color = Color.Black,
                                                        fontSize = 16.sp
                                                    ),
                                                    singleLine = true,
                                                    keyboardActions = KeyboardActions(
                                                        onSearch = {
                                                            focusManager.clearFocus()
                                                            if (searchQuery.text.isNotBlank()) {
                                                                navController.navigate("category?search=" + searchQuery.text)
                                                            }
                                                        }
                                                    ),
                                                    keyboardOptions = KeyboardOptions.Default.copy(
                                                        imeAction = ImeAction.Search
                                                    ),
                                                    decorationBox = { innerTextField ->
                                                        if (searchQuery.text.isEmpty()) {
                                                            Text(
                                                                text = stringResource(R.string.search_placeholder),
                                                                color = Color.Gray,
                                                                fontSize = 16.sp
                                                            )
                                                        }
                                                        innerTextField()
                                                    }
                                                )
                                                // Removed duplicate search bar
                                            }
                                        }
                                    }*/
                                }
                            }
                            // Coupon Slider
                            item {
                                if (priceRulesState is NetworkState.Success && sliderData.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    CouponCardSlider(
                                        priceRules = sliderData,
                                        onDiscountLongPress = { discount ->
                                            clipboardManager.setText(AnnotatedString(discount))
                                            coroutineScope.launch {
                                                scaffoldState.showSnackbar(
                                                    message = context.getString(R.string.copy_discount_code),
                                                    duration = androidx.compose.material3.SnackbarDuration.Short
                                                )
                                            }
                                        }
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }
                            // Brands Section
                            item {
                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 8.dp)
                                    ) {
                                        Text(
                                            text = stringResource(R.string.brands),
                                            color = Color(0xFF1A1A1A),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 28.sp,
                                            modifier = Modifier.weight(1f)
                                        )
                                        /*
                                        if (brands.isNotEmpty()) {
                                            Text(
                                                text = "${brands.size} brands",
                                                color = Color(0xFF8E8E93),
                                                fontSize = 14.sp
                                            )
                                        }*/
                                    }

                                    if (brandsState is NetworkState.Success) {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        BrandGrid(
                                            brands = brands,
                                            onBrandClick = { brand ->
                                                coroutineScope.launch {
                                                    scaffoldState.showSnackbar(
                                                        context.getString(R.string.clicked_on) + " ${brand.title}"
                                                    )
                                                }
                                                navController.navigate("products/${brand.title ?: "Default Title"}")
                                            }
                                        )
                                    }
                                }
                            }

                            // New Items Section
                            item {
                                if (products.isNotEmpty()) {
                                    Column {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                        ) {
                                            Text(
                                                text = stringResource(R.string.new_items),
                                                color = Color(0xFF1A1A1A),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 28.sp,
                                                modifier = Modifier.weight(1f)
                                            )
//                                            Text(
//                                                text = "${products.size} items",
//                                                color = Color(0xFF8E8E93),
//                                                fontSize = 14.sp
//                                            )
                                        }

                                        LazyRow(
                                            contentPadding = PaddingValues(
                                                horizontal = 16.dp,
                                                vertical = 16.dp
                                            ),
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            items(products) { product ->
                                                ProductCard(
                                                    product = product,
                                                    onClick = {
                                                        navController.navigate("product_details/${product.id}")
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Error Dialog
                if (showDialog != null) {
                    AlertDialog(
                        onDismissRequest = { showDialog = null },
                        title = { Text(showDialog?.first ?: "") },
                        text = { Text(showDialog?.second ?: "") },
                        confirmButton = {
                            TextButton(onClick = { showDialog = null }) {
                                Text(stringResource(R.string.cancel))
                            }
                        }
                    )
                }
            }
        }
    }
}


@Composable
private fun ProductCard(
    product: Product,
    onClick: () -> Unit
) {
    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    
    Card(
        onClick = onClick,
        modifier = Modifier
            .width(160.dp)
            .height(240.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 1.dp,
            hoveredElevation = 4.dp,
            focusedElevation = 4.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        interactionSource = interactionSource
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Product Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color(0xFFF9F9F9))
            ) {
                AsyncImage(
                    model = product.image?.src,
                    contentDescription = product.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            // Product Details
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                // Product Title
                Text(
                    text = product.title ?: "",
                    color = Color(0xFF1A1A1A),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    minLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp,
                    modifier = Modifier.height(36.dp)
                )
                
                Spacer(modifier = Modifier.height(6.dp))
                
                // Price
                val price = product.variants?.firstOrNull()?.price ?: "0.00"
                Text(
                    text = "$$price",
                    color = Color(0xFF0066FF),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    style = androidx.compose.ui.text.TextStyle(
                        shadow = androidx.compose.ui.graphics.Shadow(
                            color = Color(0x1A0066FF),
                            offset = androidx.compose.ui.geometry.Offset(0f, 1f),
                            blurRadius = 2f
                        )
                    )
                )
            }
        }
    }
}


@Composable
fun BrandGrid(
    brands: List<SmartCollection>,
    onBrandClick: (SmartCollection) -> Unit
) {
    // Take only first 12 brands
    val displayedBrands = brands.take(12)
    val itemSize = 72.dp
    val spacing = 12.dp

    // Calculate rows needed (3 rows for 12 items in 4 columns)
    val rows = (displayedBrands.size + 3) / 4

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Each row will have 4 items
        repeat(rows) { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(spacing)
            ) {
                // 4 items per row
                for (col in 0 until 4) {
                    val index = row * 4 + col
                    if (index < displayedBrands.size) {
                        val brand = displayedBrands[index]
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(4.dp)
                        ) {
                            val interactionSource =
                                remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier
                                        .size(itemSize)
                                        .combinedClickable(
                                            interactionSource = interactionSource,
                                            indication = androidx.compose.foundation.LocalIndication.current,
                                            onClick = { onBrandClick(brand) }
                                        ),
                                    elevation = CardDefaults.cardElevation(
                                        defaultElevation = 2.dp,
                                        pressedElevation = 1.dp,
                                        hoveredElevation = 3.dp
                                    )
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.fillMaxSize()
                                            .background(Color.White)
                                    ) {
                                        val imageUrl = brand.image?.src
                                        if (!imageUrl.isNullOrEmpty()) {
                                            AsyncImage(
                                                model = imageUrl,
                                                contentDescription = brand.title,
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .padding(12.dp),
                                                contentScale = ContentScale.Fit
                                            )
                                        } else {
                                            // Initial letter fallback
                                            Text(
                                                text = brand.title?.take(1)?.uppercase() ?: "",
                                                color = Color(0xFF666666),
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = brand.title ?: "",
                                    color = Color(0xFF333333),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth(0.9f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CouponCardSlider(
    priceRules: List<PriceRuleX>,
    onDiscountLongPress: (String) -> Unit
) {
    val pagerState = rememberPagerState()

    val couponImages = listOf(
        R.drawable.fiveoff,
        R.drawable.ten,
        R.drawable.twentyfive,
        R.drawable.fivety,
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Card(
            shape = RoundedCornerShape(15.dp),
            elevation = CardDefaults.cardElevation(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            HorizontalPager(
                count = priceRules.size,
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val priceRule = priceRules[page]

                val imageResId = couponImages[page % couponImages.size]

                Image(
                    painter = painterResource(id = imageResId),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .combinedClickable(
                            onClick = {},
                            onLongClick = {
                                onDiscountLongPress(priceRule.title)
                            }
                        ),
                    contentScale = ContentScale.Crop
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        CustomPagerIndicator(
            pagerState = pagerState,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

@Composable
fun CustomPagerIndicator(
    pagerState: PagerState,
    modifier: Modifier = Modifier,
    activeColor: Color = Color(0xFF0042E0),
    inactiveColor: Color = Color(0xFFC7D6FB),
    activeWidth: Dp = 40.dp,       // Increased from 24.dp → 32.dp
    inactiveWidth: Dp = 12.dp,     // Increased from 8.dp → 12.dp
    height: Dp = 12.dp,            // Increased from 8.dp → 12.dp
    spacing: Dp = 12.dp             // Slightly more spacing
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        repeat(pagerState.pageCount) { index ->
            val isSelected = pagerState.currentPage == index
            Box(
                modifier = Modifier
                    .width(if (isSelected) activeWidth else inactiveWidth)
                    .height(height)
                    .clip(RoundedCornerShape(50)) // Pill shape
                    .background(if (isSelected) activeColor else inactiveColor)
            )
        }
    }
}

