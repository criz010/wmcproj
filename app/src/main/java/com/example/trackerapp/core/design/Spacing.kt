package com.example.trackerapp.core.design

import androidx.compose.ui.unit.dp

/**
 * TrackerApp Spacing System
 *
 * Based on 4dp base unit with 8dp grid system
 * Provides consistent spacing throughout the app
 *
 * Naming Convention:
 * - XXS to XXL for standard spacing
 * - Custom named spacing for specific use cases
 */
object Spacing {
    // Base spacing unit
    val baseUnit = 4.dp

    // Standard spacing scale
    val xxxs = 2.dp   // 0.5x base
    val xxs = 4.dp    // 1x base
    val xs = 8.dp     // 2x base
    val sm = 12.dp    // 3x base
    val md = 16.dp    // 4x base (primary spacing)
    val lg = 24.dp    // 6x base
    val xl = 32.dp    // 8x base
    val xxl = 48.dp   // 12x base
    val xxxl = 64.dp  // 16x base

    // Component-specific spacing
    val cardPadding = md
    val screenPadding = md
    val buttonPadding = md
    val iconPadding = xs

    // Element spacing
    val elementSpacing = xs
    val sectionSpacing = lg
    val heroSpacing = xxl

    // Card elevation
    val elevationNone = 0.dp
    val elevationLow = 2.dp
    val elevationMedium = 4.dp
    val elevationHigh = 8.dp
    val elevationVeryHigh = 16.dp
}

/**
 * Rounded corner radii
 */
object CornerRadius {
    val none = 0.dp
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 24.dp
    val xxl = 32.dp
    val full = 999.dp
}

/**
 * Border widths
 */
object BorderWidth {
    val none = 0.dp
    val thin = 1.dp
    val medium = 2.dp
    val thick = 4.dp
}
