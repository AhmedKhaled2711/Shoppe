//package com.lee.shoppe.ui.components
//
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.LazyListState
//import androidx.compose.foundation.lazy.items
//import androidx.compose.foundation.lazy.rememberLazyListState
//import androidx.compose.material.CircularProgressIndicator
//import androidx.compose.material.ExperimentalMaterialApi
//import androidx.compose.material.Icon
//import androidx.compose.material.Text
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Error
//import androidx.compose.material.pullrefresh.PullRefreshIndicator
//import androidx.compose.material.pullrefresh.pullRefresh
//import androidx.compose.material.pullrefresh.rememberPullRefreshState
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.unit.dp
//
///**
// * A reusable optimized list component with pull-to-refresh and loading states
// */
//@OptIn(ExperimentalMaterialApi::class)
//@Composable
//fun <T> OptimizedList(
//    items: List<T>,
//    itemContent: @Composable (T) -> Unit,
//    modifier: Modifier = Modifier,
//    state: LazyListState = rememberLazyListState(),
//    isRefreshing: Boolean = false,
//    isLoading: Boolean = false,
//    onRefresh: (() -> Unit)? = null,
//    loadingContent: @Composable () -> Unit = { DefaultLoadingIndicator() },
//    emptyContent: @Composable () -> Unit = { DefaultEmptyState() },
//    errorContent: @Composable (String?) -> Unit = { DefaultErrorState(it) },
//    error: String? = null
//) {
//    Box(
//        modifier = modifier.fillMaxSize(),
//        contentAlignment = Alignment.Center
//    ) {
//        when {
//            isLoading && items.isEmpty() -> {
//                loadingContent()
//            }
//            error != null -> {
//                errorContent(error)
//            }
//            items.isEmpty() -> {
//                emptyContent()
//            }
//            else -> {
//                val refreshState = rememberPullRefreshState(
//                    refreshing = isRefreshing,
//                    onRefresh = { onRefresh?.invoke() }
//                )
//
//                Box(Modifier.pullRefresh(refreshState)) {
//                    LazyColumn(
//                        state = state,
//                        modifier = Modifier.fillMaxSize(),
//                        contentPadding = PaddingValues(8.dp),
//                        verticalArrangement = Arrangement.spacedBy(8.dp)
//                    ) {
//                        items(items) { item ->
//                            itemContent(item)
//                        }
//                    }
//
//                    if (onRefresh != null) {
//                        PullRefreshIndicator(
//                            refreshing = isRefreshing,
//                            state = refreshState,
//                            modifier = Modifier.align(Alignment.TopCenter)
//                        )
//                    }
//                }
//            }
//        }
//    }
//}
//
//@Composable
//private fun DefaultLoadingIndicator() {
//    Column(
//        modifier = Modifier.fillMaxSize(),
//        verticalArrangement = Arrangement.Center,
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        CircularProgressIndicator()
//    }
//}
//
//@Composable
//private fun DefaultEmptyState() {
//    Column(
//        modifier = Modifier.fillMaxSize(),
//        verticalArrangement = Arrangement.Center,
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        Text("No items found")
//    }
//}
//
//@Composable
//private fun DefaultErrorState(message: String?) {
//    Column(
//        modifier = Modifier.fillMaxSize(),
//        verticalArrangement = Arrangement.Center,
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        Icon(
//            imageVector = Icons.Default.Error,
//            contentDescription = "Error",
//            tint = MaterialTheme.colorScheme.error
//        )
//        Spacer(modifier = Modifier.height(8.dp))
//        Text(
//            text = message ?: "An error occurred",
//            color = MaterialTheme.colorScheme.error
//        )
//    }
//}
