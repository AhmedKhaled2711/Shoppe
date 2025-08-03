package com.lee.shoppe.ui.screens

import android.app.AlertDialog
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.lee.shoppe.R
import com.lee.shoppe.data.model.CustomerData
import com.lee.shoppe.data.network.networking.NetworkState
import com.lee.shoppe.ui.components.LoadingWithMessages
import com.lee.shoppe.ui.components.ScreenHeader
import com.lee.shoppe.ui.utils.PaymentConstants
import com.lee.shoppe.ui.viewmodel.CartAddressViewModel
import com.lee.shoppe.ui.viewmodel.CartViewModel
import com.lee.shoppe.ui.viewmodel.OrderDetailsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentSheetScreen(
    paymentUrl: String,
    discountValue: Double,
    onDismiss: () -> Unit,
    navController: NavController,
    cartViewModel: CartViewModel = hiltViewModel(),
    orderDetailsViewModel: OrderDetailsViewModel = hiltViewModel(),
    addressViewModel: CartAddressViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val customerData = remember { CustomerData.getInstance(context) }

    var isLoading by remember { mutableStateOf(true) }
    val loadingMessage = "Processing Payment"
    val secondaryMessage = "Please wait while we connect to the payment gateway..."

    val cartState by cartViewModel.cartProducts.collectAsState()
    val addressState by addressViewModel.products.collectAsState()

    LaunchedEffect(Unit) {
        cartViewModel.getCartProducts(customerData.cartListId)
        addressViewModel.getCustomerData(customerData.id , forceRefresh = true)
    }

    val lineItems = (cartState as? NetworkState.Success)?.data?.draft_order?.line_items?.drop(1) ?: emptyList()
    val selectedAddress = (addressState as? NetworkState.Success)?.data?.customer?.addresses?.firstOrNull { it.default }

    val snackbarHostState = remember { SnackbarHostState() }

    val handleSuccess: () -> Unit = {
        Log.i("PaymentSheetScreen", "Payment successful, proceeding with order placement")
        scope.launch {
            snackbarHostState.showSnackbar(context.getString(R.string.payment_success))

            // Ensure data is loaded before placing order
            val currentLineItems = (cartState as? NetworkState.Success)?.data?.draft_order?.line_items?.drop(1) ?: emptyList()
            val currentSelectedAddress = (addressState as? NetworkState.Success)?.data?.customer?.addresses?.firstOrNull { it.default }
            
            Log.d("PaymentSheetScreen", "Line items count: ${currentLineItems.size}")
            Log.d("PaymentSheetScreen", "Selected address: ${currentSelectedAddress?.address1 ?: "null"}")
            
            if (currentSelectedAddress == null || currentLineItems.isEmpty()) {
                Log.e("PaymentSheetScreen", "Missing data - Address: ${currentSelectedAddress != null}, Items: ${currentLineItems.size}")
                snackbarHostState.showSnackbar("Error: Missing address or cart items. Please try again.")
                return@launch
            }

            placeOrder(
                orderDetailsViewModel = orderDetailsViewModel,
                cartViewModel = cartViewModel,
                customerData = customerData,
                selectedAddress = currentSelectedAddress,
                lineItems = currentLineItems,
                discountValue = discountValue.toString(),
                paymentMethod = "Visa",
                currency = customerData.currency,
                onSuccess = {
                    showAlertDialog(context)
                    scope.launch {
                        //delay(4000L)
                        CoroutineScope(Dispatchers.Main).launch {
                            navController.navigate("home") {
                                popUpTo(0)
                            }
                        }
                        onDismiss()
                    }
                },
                onError = { errorMessage ->
                    scope.launch {
                        snackbarHostState.showSnackbar(errorMessage)
                    }
                    Log.d("PaymentSheetScreen", "Order placement error: $errorMessage")
                }
            )
        }
    }

    Scaffold(
        topBar = {
            ScreenHeader(
                title = "Payment",
                onBackClick = {
                    Log.d("PaymentSheetScreen", "Back button clicked")
                    onDismiss()
                },
                showBackButton = true
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                LoadingWithMessages(
                    modifier = Modifier.fillMaxSize(),
                    mainMessage = loadingMessage,
                    secondaryMessage = secondaryMessage,
                    loadingIndicatorColor = MaterialTheme.colorScheme.primary,
                    spacing = 16.dp,
                    messageSpacing = 8.dp
                )
            }

            AndroidView(
                factory = { context ->
                    WebView(context).apply {
                        // Enable scrolling and touch
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.loadWithOverviewMode = true
                        settings.useWideViewPort = true
                        settings.builtInZoomControls = false
                        settings.displayZoomControls = false
                        
                        // Enable scrolling
                        isVerticalScrollBarEnabled = true
                        isHorizontalScrollBarEnabled = true
                        
                        // Make WebView focusable for touch events
                        isFocusable = true
                        isFocusableInTouchMode = true
                        
                        webViewClient = object : WebViewClient() {
                            override fun shouldOverrideUrlLoading(
                                view: WebView?,
                                request: WebResourceRequest?
                            ): Boolean {
                                val url = request?.url.toString()
                                Log.d("PaymentSheetScreen", "URL loading: $url")
                                if (url.startsWith(PaymentConstants.SUCCESS_URL)) {
                                    handleSuccess()
                                    return true
                                }
                                return false
                            }

                            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                                if (url != null && url.startsWith(PaymentConstants.SUCCESS_URL)) {
                                    handleSuccess()
                                    return true
                                }
                                return false
                            }

                            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                                super.onPageStarted(view, url, favicon)
                                isLoading = true
                                Log.d("PaymentSheetScreen", "Page started loading: $url")
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                isLoading = false
                                Log.d("PaymentSheetScreen", "Page finished loading: $url")
                            }

                            override fun onReceivedError(
                                view: WebView?,
                                request: WebResourceRequest?,
                                error: android.webkit.WebResourceError?
                            ) {
                                super.onReceivedError(view, request, error)
                                isLoading = false
                                Log.e("PaymentSheetScreen", "WebView error: ${error?.description}, URL: ${request?.url}")
                                scope.launch {
                                    snackbarHostState.showSnackbar("Error loading payment page: ${error?.description}")
                                }
                            }

                            override fun onReceivedHttpError(
                                view: WebView?,
                                request: WebResourceRequest?,
                                errorResponse: android.webkit.WebResourceResponse?
                            ) {
                                super.onReceivedHttpError(view, request, errorResponse)
                                isLoading = false
                                Log.e("PaymentSheetScreen", "HTTP error: ${errorResponse?.statusCode}, URL: ${request?.url}")
                                scope.launch {
                                    snackbarHostState.showSnackbar("HTTP error: ${errorResponse?.statusCode}")
                                }
                            }
                        }
                        webChromeClient = WebChromeClient()
                        
                        // Validate payment URL before loading
                        if (paymentUrl.isBlank() || !paymentUrl.startsWith("http")) {
                            Log.e("PaymentSheetScreen", "Invalid payment URL: $paymentUrl")
                            scope.launch {
                                snackbarHostState.showSnackbar("Invalid payment URL")
                            }
                            isLoading = false
                        } else {
                            loadUrl(paymentUrl)
                            Log.d("PaymentSheetScreen", "Loading payment URL: $paymentUrl")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f) // Take remaining space
            )

            if (isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

private fun showAlertDialog(context: android.content.Context) {
    Handler(Looper.getMainLooper()).post {
        val alertDialog = AlertDialog.Builder(context)
            .setTitle("order_success")
            .setMessage(context.getString(R.string.order_placed_success))
            .setPositiveButton(context.getString(R.string.ok)) { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        alertDialog.show()

        Handler(Looper.getMainLooper()).postDelayed({
            alertDialog.dismiss()
        }, 4000)
    }
}
