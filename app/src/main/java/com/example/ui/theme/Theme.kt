package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.core.view.WindowCompat

// Modern Material 3 Color Scheme
val BudgetPurple = Color(0xFF6A1B9A)
val BudgetTeal = Color(0xFF03DAC5)
val BudgetGreen = Color(0xFF00C853)
val BudgetOrange = Color(0xFFFFA726)
val BudgetGray = Color(0xFF9E9E9E)
val BudgetLightGray = Color(0xFFE0E0E0)
val BudgetBlack = Color(0xFF000000)
val BudgetWhite = Color(0xFFFFFFFF)
val BudgetNeutral1 = Color(0xFFE8E8E8)
val BudgetNeutral2 = Color(0xFFBDBDBD)
val BudgetNeutral3 = Color(0xFF9E9E9E)
val BudgetNeutral4 = Color(0xFF616161)
val BudgetNeutral5 = Color(0xFF424242)

val BudgetPrimary = BudgetPurple
val BudgetSecondary = BudgetTeal
val BudgetTertiary = BudgetGreen

// Custom Font Family
val BudgetFontFamily = FontFamily(
    Font(R.font.roboto_medium, FontWeight.Medium),
    Font(R.font.roboto_bold, FontWeight.Bold),
    Font(R.font.roboto_italic, FontWeight.Italic),
    Font(R.font.roboto_light, FontWeight.Light)
)
private val AppleLightColorScheme = lightColorScheme(
    primary = AppleActionBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE5F1FF),
    onPrimaryContainer = AppleActionBlue,
    secondary = AppleActionBlue,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFEBEBEF),
    onSecondaryContainer = AppleInk,
    background = AppleParchment,
    onBackground = AppleInk,
    surface = AppleCanvas,
    onSurface = AppleInk,
    surfaceVariant = AppleSurfacePearl,
    onSurfaceVariant = AppleBodyMuted,
    outline = AppleHairline,
    outlineVariant = AppleDividerSoft,
    error = AppleDangerRed,
    onError = Color.White
)

// 2. Apple Dark: Apple Tile 1 (#272729) & Tile 3 (#1c1c1e), Sky Blue (#2997ff)
private val AppleDarkColorScheme = darkColorScheme(
    primary = AppleSkyBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF0F3A66),
    onPrimaryContainer = Color.White,
    secondary = AppleSkyBlue,
    onSecondary = Color.White,
    secondaryContainer = AppleTile2,
    onSecondaryContainer = Color.White,
    background = AppleTile3,
    onBackground = AppleBodyOnDark,
    surface = AppleTile1,
    onSurface = AppleBodyOnDark,
    surfaceVariant = AppleTile2,
    onSurfaceVariant = AppleBodyMuted,
    outline = AppleHairlineDark,
    outlineVariant = Color(0xFF2C2C2E),
    error = AppleDangerRed,
    onError = Color.White
)

// 3. Apple Midnight OLED: Pure Black Void (#000000), Tile 3 (#1c1c1e), Sky Blue (#2997ff)
private val AppleMidnightColorScheme = darkColorScheme(
    primary = AppleSkyBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF0F3A66),
    onPrimaryContainer = Color.White,
    secondary = AppleSkyBlue,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF161618),
    onSecondaryContainer = Color.White,
    background = AppleSurfaceBlack,
    onBackground = Color.White,
    surface = Color(0xFF141416),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF1C1C1E),
    onSurfaceVariant = Color(0xFFA1A1A6),
    outline = AppleHairlineOled,
    outlineVariant = Color(0xFF1E1E20),
    error = AppleDangerRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    themeMode: String = "SYSTEM",
    darkTheme: Boolean? = null,
    content: @Composable () -> Unit
) {
    val systemInDark = isSystemInDarkTheme()
    val isDark = when {
        darkTheme != null -> darkTheme
        themeMode == "LIGHT" -> false
        themeMode == "DARK" -> true
        themeMode == "MIDNIGHT" || themeMode == "OLED" -> true
        else -> systemInDark
    }

    val colorScheme = when {
        isDark -> darkColorScheme(
            primary = BudgetPrimary,
            primaryContainer = BudgetPrimary.copy(alpha = 0.8f),
            secondary = BudgetSecondary,
            secondaryContainer = BudgetSecondary.copy(alpha = 0.8f),
            tertiary = BudgetTertiary,
            surface = BudgetNeutral1,
            background = BudgetBlack.copy(alpha = 0.9f),
            onPrimary = BudgetWhite,
            onSecondary = BudgetWhite,
            onTertiary = BudgetWhite,
            onSurface = BudgetBlack,
            onBackground = BudgetWhite,
            error = Color.Red
        )
        else -> lightColorScheme(
            primary = BudgetPrimary,
            primaryContainer = BudgetPrimary.copy(alpha = 0.8f),
            secondary = BudgetSecondary,
            secondaryContainer = BudgetSecondary.copy(alpha = 0.8f),
            tertiary = BudgetTertiary,
            surface = BudgetWhite,
            background = BudgetLightGray,
            onPrimary = BudgetWhite,
            onSecondary = BudgetWhite,
            onTertiary = BudgetWhite,
            onSurface = BudgetBlack,
            onBackground = BudgetBlack,
            error = Color.Red
        )
    }

    val typography = androidx.compose.material3.Typography(
        displayLarge = androidx.compose.material3.Typography.h1.copy(
            fontFamily = BudgetFontFamily,
            fontWeight = FontWeight.Bold
        ),
        displayMedium = androidx.compose.material3.Typography.h2.copy(
            fontFamily = BudgetFontFamily,
            fontWeight = FontWeight.Bold
        ),
        displaySmall = androidx.compose.material3.Typography.h3.copy(
            fontFamily = BudgetFontFamily,
            fontWeight = FontWeight.Bold
        ),
        headlineLarge = androidx.compose.material3.Typography.h4.copy(
            fontFamily = BudgetFontFamily,
            fontWeight = FontWeight.Bold
        ),
        headlineMedium = androidx.compose.material3.Typography.h5.copy(
            fontFamily = BudgetFontFamily,
            fontWeight = FontWeight.Medium
        ),
        headlineSmall = androidx.compose.material3.Typography.h6.copy(
            fontFamily = BudgetFontFamily,
            fontWeight = FontWeight.Medium
        ),
        bodyLarge = androidx.compose.material3.Typography.bodyLarge.copy(
            fontFamily = BudgetFontFamily,
            fontWeight = FontWeight.Normal
        ),
        bodyMedium = androidx.compose.material3.Typography.bodyMedium.copy(
            fontFamily = BudgetFontFamily,
            fontWeight = FontWeight.Normal
        ),
        bodySmall = androidx.compose.material3.Typography.bodySmall.copy(
            fontFamily = BudgetFontFamily,
            fontWeight = FontWeight.Normal
        ),
        labelLarge = androidx.compose.material3.Typography.labelLarge.copy(
            fontFamily = BudgetFontFamily,
            fontWeight = FontWeight.Medium
        ),
        labelMedium = androidx.compose.material3.Typography.labelMedium.copy(
            fontFamily = BudgetFontFamily,
            fontWeight = FontWeight.Medium
        ),
        labelSmall = androidx.compose.material3.Typography.labelSmall.copy(
            fontFamily = BudgetFontFamily,
            fontWeight = FontWeight.Medium
        )
    )

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                window.navigationBarColor = colorScheme.background.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDark
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !isDark
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        content = content
    )
}

