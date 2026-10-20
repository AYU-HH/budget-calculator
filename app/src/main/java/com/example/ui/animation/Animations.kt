package com.example.ui.animation

import androidx.compose.animation.core.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Animation durations
val DefaultAnimationDuration = 300
val FastAnimationDuration = 150
val SlowAnimationDuration = 600

// Shared transition animations
fun rememberTransitionAnimation(): Transition = remember {
    Transition(
        transitionSpec = {
            if (initialState == null || targetState == null) {
                // No transition if either state is null
                return@Transition null
            }
            // Define the transition animation
            slideInVertically(
                animationSpec = tween(
                    durationMillis = DefaultAnimationDuration,
                    easing = LinearEasing
                )
            ) with slideOutVertically(
                animationSpec = tween(
                    durationMillis = DefaultAnimationDuration,
                    easing = LinearEasing
                )
            ) combining slideInHorizontally(
                animationSpec = tween(
                    durationMillis = DefaultAnimationDuration,
                    easing = LinearEasing
                )
            ) with slideOutHorizontally(
                animationSpec = tween(
                    durationMillis = DefaultAnimationDuration,
                    easing = LinearEasing
                )
            )
        }
    )
}

// Card animation
@Composable
fun CardAnimation(modifier: Modifier = Modifier) {
    var isVisible by remember { mutableStateOf(false) }
    val scale by animateDpAsState(
        targetValue = if (isVisible) 1f else 0.9f,
        animationSpec = tween(
            durationMillis = DefaultAnimationDuration,
            easing = LinearEasing
        )
    )

    Box(modifier = modifier) {
        // Your content here
        Box(
            modifier = Modifier
                .fillMaxSize()
                .scale(scale)
        ) {
            // Placeholder for card content
        }
    }

    // Trigger animation when visible state changes
    LaunchedEffect(isVisible) {
        isVisible = !isVisible
    }
}

// Fade animation
@Composable
fun FadeAnimation(modifier: Modifier = Modifier, visible: Boolean) {
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(
            durationMillis = DefaultAnimationDuration,
            easing = LinearEasing
        )
    )

    Box(modifier = modifier) {
        Box(
            modifier = Modifier.alpha(alpha)
        ) {
            // Placeholder for content
        }
    }
}