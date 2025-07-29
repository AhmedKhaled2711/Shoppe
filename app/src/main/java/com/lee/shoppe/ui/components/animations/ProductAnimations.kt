package com.lee.shoppe.ui.components.animations

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.core.spring

/**
 * A composable that applies a fade-in and scale-in animation to its content.
 * @param modifier Modifier to be applied to the animated content
 * @param delayMs Delay before starting the animation in milliseconds
 * @param content The content to be animated
 */
@Composable
fun AnimatedProductItem(
    modifier: Modifier = Modifier,
    delayMs: Int = 0,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(
            animationSpec = tween(
                durationMillis = 300,
                delayMillis = delayMs
            )
        ) + scaleIn(
            initialScale = 0.95f,
            animationSpec = tween(
                durationMillis = 300,
                delayMillis = delayMs
            )
        ),
        exit = fadeOut()
    ) {
        Box(modifier = modifier) {
            content()
        }
    }
}

/**
 * A composable that applies a staggered animation to items in a list.
 * @param index The index of the item in the list (used for staggering)
 * @param delayPerItemMs Delay between each item's animation in milliseconds
 * @param content The content to be animated
 */
@Composable
fun StaggeredAnimatedItem(
    index: Int,
    delayPerItemMs: Int = 100,
    content: @Composable () -> Unit
) {
    val delay = index * delayPerItemMs
    val animationSpec = tween<Float>(
        durationMillis = 400,
        delayMillis = delay,
        easing = androidx.compose.animation.core.FastOutSlowInEasing
    )

    val alpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = animationSpec,
        label = "fade_animation"
    )
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = animationSpec,
        label = "scale_animation"
    )

    Box(
        modifier = Modifier
            .graphicsLayer {
                this.alpha = alpha
                this.scaleX = scale
                this.scaleY = scale
            }
    ) {
        content()
    }
}

///**
// * A helper function to apply item animations in a LazyColumn or LazyVerticalGrid
// */
//fun Modifier.animateItem() = this.animateItemPlacement(
//    animationSpec = spring(
//        dampingRatio = 0.7f,
//        stiffness = 300f
//    )
//)
