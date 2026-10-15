package com.brohit.truecalc.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Colour roles Material 3 doesn't have. Currently one accent (amber) for the Convert and Math
 * tool categories, shaped like a scheme colour group.
 *
 * Read with `LocalExtendedColors.current` inside [TrueCalcTheme].
 */
@Immutable
data class ExtendedColors(
    val accent: Color,
    val onAccent: Color,
    val accentContainer: Color,
    val onAccentContainer: Color,
)

internal val LightExtendedColors = ExtendedColors(
    accent = LightExtendedAccent,
    onAccent = LightExtendedOnAccent,
    accentContainer = LightExtendedAccentContainer,
    onAccentContainer = LightExtendedOnAccentContainer,
)

internal val DarkExtendedColors = ExtendedColors(
    accent = DarkExtendedAccent,
    onAccent = DarkExtendedOnAccent,
    accentContainer = DarkExtendedAccentContainer,
    onAccentContainer = DarkExtendedOnAccentContainer,
)

/** Defaults to the light set so previews without [TrueCalcTheme] still render. */
val LocalExtendedColors = staticCompositionLocalOf { LightExtendedColors }
