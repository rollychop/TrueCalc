package com.brohit.truecalc.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * System font (Roboto on most devices); no downloadable-font provider, so text renders offline
 * and without Play Services on first run.
 */
private val AppFontFamily = FontFamily.Default

private fun style(
    size: Int,
    lineHeight: Int,
    weight: FontWeight,
    letterSpacing: Double,
) = TextStyle(
    fontFamily = AppFontFamily,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    letterSpacing = letterSpacing.sp,
)

/** Full Material 3 type scale (M3 baseline sizes; titles/headlines slightly heavier). */
val AppTypography = Typography(
    displayLarge = style(57, 64, FontWeight.Normal, -0.25),
    displayMedium = style(45, 52, FontWeight.Normal, 0.0),
    displaySmall = style(36, 44, FontWeight.Normal, 0.0),
    headlineLarge = style(32, 40, FontWeight.SemiBold, 0.0),
    headlineMedium = style(28, 36, FontWeight.SemiBold, 0.0),
    headlineSmall = style(24, 32, FontWeight.SemiBold, 0.0),
    titleLarge = style(22, 28, FontWeight.SemiBold, 0.0),
    titleMedium = style(16, 24, FontWeight.SemiBold, 0.15),
    titleSmall = style(14, 20, FontWeight.Medium, 0.1),
    bodyLarge = style(16, 24, FontWeight.Normal, 0.5),
    bodyMedium = style(14, 20, FontWeight.Normal, 0.25),
    bodySmall = style(12, 16, FontWeight.Normal, 0.4),
    labelLarge = style(14, 20, FontWeight.Medium, 0.1),
    labelMedium = style(12, 16, FontWeight.Medium, 0.5),
    labelSmall = style(11, 16, FontWeight.Medium, 0.5),
)

/**
 * Number styles with tabular figures (`tnum`) so results don't jitter while typing.
 * Prefer the `Typography` extensions ([numberDisplay], [numberLarge], [numberMedium]) inside
 * composables; these constants are for non-composable contexts.
 */
object AppTextStyles {
    private const val TABULAR = "tnum"

    /** Hero result number. */
    val NumberDisplay: TextStyle = AppTypography.displaySmall.copy(
        fontWeight = FontWeight.SemiBold,
        fontFeatureSettings = TABULAR,
    )

    /** Secondary big numbers (e.g. summary tiles). */
    val NumberLarge: TextStyle = AppTypography.headlineSmall.copy(
        fontWeight = FontWeight.SemiBold,
        fontFeatureSettings = TABULAR,
    )

    /** Values in breakdown rows. */
    val NumberMedium: TextStyle = AppTypography.titleMedium.copy(
        fontFeatureSettings = TABULAR,
    )
}

/** Hero number derived from the current theme's displaySmall, with tabular figures. */
val Typography.numberDisplay: TextStyle
    get() = displaySmall.copy(fontWeight = FontWeight.SemiBold, fontFeatureSettings = "tnum")

/** Large number derived from headlineSmall, with tabular figures. */
val Typography.numberLarge: TextStyle
    get() = headlineSmall.copy(fontWeight = FontWeight.SemiBold, fontFeatureSettings = "tnum")

/** Row value number derived from titleMedium, with tabular figures. */
val Typography.numberMedium: TextStyle
    get() = titleMedium.copy(fontFeatureSettings = "tnum")
