package com.lee.shoppe.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
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
import com.lee.shoppe.data.model.ProductResponse
import com.lee.shoppe.data.model.ProductDetails
import com.lee.shoppe.data.model.Reviews
import com.lee.shoppe.data.network.networking.NetworkState
import com.lee.shoppe.ui.viewmodel.FavViewModel
import com.lee.shoppe.ui.viewmodel.ProductInfoViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.random.Random

// Utility function to convert ProductDetails to Product
fun productDetailsToProduct(details: ProductDetails): Product {
    return Product(
        id = details.id,
        title = details.title,
        image = details.image,
        variants = details.variants,
        tags = null,
        product_type = null
    )
}

@Composable
fun ProductDetailsScreen(
    productId: Long,
    navController: NavController,
    favViewModel: FavViewModel = hiltViewModel(),
    productInfoViewModel: ProductInfoViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val customerData = CustomerData.getInstance(context)
    
    // State management
    var isFavorite by remember { mutableStateOf(false) }
    var selectedVariantId by remember { mutableStateOf(-1L) }
    var selectedPrice by remember { mutableStateOf("") }
    var showRemoveDialog by remember { mutableStateOf(false) }
    var productToRemove by remember { mutableStateOf<Product?>(null) }
    
    // Collect states from ViewModels
    val productState by productInfoViewModel.product.collectAsState()
    val productCardState by productInfoViewModel.productCard.collectAsState()
    val productSuggestionsState by productInfoViewModel.productSuggestions.collectAsState()
    val favState by favViewModel.product.collectAsState()
    
    // Load product info when screen loads
    LaunchedEffect(productId) {
        productInfoViewModel.getProductInfo(productId)
    }
    
    // Listen to FavViewModel state changes
    LaunchedEffect(favState) {
        when (favState) {
            is NetworkState.Success -> {
                println("FavViewModel operation completed successfully in ProductDetailsScreen")
                // Optionally show success message
                android.widget.Toast.makeText(context, "Favorite updated successfully", android.widget.Toast.LENGTH_SHORT).show()
            }
            is NetworkState.Failure -> {
                println("FavViewModel operation failed in ProductDetailsScreen: ${(favState as NetworkState.Failure).error.message}")
                // Revert the favorite state if operation failed
                if (productState is NetworkState.Success) {
                    val product = (productState as NetworkState.Success<ProductResponse>).data.product
                    product?.id?.let { id ->
                        if (customerData.favListId > 0) {
                            favViewModel.isFavProduct(
                                id = id,
                                listId = customerData.favListId,
                                favTrue = { isFavorite = true },
                                favFalse = { isFavorite = false }
                            )
                        }
                    }
                }
                // Show error message
                android.widget.Toast.makeText(context, "Failed to update favorite: ${(favState as NetworkState.Failure).error.message}", android.widget.Toast.LENGTH_SHORT).show()
            }
            else -> {}
        }
    }
    
    // Check favorite status when product loads
    LaunchedEffect(productState) {
        if (productState is NetworkState.Success && customerData.isLogged) {
            val product = (productState as NetworkState.Success<ProductResponse>).data.product
            product?.id?.let { id ->
                if (customerData.favListId > 0) {
                    favViewModel.isFavProduct(
                        id = id,
                        listId = customerData.favListId,
                        favTrue = { 
                            println("Product $id is favorite")
                            isFavorite = true 
                        },
                        favFalse = { 
                            println("Product $id is not favorite")
                            isFavorite = false 
                        }
                    )
                } else {
                    println("favListId not set yet, defaulting to not favorite")
                    isFavorite = false
                }
            }
        }
    }

    // Load product suggestions when product loads
    LaunchedEffect(productState) {
        if (productState is NetworkState.Success) {
            val product = (productState as NetworkState.Success<ProductResponse>).data.product
            product?.vendor?.let { vendor ->
                productInfoViewModel.getProductSuggestions(vendor)
            }
        }
    }
    
    // Handle cart state changes
    LaunchedEffect(productCardState) {
        when (productCardState) {
            is NetworkState.Success -> {
                android.widget.Toast.makeText(context, "Product added to cart successfully!", android.widget.Toast.LENGTH_SHORT).show()
                productInfoViewModel.resetProductCardState()
            }
            is NetworkState.Failure -> {
                val errorMessage = (productCardState as NetworkState.Failure).error.message
                android.widget.Toast.makeText(context, errorMessage ?: "Failed to add to cart", android.widget.Toast.LENGTH_SHORT).show()
                productInfoViewModel.resetProductCardState()
            }
            else -> {}
        }
    }
    
    val coroutineScope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(bottom = 80.dp) // Padding for bottom bar
        ) {
            // Custom Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
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
                    text = "Product Details",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    modifier = Modifier.weight(1f)
                )
            }
            //Spacer(modifier = Modifier.height(8.dp))
            // Main content (no Box)
        when (productState) {
            is NetworkState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is NetworkState.Success -> {
                val product = (productState as NetworkState.Success<ProductResponse>).data.product
                if (product != null) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            // Product Images Slider
                            if (product.images?.isNotEmpty() == true) {
                                var currentPage by remember { mutableStateOf(0) }
                                
                                Column {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(300.dp)
                                    ) {
                                        AsyncImage(
                                            model = product.images[currentPage].src,
                                            contentDescription = product.title,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop,
                                            error = painterResource(id = R.drawable.broken_image)
                                        )
                                        
                                        // Image indicators
                                        Row(
                                            modifier = Modifier
                                                .align(Alignment.BottomCenter)
                                                .padding(bottom = 16.dp),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            repeat(product.images.size) { index ->
                                                Box(
                                                    modifier = Modifier
                                                        .size(8.dp)
                                                        .background(
                                                            color = if (currentPage == index) 
                                                                Color(0xFF2196F3)
                                                            else Color.Gray.copy(alpha = 0.3f),
                                                            shape = CircleShape
                                                        )
                                                )
                                            }
                                        }
                                    }
                                    
                                    // Image selection buttons
                                    if (product.images.size > 1) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceEvenly
                                        ) {
                                            repeat(product.images.size) { index ->
                                                AsyncImage(
                                                    model = product.images[index].src,
                                                    contentDescription = null,
                                                    modifier = Modifier
                                                        .size(60.dp)
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .clickable { currentPage = index }
                                                        .border(
                                                            width = if (currentPage == index) 2.dp else 0.dp,
                                                            color = if (currentPage == index) 
                                                                Color(0xFF2196F3)
                                                            else Color.Transparent,
                                                            shape = RoundedCornerShape(8.dp)
                                                        ),
                                                    contentScale = ContentScale.Crop
                                                )
                                            }
                                        }
                                    }
                                }
                            } else {
                                AsyncImage(
                                    model = product.image?.src,
                                    contentDescription = product.title,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(300.dp),
                                    contentScale = ContentScale.Crop,
                                    error = painterResource(id = R.drawable.broken_image)
                                )
                            }
                        }
                        
                        item {
                            // Product Title
                            product.title?.let {
                                Text(
                                    text = it,
                                    color = Color.Black,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        
                                /*
                        item {
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
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "(${String.format("%.1f", randomRating)})",
                                    color = Color.Gray,
                                    fontSize = 14.sp
                                )
                            }
                                }*/
                        
                        item {
                            // Price and Currency
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val price = selectedPrice.ifEmpty { product.variants?.get(0)?.price ?: "0.00" }
                                val currency = customerData.currency
                                val currencySymbol = when (currency) {
                                    "USD" -> "$"
                                    "EGY" -> "EGP"
                                    "EUR" -> "€"
                                    "GBP" -> "£"
                                    else -> currency
                                }
                                
                                Text(
                                    text = "$price $currencySymbol",
                                    color = Color.Red,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        
                        item {
                            // Variants Selection
                                    if ((product.variants?.size ?: 0) > 1) {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "Select Variant:",
                                        color = Color.Black,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        items(product.variants ?: emptyList()) { variant ->
                                            Card(
                                                modifier = Modifier
                                                    .clickable {
                                                        val id = variant.id
                                                        val price = variant.price
                                                        if (id != null && price != null) {
                                                            selectedVariantId = id
                                                            selectedPrice = price
                                                        }
                                                    }
                                                    .border(
                                                        width = if (selectedVariantId == variant.id) 2.dp else 0.dp,
                                                        color = if (selectedVariantId == variant.id) 
                                                            Color(0xFF2196F3)
                                                        else Color.Transparent,
                                                        shape = RoundedCornerShape(8.dp)
                                                    ),
                                                colors = CardDefaults.cardColors(
                                                    containerColor = if (selectedVariantId == variant.id)
                                                        Color(0xFFE3F2FD)
                                                    else Color.White
                                                )
                                            ) {
                                                Column(
                                                    modifier = Modifier.padding(12.dp),
                                                    horizontalAlignment = Alignment.CenterHorizontally
                                                ) {
                                                    variant.title?.let {
                                                        Text(
                                                            text = it,
                                                            color = Color.Black,
                                                            fontSize = 14.sp,
                                                            fontWeight = FontWeight.Medium
                                                        )
                                                    }
                                                    variant.price?.let {
                                                        Text(
                                                            text = it,
                                                            color = Color.Gray,
                                                            fontSize = 12.sp
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        
                        item {
                            // Product Description
                            if (!product.body_html.isNullOrEmpty()) {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                                text = "Description",
                                        color = Color.Black,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = product.body_html,
                                        color = Color.Gray,
                                        fontSize = 16.sp,
                                        lineHeight = 24.sp
                                    )
                                }
                            }
                        }
                        
                        item {
                                    // Rating And Review
                                    val reviewsList = Reviews().getReviews()
                                    val randomReview = reviewsList.random()
                                    val averageRating = (reviewsList.map { it.rate }.average() * 10).toInt() / 10f
                                    ProductReviewSummary(
                                        averageRating = averageRating,
                                        reviewerName = randomReview.name,
                                        reviewerImageRes = randomReview.imageResId,
                                        reviewerRating = randomReview.rate,
                                        reviewText = randomReview.description ,
                                        onViewAllReviews = { navController.navigate("reviews") }
                                    )
                                }

                                item {
                                    // You may want other (Product Suggestions)
                                    when (productSuggestionsState) {
                                        is NetworkState.Loading -> {
                                            Box(
                                                modifier = Modifier.fillMaxWidth(),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                CircularProgressIndicator()
                                            }
                                        }
                                        is NetworkState.Success -> {
                                            val suggestions = (productSuggestionsState as NetworkState.Success<ProductResponse>).data.products ?: emptyList()
                                            if (suggestions.isNotEmpty()) {
                                                Column(modifier = Modifier.fillMaxWidth()) {
                                        Text(
                                                        text = "You may want other",
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 18.sp,
                                            color = Color.Black,
                                                        modifier = Modifier.padding(bottom = 8.dp)
                                        )
                                        LazyRow(
                                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                                        items(suggestions) { product ->
                                                Card(
                                                    modifier = Modifier
                                                                    .width(160.dp)
                                                                    .height(240.dp)
                                                        .clickable {
                                                                        navController.navigate("product_details/${product.id}")
                                                                    },
                                                                shape = RoundedCornerShape(10.dp),
                                                                colors = CardDefaults.cardColors(containerColor = Color.White)
                                                            ) {
                                                                Column(
                                                                    modifier = Modifier.fillMaxSize()
                                                                ) {
                                                                    Box(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                            .height(120.dp)
                                                                    ) {
                                                                        AsyncImage(
                                                                            model = product.image?.src,
                                                                            contentDescription = product.title,
                                                                            modifier = Modifier.fillMaxSize(),
                                                            contentScale = ContentScale.Crop,
                                                            error = painterResource(id = R.drawable.broken_image)
                                                        )
                                                                    }
                                                        Column(
                                                                        modifier = Modifier
                                                                            .fillMaxWidth()
                                                                            .padding(8.dp)
                                                        ) {
                                                                        product.title?.let {
                                                                Text(
                                                                    text = it,
                                                                    color = Color.Black,
                                                                                fontSize = 14.sp,
                                                                                fontWeight = FontWeight.Bold,
                                                                                maxLines = 2
                                                                            )
                                                                        }
                                                                        Spacer(modifier = Modifier.height(4.dp))
                                                                        // Price
                                                                        val price = product.variants?.get(0)?.price ?: "0.0"
                                                                        val currencySymbol = when (customerData.currency) {
                                                                            "USD" -> "$"
                                                                            "EGY" -> "EGP"
                                                                            "EUR" -> "€"
                                                                            "GBP" -> "£"
                                                                            else -> customerData.currency
                                                                        }

                                                            Text(
                                                                            text = "$price $currencySymbol",
                                                                color = Color.Red,
                                                                            fontSize = 14.sp,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                        }
                                        is NetworkState.Failure -> {
                                            // Optionally show nothing or an error
                                        }
                                        NetworkState.Idle -> {}
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Product not found",
                            color = Color.Black,
                            fontSize = 18.sp
                        )
                    }
                }
            }
            is NetworkState.Failure -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = Color.Red
                        )
                        Text(
                            text = "Failed to load product",
                            color = Color.Black,
                            fontSize = 18.sp
                        )
                        Button(
                            onClick = { productInfoViewModel.getProductInfo(productId) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2196F3)
                            )
                        ) {
                            Text(
                                text = "Retry",
                                color = Color.White
                            )
                        }
                    }
                }
                    }
                    NetworkState.Idle -> { /* No content */ }
                }
            }
        }
        // Bottom Bar
    Box(modifier = Modifier.fillMaxSize()) {

        ProductBottomBar(
                isFavorite = isFavorite,
                onFavoriteClick = {
                    if (isFavorite) {
                        // Show confirmation dialog before removing
                        val details = (productState as? NetworkState.Success<ProductResponse>)?.data?.product
                        if (details != null) {
                            productToRemove = productDetailsToProduct(details)
                            showRemoveDialog = true
                        }
                    } else {
                        isFavorite = true
                        val details = (productState as? NetworkState.Success<ProductResponse>)?.data?.product
                        if (details != null) {
                            val productForFav = productDetailsToProduct(details)
                            favViewModel.insertFavProduct(
                                product = productForFav,
                                listId = customerData.favListId,
                                onFavListCreated = { newFavListId ->
                                    customerData.favListId = newFavListId
                                }
                            )
                        }
                    }
                },
                onAddToCart = {
                    // Use the same add to cart logic as before
                    if (customerData.isLogged) {
                        if (selectedVariantId == -1L && (productState is NetworkState.Success && (productState as NetworkState.Success<ProductResponse>).data.product?.variants?.size ?: 0 > 1)) {
                            Toast.makeText(context, "Please select a variant first", Toast.LENGTH_SHORT).show()
                        } else {
                            val product = (productState as? NetworkState.Success<ProductResponse>)?.data?.product
                            val variantId = if (selectedVariantId == -1L) {
                                product?.variants?.get(0)?.id ?: -1L
                            } else {
                                selectedVariantId
                            }
                            if (variantId != -1L && product != null) {
                                if (customerData.cartListId <= 0) {
                                    coroutineScope.launch {
                                        try {
                                            val draftOrderResponse = productInfoViewModel.repository.createDraftOrders(
                                                DraftOrderResponse(DraftOrderResponse.DraftOrder())
                                            ).first()
                                            val newCartId = draftOrderResponse.draft_order.id
                                            customerData.cartListId = newCartId
                                            productInfoViewModel.insertCardProduct(
                                                product = product,
                                                vId = variantId,
                                                listId = newCartId
                                            )
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Failed to create cart. Please try again.", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                } else {
                                    productInfoViewModel.insertCardProduct(
                                        product = product,
                                        vId = variantId,
                                        listId = customerData.cartListId
                                    )
                                }
                            }
                        }
                    } else {
                        Toast.makeText(context, "Please login first", Toast.LENGTH_SHORT).show()
                    }
                },
                onBuyNow = {
                    // Implement buy now logic or navigation
                    Toast.makeText(context, "Buy now clicked", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }

    if (showRemoveDialog && productToRemove != null) {
        Dialog(onDismissRequest = {
            showRemoveDialog = false
            productToRemove = null
        }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        tint = Color(0xFF0057FF),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Remove from Favorites",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color(0xFF0057FF),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Are you sure you want to remove this product from your favorites?",
                        color = Color.Gray,
                        fontSize = 16.sp,
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = {
                                showRemoveDialog = false
                                productToRemove = null
                            },
                            border = BorderStroke(1.dp, Color(0xFF0057FF)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Cancel", color = Color(0xFF0057FF), fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Button(
                            onClick = {
                                productToRemove?.let { product ->
                                    favViewModel.deleteFavProduct(product.id ?: 0, customerData.favListId)
                                    isFavorite = false
                                }
                                showRemoveDialog = false
                                productToRemove = null
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                        ) {
                            Text("Remove", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun ProductReviewSummary(
    averageRating: Float = 4f,
    reviewerName: String = "Veronika",
    reviewerImageRes: Int = R.drawable.img_1, // Use your drawable
    reviewerRating: Float =  4.5f,
    reviewText: String = "Lorem ipsum dolor sit amet, consectetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed ...",
    onViewAllReviews: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
    ) {
        Text(
            text = "Rating & Reviews",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Stars
            repeat(5) { i ->
                Icon(
                    imageVector = if (i < averageRating.toInt()) Icons.Default.Star else Icons.Default.StarOutline,
                    contentDescription = null,
                    tint = Color(0xFFECA61B),
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            // Average badge
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFF0F4FF)
            ) {
                Text(
                    text = "${averageRating}/5",
                    color = Color(0xFF1A237E),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        // Review Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            //elevation = CardDefaults.cardElevation(4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = reviewerImageRes),
                        contentDescription = "${reviewerName}'s photo",
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(reviewerName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            repeat(5) { i ->
                                Icon(
                                    imageVector = if (i < reviewerRating.toInt()) Icons.Default.Star else Icons.Default.StarOutline,
                                    contentDescription = null,
                                    tint = Color(0xFFECA61B),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = reviewText,
                    color = Color.Gray,
                    fontSize = 14.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        // View All Reviews Button
        Button(
            onClick = onViewAllReviews,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF004CFF)
            )
        ) {
            Text(
                text = "View All Reviews",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }
    }
}

@Composable
fun ProductBottomBar(
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit,
    onAddToCart: () -> Unit,
    onBuyNow: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Favorite button
            IconButton(
                onClick = onFavoriteClick,
                modifier = Modifier
                    .size(48.dp)
                    .background(Color.White, shape = RoundedCornerShape(16.dp))
                    .border(1.dp, Color(0xFFE0E0E0), shape = RoundedCornerShape(16.dp))
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (isFavorite) Color(0xFF0057FF) else Color.Black,
                    modifier = Modifier.size(28.dp)
                )
            }
            // Add to cart button
            Button(
                onClick = onAddToCart,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
            ) {
                Text(
                    text = "Add to cart",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
            // Buy now button
            Button(
                onClick = onBuyNow,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0057FF))
            ) {
                Text(
                    text = "Buy now",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        }
    }
}
