package com.lee.shoppe.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.lee.shoppe.data.model.CustomerData
import com.lee.shoppe.ui.viewmodel.CartViewModel
import com.lee.shoppe.ui.viewmodel.OrderDetailsViewModel
import com.lee.shoppe.ui.viewmodel.CartAddressViewModel
import com.lee.shoppe.data.network.networking.NetworkState
import kotlin.math.abs
import androidx.compose.material3.SnackbarHostState
import androidx.core.content.ContentProviderCompat.requireContext
import com.example.fashionshop.Model.AddressBody
import com.example.fashionshop.Model.CustomerBody
import com.example.fashionshop.Model.DefaultAddressBody
import com.example.fashionshop.Model.LineItemBody
import com.example.fashionshop.Model.OrderBody
import com.lee.shoppe.data.model.Address
import com.lee.shoppe.data.model.DraftOrderResponse
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailsScreen(
    addressId: Long,
    navController: NavController,
    cartViewModel: CartViewModel = hiltViewModel(),
    orderDetailsViewModel: OrderDetailsViewModel = hiltViewModel(),
    addressViewModel: CartAddressViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val customerData = remember { CustomerData.getInstance(context) }
    val cartState by cartViewModel.cartProducts.collectAsState()
    val addressState by addressViewModel.products.collectAsState()
    val discountCodesState by orderDetailsViewModel.productCode.collectAsState()
    val couponText = remember { mutableStateOf("") }
    val discountPercent = remember { mutableStateOf(0.0) }
    val isCouponValid = remember { mutableStateOf(false) }
    val validateButtonColor = remember { mutableStateOf(Color.Gray) }
    val validateButtonText = remember { mutableStateOf("Validate") }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Load cart and address data on entry
    LaunchedEffect(customerData.cartListId) {
        if (customerData.cartListId > 0) cartViewModel.getCartProducts(customerData.cartListId)
        addressViewModel.getAllcustomer(customerData.id)
        orderDetailsViewModel.getAdsCode()
    }

    // Get cart line items and subtotal
    val lineItems = (cartState as? NetworkState.Success)?.data?.draft_order?.line_items?.drop(1) ?: emptyList()
    val subtotal = lineItems.sumOf { (it.price?.toDoubleOrNull() ?: 0.0) * (it.quantity ?: 1) }
    val currency = customerData.currency.ifEmpty { "EGP" }

    // Get selected address
    val selectedAddress = (addressState as? NetworkState.Success)?.data?.customer?.addresses?.find { it.id == addressId }

    // Calculate discount and total
    val discountAmount = subtotal * (abs(discountPercent.value) / 100)
    val total = subtotal - discountAmount

    Scaffold(
        topBar = { TopAppBar(title = { Text("Order Details") }) },
        bottomBar = {
            Button(
                onClick = {
                    if (selectedAddress != null && lineItems.isNotEmpty()) {
                        val addressBody = AddressBody(
                            address1 = selectedAddress.address1,
                            city = selectedAddress.city,
                            country = selectedAddress.country,
                            country_code = selectedAddress.country_code,
                            first_name = selectedAddress.first_name,
                            last_name = selectedAddress.last_name,
                            name = selectedAddress.name,
                            phone = selectedAddress.phone,
                            zip = selectedAddress.zip
                        )

                        val defaultAddress = DefaultAddressBody(
                            first_name = selectedAddress.first_name,
                            address1 = selectedAddress.address1,
                            phone = selectedAddress.phone,
                            city = selectedAddress.city,
                            zip = selectedAddress.zip,
                            country = selectedAddress.country,
                            last_name = selectedAddress.last_name,
                            name = selectedAddress.name,
                            country_code = selectedAddress.country_code,
                            default = true
                        )
                        val customer = CustomerBody(
                            id = CustomerData.getInstance(context).id,
                            email = CustomerData.getInstance(context).email,
                            first_name = CustomerData.getInstance(context).name,
                            last_name = CustomerData.getInstance(context).name,
                            currency = currency,
                            default_address = defaultAddress
                        )
                        val lineItem = lineItems.map { lineItem ->
                            LineItemBody(
                                variant_id = lineItem.variant_id,
                                quantity = lineItem.quantity,
                                id = lineItem.id,
                                title = lineItem.title,
                                price = lineItem.price,
                                sku = lineItem.sku,
                                properties = lineItem.properties.map { draftProperty ->
                                    LineItemBody.Property(
                                        name = draftProperty.name,
                                        value = draftProperty.value
                                    )
                                }


                            )
                        }
                        val orderBody = mapOf(
                            "order" to OrderBody(
                                billing_address = addressBody,
                                customer = customer,
                                line_items = lineItem,
                                total_tax = 13.5,
                                currency = currency,
                                total_discounts = discountAmount.toString(),
                                referring_site = "Cash"
                            )
                        )
                        coroutineScope.launch {
                            orderDetailsViewModel.createOrder(
                                orderBody = orderBody,
                                onSuccess = {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Order placed successfully!")
                                        cartViewModel.clearCart(customerData.cartListId)
                                        navController.navigate("home") {
                                            popUpTo(0)
                                        }
                                    }
                                },
                                onError = { errorMsg ->
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Order failed: $errorMsg")
                                    }
                                }
                            )
                        }
                    } else {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Missing address or cart items.")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Place Order", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (cartState is NetworkState.Loading || addressState is NetworkState.Loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Address details
                    selectedAddress?.let { address ->
                        Text("Shipping to: ${address.name}, ${address.address1}, ${address.city}, ${address.country}", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Coupon Row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = couponText.value,
                            onValueChange = { couponText.value = it },
                            label = { Text("Coupon") },
                            modifier = Modifier.weight(2f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                // Validate coupon
                                val codes = (discountCodesState as? NetworkState.Success)?.data?.price_rules ?: emptyList()
                                val found = codes.find { it.title == couponText.value }
                                if (found != null) {
                                    discountPercent.value = found.value.toDoubleOrNull() ?: 0.0
                                    isCouponValid.value = true
                                    validateButtonColor.value = Color.Green
                                    validateButtonText.value = "Valid"
                                } else {
                                    discountPercent.value = 0.0
                                    isCouponValid.value = false
                                    validateButtonColor.value = Color.Gray
                                    validateButtonText.value = "Validate"
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = validateButtonColor.value)
                        ) {
                            Text(validateButtonText.value, color = Color.White)
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))

                    // Subtotal
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Subtotal", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text(String.format("%.2f", subtotal), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text(currency, fontSize = 14.sp, color = Color.Black)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    // Discount
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Discount", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text("${discountPercent.value}%", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    // Total
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text(String.format("%.2f", total), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text(currency, fontSize = 14.sp, color = Color.Black)
                    }
                }
            }
        }
    }
}
