package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.animation.applePressEffect

/**
 * Clean Apple Design System Surfaces & Cards.
 * Liquid glass effects (animated fluid orbs, glossy caustics sheen, specular bevel refractions)
 * have been completely removed in favor of clean, modern iOS-style surfaces:
 * - Crisp subtle borders (0.5dp - 1dp)
 * - Clean solid / subtle tinted elevated surfaces
 * - Soft, natural drop shadows
 * - Apple tactile spring press physics (Modifier.applePressEffect)
 */

/**
 * Clean system backdrop replacing the animated liquid orbs.
 * Provides a calm, distraction-free neutral surface consistent with Apple HIG.
 */
@Composable
fun LiquidBackdrop(
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val bgColor = if (isDarkTheme) {
        Color(0xFF0F121C)
    } else {
        Color(0xFFF6F8FC)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        content()
    }
}

/**
 * Clean card modifier providing Apple-style elevation, subtle borders, and smooth corners.
 * Replaces previous liquid caustics sheen and specular refraction overlays.
 */
fun Modifier.liquidGlass(
    shape: Shape = RoundedCornerShape(16.dp),
    tint: Color = Color.Unspecified,
    tintAlpha: Float = 0.08f,
    borderWidth: Dp = 1.dp,
    borderAlpha: Float = 1.0f,
    elevation: Dp = 1.dp,
    isDark: Boolean = false,
    liquidSheen: Boolean = false
): Modifier = composed {
    val isLight = !isDark

    val surfaceColor = if (tint != Color.Unspecified) {
        if (isLight) {
            Color.White
        } else {
            Color(0xFF1A1F2E)
        }
    } else {
        if (isLight) {
            Color.White
        } else {
            Color(0xFF181C2A)
        }
    }

    val overlayTint = if (tint != Color.Unspecified) {
        tint.copy(alpha = if (isLight) tintAlpha else tintAlpha * 0.8f)
    } else {
        Color.Transparent
    }

    val borderColor = if (isLight) {
        Color(0xFFE2E8F0).copy(alpha = 0.8f * borderAlpha)
    } else {
        Color(0xFF2E384D).copy(alpha = 0.7f * borderAlpha)
    }

    val shadowModifier = if (elevation > 0.dp) {
        Modifier.shadow(
            elevation = elevation,
            shape = shape,
            ambientColor = if (isLight) Color(0x0D000000) else Color(0x2B000000),
            spotColor = if (isLight) Color(0x1A0F172A) else Color(0x40000000)
        )
    } else Modifier

    this
        .then(shadowModifier)
        .clip(shape)
        .background(surfaceColor)
        .then(
            if (tint != Color.Unspecified) {
                Modifier.background(overlayTint)
            } else Modifier
        )
        .border(borderWidth, borderColor, shape)
}

/**
 * Clean Apple-style Card container:
 * Crisp borders, soft shadows, rounded corners, and tactile press spring physics.
 */
@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(18.dp),
    tint: Color = Color.Unspecified,
    tintAlpha: Float = 0.08f,
    borderWidth: Dp = 1.dp,
    borderAlpha: Float = 0.85f,
    elevation: Dp = 1.5.dp,
    isDark: Boolean = false,
    liquidSheen: Boolean = false,
    pressScale: Float = 0.965f,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val clickableModifier = if (onClick != null) {
        Modifier
            .applePressEffect(scaleDown = pressScale)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = MaterialTheme.colorScheme.primary),
                onClick = onClick
            )
    } else Modifier

    Box(
        modifier = modifier
            .then(clickableModifier)
            .liquidGlass(
                shape = shape,
                tint = tint,
                tintAlpha = tintAlpha,
                borderWidth = borderWidth,
                borderAlpha = borderAlpha,
                elevation = elevation,
                isDark = isDark,
                liquidSheen = false
            )
    ) {
        content()
    }
}

/**
 * Clean iOS-style Badge:
 * Clean tinted pill background with subtle crisp border.
 */
@Composable
fun LiquidGlassBadge(
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.primary,
    shape: Shape = CircleShape,
    isDark: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
    val isLight = !isDark
    val bgTint = tint.copy(alpha = if (isLight) 0.12f else 0.20f)
    val borderColor = tint.copy(alpha = if (isLight) 0.25f else 0.35f)

    Box(
        modifier = modifier
            .clip(shape)
            .background(bgTint)
            .border(0.8.dp, borderColor, shape)
    ) {
        content()
    }
}
