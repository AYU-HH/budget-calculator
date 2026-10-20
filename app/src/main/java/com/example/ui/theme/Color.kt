package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// ============================================================================
// Apple Design System Tokens (per Apple-design-analysis specification)
// ============================================================================

// Brand & Accent Colors
val AppleActionBlue = Color(0xFF0066CC)       // Action Blue - Single universal interactive color
val ApplePrimaryFocus = Color(0xFF0071E3)     // Focus Blue
val AppleSkyBlue = Color(0xFF2997FF)          // Sky Link Blue on dark surfaces
val AppleSuccessGreen = Color(0xFF34C759)     // Apple iOS System Green
val AppleDangerRed = Color(0xFFFF3B30)        // Apple iOS System Red
val AppleWarningOrange = Color(0xFFFF9500)    // Apple iOS System Orange
val AppleAccentPurple = Color(0xFF5856D6)     // Apple iOS System Indigo/Purple

// Typography & Text Ink
val AppleInk = Color(0xFF1D1D1F)              // Near-black ink for headlines and body on light
val AppleBody = Color(0xFF1D1D1F)
val AppleBodyOnDark = Color(0xFFFFFFFF)
val AppleBodyMuted = Color(0xFF86868B)
val AppleInkMuted80 = Color(0xFF333333)
val AppleInkMuted48 = Color(0xFF7A7A7A)

// Surfaces & Canvases
val AppleCanvas = Color(0xFFFFFFFF)           // Pure white canvas
val AppleParchment = Color(0xFFF5F5F7)        // Apple signature off-white canvas
val AppleSurfacePearl = Color(0xFFFAFAFC)     // Pearl secondary surface
val AppleTile1 = Color(0xFF272729)            // Primary dark-tile surface
val AppleTile2 = Color(0xFF2A2A2C)            // Secondary dark tile
val AppleTile3 = Color(0xFF1C1C1E)            // Tertiary dark / deep background
val AppleSurfaceBlack = Color(0xFF000000)     // Pure Black void (OLED / Nav)
val AppleSurfaceChip = Color(0xFFD2D2D7)      // Translucent chip base

// Hairlines & Dividers (Zero elevation - hairlines create distinction)
val AppleDividerSoft = Color(0xFFF0F0F0)
val AppleHairline = Color(0xFFE0E0E0)
val AppleHairlineDark = Color(0xFF38383A)
val AppleHairlineOled = Color(0xFF28282A)

// ============================================================================
// Semantic & Legacy Compatibility Mappings
// ============================================================================
val BrandPurple = AppleActionBlue
val BrandPurpleDark = AppleActionBlue
val BrandPurpleLight = AppleSkyBlue
val BrandPurpleSubtle = Color(0xFFE5F1FF)

// Stat Highlights
val IncomeBlue = AppleActionBlue
val ExpensesOrange = AppleWarningOrange
val RemainingGreen = AppleSuccessGreen
val DangerRed = AppleDangerRed
val ExpenseRed = AppleDangerRed
val ProgressBarTrack = Color(0xFFE5E5EA)

// Light Theme Colors (Apple Canvas & Parchment)
val LightBackground = AppleParchment
val LightSurface = AppleCanvas
val LightSurfaceVariant = AppleSurfacePearl
val LightOnBackground = AppleInk
val LightOnSurface = AppleInk
val LightOutline = AppleHairline

// Dark Theme Colors (Apple Tile 1 & Tile 3)
val DarkBackground = AppleTile3
val DarkSurface = AppleTile1
val DarkSurfaceVariant = AppleTile2
val DarkOnBackground = AppleBodyOnDark
val DarkOnSurface = AppleBodyOnDark
val DarkOutline = AppleHairlineDark

// Apple Calculator Specific Colors (iOS Calculator Palette)
val CalcScreenBg = Color(0xFF000000)
val CalcKeyNumberBg = Color(0xFF333333)
val CalcKeyNumberBgLight = Color(0xFFE5E5EA)
val CalcKeyActionBg = Color(0xFFA5A5A5)
val CalcKeyActionBgLight = Color(0xFFD1D1D6)
val CalcKeyOperatorBg = AppleWarningOrange
val CalcKeyClearBg = Color(0xFFA5A5A5)
val CalcKeyEqualsBg = AppleWarningOrange

