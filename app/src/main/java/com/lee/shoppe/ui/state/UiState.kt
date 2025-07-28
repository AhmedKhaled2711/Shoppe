//package com.lee.shoppe.ui.state
//
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.height
//import androidx.compose.material.CircularProgressIndicator
//import androidx.compose.material.Icon
//import androidx.compose.material.Text
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Error
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.MutableState
//import androidx.compose.runtime.Stable
//import androidx.compose.runtime.collectAsState
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.saveable.rememberSaveable
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.unit.dp
//import com.lee.shoppe.data.network.networking.NetworkState
//import kotlinx.coroutines.flow.Flow
//
///**
// * A generic class that holds a value with its loading status.
// */
//@Stable
//sealed class UiState<out T> {
//    object Loading : UiState<Nothing>()
//    data class Success<out T>(val data: T) : UiState<T>()
//    data class Error(val message: String? = null, val throwable: Throwable? = null) : UiState<Nothing>()
//
//    val isLoading: Boolean get() = this is Loading
//    val isError: Boolean get() = this is Error
//    val isSuccess: Boolean get() = this is Success
//
//    fun getOrNull(): T? = (this as? Success)?.data
//    fun getOrThrow(): T = (this as Success).data
//    fun errorMessage(): String? = (this as? Error)?.message
//}
//
///**
// * Remember a UiState that survives configuration changes
// */
//@Composable
//fun <T> rememberUiState(initial: UiState<T>): MutableState<UiState<T>> {
//    return rememberSaveable { mutableStateOf(initial) }
//}
//
///**
// * Execute an action and update the UI state accordingly
// */
//suspend fun <T> UiState<T>.executeWithState(
//    onLoading: () -> Unit = {},
//    onSuccess: (T) -> Unit = {},
//    onError: (String?, Throwable?) -> Unit = { _, _ -> }
//) {
//    when (this) {
//        is UiState.Loading -> onLoading()
//        is UiState.Success -> onSuccess(data)
//        is UiState.Error -> onError(message, throwable)
//    }
//}
//
///**
// * Convert a network state to UI state
// */
//fun <T> NetworkState<T>.toUiState(): UiState<T> {
//    return when (this) {
//        is NetworkState.Loading -> UiState.Loading
//        is NetworkState.Success -> UiState.Success(data)
//        is NetworkState.Failure -> UiState.Error(error.message, error)
//        NetworkState.Idle -> TODO()
//    }
//}
//
///**
// * Collect a flow and convert it to a Compose State
// */
//@Composable
//fun <T> Flow<UiState<T>>.collectAsUiState(
//    initial: UiState<T> = UiState.Loading
//): State<UiState<T>> {
//    return collectAsState(initial)
//}
//
///**
// * A wrapper for handling common UI states
// */
//@Composable
//fun <T> UiStateHandler(
//    state: UiState<T>,
//    onLoading: @Composable () -> Unit = { DefaultLoadingState() },
//    onError: @Composable (String?) -> Unit = { DefaultErrorState(it) },
//    onSuccess: @Composable (T) -> Unit
//) {
//    when (state) {
//        is UiState.Loading -> onLoading()
//        is UiState.Error -> onError(state.message)
//        is UiState.Success -> onSuccess(state.data)
//    }
//}
//
//@Composable
//private fun DefaultLoadingState() {
//    Box(
//        modifier = Modifier.fillMaxSize(),
//        contentAlignment = Alignment.Center
//    ) {
//        CircularProgressIndicator()
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
