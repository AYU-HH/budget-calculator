package com.example.ui.animation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * Apple Fluid Motion Design System (WWDC Fluid Interfaces & HIG Motion):
 *
 * Core Principles:
 * 1. Physical Springs over Linear Curves: Motion governed by mass, stiffness, and damping.
 * 2. Instant Tactile Response: Zero-latency compression on press, bouncy overshoot on release.
 * 3. Directional Momentum: Transitions respect gestural & spatial context (slide with cross-fade).
 * 4. Staggered Cascades: Content elements glide in sequentially rather than appearing en masse.
 * 5. Fluid Continuity: Numeric values roll and morph continuously without jarring flashes.
 */
object AppleMotion {
    // Highly responsive bouncy spring: tactile button presses, chips, FAB, keypads, checkmarks
    val BouncySpring = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMediumLow
    )

    // Snappy spring: quick micro-interactions, toggles, icon transformations
    val SnappySpring = spring<Float>(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessMedium
    )

    // Gentle gliding spring: full sheet presentations, expanded cards, layout rebalancing
    val GentleSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessLow
    )

    // Spatial Spring for Dp transitions
    val DpSpring = spring<Dp>(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessMediumLow
    )

    // IntOffset Spring for positional slides
    val OffsetSpring = spring<IntOffset>(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessMediumLow
    )

    // Standard Apple cascade delay between successive staggered elements
    const val StaggerDelayStepMs = 40L
}

/**
 * Modifier.applePressEffect:
 * Implements Apple's signature tactile squish & bouncy pop feedback.
 * When the user touches down, the component immediately scales down (scaleDown)
 * with snappy stiffness. On release, it overshoots and springs back smoothly.
 */
fun Modifier.applePressEffect(
    scaleDown: Float = 0.95f,
    pressedAlpha: Float = 0.92f,
    enabled: Boolean = true
): Modifier = composed {
    if (!enabled) return@composed this

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) scaleDown else 1.0f,
        animationSpec = if (isPressed) {
            spring(dampingRatio = 0.9f, stiffness = 1200f) // instant compression
        } else {
            AppleMotion.BouncySpring // bouncy playful release
        },
        label = "apple_press_scale"
    )

    val alpha by animateFloatAsState(
        targetValue = if (isPressed) pressedAlpha else 1.0f,
        animationSpec = spring(stiffness = Spring.StiffnessHigh),
        label = "apple_press_alpha"
    )

    this
        .scale(scale)
        .alpha(alpha)
}

/**
 * Modifier.fluidBouncyClick:
 * Combines Apple-style bouncy spring compression with Compose ripple and click handling.
 */
fun Modifier.fluidBouncyClick(
    scaleDown: Float = 0.95f,
    rippleColor: Color = Color.Unspecified,
    enabled: Boolean = true,
    onClick: () -> Unit
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) scaleDown else 1.0f,
        animationSpec = if (isPressed) {
            spring(dampingRatio = 0.9f, stiffness = 1200f)
        } else {
            AppleMotion.BouncySpring
        },
        label = "fluid_bouncy_click_scale"
    )

    this
        .scale(scale)
        .clickable(
            interactionSource = interactionSource,
            indication = ripple(color = rippleColor),
            enabled = enabled,
            onClick = onClick
        )
}

/**
 * Modifier.fluidStaggeredEntrance:
 * Cascades UI elements into the view with Apple-style staggered spring reveals:
 * - Slides upward smoothly (from +20.dp to 0.dp)
 * - Fades from alpha 0.0 to 1.0
 * - Scales subtly from 0.96 to 1.0
 */
fun Modifier.fluidStaggeredEntrance(
    index: Int,
    baseDelayMs: Long = AppleMotion.StaggerDelayStepMs,
    stiffness: Float = Spring.StiffnessMediumLow
): Modifier = composed {
    var hasAppeared by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(index * baseDelayMs)
        hasAppeared = true
    }

    val alpha by animateFloatAsState(
        targetValue = if (hasAppeared) 1f else 0f,
        animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
        label = "stagger_alpha_$index"
    )

    val offsetY by animateDpAsState(
        targetValue = if (hasAppeared) 0.dp else 24.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = stiffness
        ),
        label = "stagger_offset_$index"
    )

    val scale by animateFloatAsState(
        targetValue = if (hasAppeared) 1f else 0.95f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = stiffness
        ),
        label = "stagger_scale_$index"
    )

    this
        .offset(y = offsetY)
        .scale(scale)
        .alpha(alpha)
}

/**
 * Fluid Tab Transition:
 * Provides Apple-style directional screen slide + cross-fade with spring physics.
 */
fun fluidTabTransitionSpec(
    targetState: Int,
    initialState: Int
): ContentTransform {
    val isMovingRight = targetState > initialState
    val slideOffset = 220

    val enterTransition: EnterTransition = slideInHorizontally(
        animationSpec = AppleMotion.OffsetSpring,
        initialOffsetX = { if (isMovingRight) slideOffset else -slideOffset }
    ) + fadeIn(
        animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing)
    )

    val exitTransition: ExitTransition = slideOutHorizontally(
        animationSpec = AppleMotion.OffsetSpring,
        targetOffsetX = { if (isMovingRight) -slideOffset else slideOffset }
    ) + fadeOut(
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing)
    )

    return enterTransition togetherWith exitTransition
}

/**
 * Fluid Card Expansion Transition:
 * For expanding/collapsing dialogs, details, or sections with bouncy spring dynamics.
 */
fun fluidExpandVertical(): EnterTransition =
    expandVertically(
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        )
    ) + fadeIn(animationSpec = tween(durationMillis = 240))

fun fluidShrinkVertical(): ExitTransition =
    shrinkVertically(
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        )
    ) + fadeOut(animationSpec = tween(durationMillis = 180))

/**
 * Modifier.fluidPulsingBadge:
 * Micro-pulse animation for active indicators or warning badges.
 */
fun Modifier.fluidPulsingBadge(enabled: Boolean = true): Modifier = composed {
    if (!enabled) return@composed this

    val scaleAnim = remember { Animatable(1f) }

    LaunchedEffect(enabled) {
        if (enabled) {
            while (true) {
                scaleAnim.animateTo(
                    targetValue = 1.08f,
                    animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing)
                )
                scaleAnim.animateTo(
                    targetValue = 1.0f,
                    animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing)
                )
            }
        }
    }

    this.scale(scaleAnim.value)
}
