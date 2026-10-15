package com.brohit.truecalc.ui.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Percent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.brohit.truecalc.ui.tools.ToolCategory
import com.brohit.truecalc.ui.tools.ToolRegistry
import com.brohit.truecalc.ui.tools.categoryAccentColors
import com.brohit.truecalc.ui.tools.categoryColors

/** Card colours for a tool or category tile, all from the theme's colour roles. */
@Immutable
data class ToolColors(
    /** Card background (category container colour). */
    val background: Color,
    /** Filled circle behind the icon (category accent). */
    val iconContainer: Color,
    /** Icon tint on [iconContainer]. */
    val iconContent: Color,
)

/**
 * Colours for a tool tile (by tool name) or a category card (by legacy category name), derived
 * from the tool's [ToolCategory] via [categoryColors] / [categoryAccentColors].
 */
@Composable
fun getToolColors(name: String): ToolColors {
    val category = toolCategoryFor(name)
    val (container, _) = categoryColors(category)
    val (accent, onAccent) = categoryAccentColors(category)
    return ToolColors(background = container, iconContainer = accent, iconContent = onAccent)
}

/** Maps a registered tool name, or a legacy category name ("Financial"…), to its category. */
fun toolCategoryFor(name: String): ToolCategory =
    ToolRegistry.all.firstOrNull { it.name == name }?.category
        ?: when (name) {
            "Financial" -> ToolCategory.Finance
            "Health & Fitness" -> ToolCategory.Health
            "Unit Conversion" -> ToolCategory.Convert
            "Math" -> ToolCategory.Math
            else -> ToolCategory.entries.firstOrNull { it.title == name } ?: ToolCategory.Everyday
        }

fun getCategoryIcon(category: String): ImageVector {
    return when (category) {
        "Financial" -> Icons.Default.MonetizationOn
        "Health & Fitness" -> Icons.Default.Accessibility
        "Unit Conversion" -> Icons.Default.CurrencyExchange
        "Math" -> Icons.Default.Percent
        else -> Icons.Default.Category
    }
}

fun getCategorySummary(category: String): String {
    return when (category) {
        "Financial" -> "EMI, Interest, FD, RD"
        "Health & Fitness" -> "BMI, Calories, Ideal Weight"
        "Unit Conversion" -> "Currency, Temp, Weight, Length"
        "Math" -> "Percentage, Ratio, Average"
        else -> "All tools"
    }
}
