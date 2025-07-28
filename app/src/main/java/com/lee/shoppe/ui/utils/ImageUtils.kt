//package com.lee.shoppe.ui.utils
//
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.layout.ContentScale
//import androidx.compose.ui.platform.LocalContext
//import coil.compose.AsyncImage
//import coil.compose.AsyncImagePainter
//import coil.compose.rememberAsyncImagePainter
//import coil.request.ImageRequest
//import coil.size.Size
//import com.lee.shoppe.R
//
//@Composable
//fun NetworkImage(
//    url: String?,
//    contentDescription: String?,
//    modifier: Modifier = Modifier,
//    contentScale: ContentScale = ContentScale.Crop,
//    placeholder: Int = R.drawable.placeholder_image,
//    error: Int = R.drawable.error_image,
//    onLoading: (Boolean) -> Unit = {},
//    onError: (Throwable?) -> Unit = {}
//) {
//    val painter = rememberAsyncImagePainter(
//        model = ImageRequest.Builder(LocalContext.current)
//            .data(url)
//            .size(Size.ORIGINAL)
//            .crossfade(true)
//            .error(error)
//            .placeholder(placeholder)
//            .build(),
//        onState = { state ->
//            when (state) {
//                is AsyncImagePainter.State.Loading -> onLoading(true)
//                is AsyncImagePainter.State.Success -> onLoading(false)
//                is AsyncImagePainter.State.Error -> {
//                    onLoading(false)
//                    onError(state.result.throwable)
//                }
//                else -> {}
//            }
//        }
//    )
//
//    AsyncImage(
//        model = url,
//        contentDescription = contentDescription,
//        modifier = modifier,
//        contentScale = contentScale,
//        placeholder = painterResource(id = placeholder),
//        error = painterResource(id = error),
//        onLoading = { onLoading(true) },
//        onSuccess = { onLoading(false) },
//        onError = {
//            onLoading(false)
//            onError(it.result.throwable)
//        }
//    )
//}
//
//@Composable
//fun rememberImagePainter(
//    url: String?,
//    placeholder: Int = R.drawable.placeholder_image,
//    error: Int = R.drawable.error_image
//) = rememberAsyncImagePainter(
//    model = ImageRequest.Builder(LocalContext.current)
//        .data(url)
//        .size(Size.ORIGINAL)
//        .crossfade(true)
//        .placeholder(placeholder)
//        .error(error)
//        .build()
//)
