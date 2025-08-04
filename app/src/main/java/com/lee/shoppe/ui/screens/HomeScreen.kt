package com.lee.shoppe.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
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
import com.lee.shoppe.data.model.SmartCollection
import com.lee.shoppe.data.network.networking.NetworkState
import com.lee.shoppe.ui.screens.dialogBox.NetworkErrorBox
import com.lee.shoppe.ui.utils.isNetworkConnected
import com.lee.shoppe.ui.components.LoadingWithMessages
import com.lee.shoppe.ui.viewmodel.HomeViewModel
import kotlinx.coroutines.launch
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.platform.LocalFocusManager


@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
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

    LaunchedEffect(Unit) {
        if (!isNetworkConnected(context)) {
            isOffline = true
        } else {
            isOffline = false
            viewModel.fetchDataIfNeeded()
        }
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
                "You're making requests too quickly. Please wait a moment and try again."
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
    } else {
        Scaffold(
            snackbarHost = { SnackbarHost(scaffoldState) }
        ) { _ ->
            Box(modifier = Modifier.fillMaxSize()) {
                if (isLoading) {
                    LoadingWithMessages(
                        modifier = Modifier.fillMaxSize(),
                        mainMessage = stringResource(R.string.loading_main_message),
                        secondaryMessage = stringResource(R.string.loading_secondary_message),
                        loadingIndicatorColor = Color(0xFF2196F3)
                    )
                } else if (showEmpty) {
                    LoadingWithMessages(
                        modifier = Modifier.fillMaxSize(),
                        mainMessage = stringResource(R.string.network_message_main),
                        secondaryMessage = "${stringResource(R.string.network_message_first)}\n${stringResource(R.string.network_message_second)}",
                        loadingIndicatorColor = Color(0xFF2196F3),
                        spacing = 8.dp,
                        messageSpacing = 4.dp
                    )
                }
                else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.shoppe),
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 25.sp,
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            // Professional SearchBar
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp),
                                shape = RoundedCornerShape(20.dp),
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
                                                        text = "Search products...",
                                                        color = Color.Gray,
                                                        fontSize = 16.sp
                                                    )
                                                }
                                                innerTextField()
                                            }
                                        )
                                    }
                                }
                            }
                        }
                        if (priceRulesState is NetworkState.Success) {
                            CouponCardSlider(
                                priceRules = sliderData,
                                onDiscountLongPress = { discount ->
                                    clipboardManager.setText(AnnotatedString(discount))
                                    coroutineScope.launch {
                                        scaffoldState.showSnackbar(context.getString(R.string.copy_discount_code))
                                    }
                                }
                            )
                        }
                        Text(
                            text = stringResource(R.string.brands),
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 25.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),

                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        if (brandsState is NetworkState.Success) {
                            BrandGrid(
                                brands = brands,
                                onBrandClick = { brand ->
                                    coroutineScope.launch {
                                        scaffoldState.showSnackbar(context.getString(R.string.clicked_on) + " ${brand.title}")
                                    }
                                    navController.navigate("products/${brand.title ?: "Default Title"}")
                                }
                            )
                        }
                    }
                }
                // Error Dialog
                showDialog?.let { (title, message) ->
                    AlertDialog(
                        onDismissRequest = { showDialog = null },
                        title = { Text(title) },
                        text = { Text(message) },
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
fun BrandGrid(
    brands: List<SmartCollection>,
    onBrandClick: (SmartCollection) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 100.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(brands) { brand ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .size(100.dp)
                        .combinedClickable(
                            onClick = { onBrandClick(brand) },
                            onLongClick = null
                        )
                ) {
                    val imageUrl = brand.image?.src
                    if (imageUrl != null) {
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = brand.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        // Placeholder image if missing
                        Image(
                            painter = painterResource(id = R.drawable.broken_image),
                            contentDescription = brand.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = brand.title ?: "",
                    color = Color.Black,
                    fontSize = 14.sp,
                    maxLines = 1
                )
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
    activeWidth: Dp = 32.dp,       // Increased from 24.dp → 32.dp
    inactiveWidth: Dp = 12.dp,     // Increased from 8.dp → 12.dp
    height: Dp = 12.dp,            // Increased from 8.dp → 12.dp
    spacing: Dp = 8.dp             // Slightly more spacing
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

