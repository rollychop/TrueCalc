package com.brohit.truecalc.ui.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import com.brohit.truecalc.ui.theme.PrimaryBlue
import com.brohit.truecalc.ui.theme.SuccessGreen
import com.brohit.truecalc.ui.theme.TonalAmber
import com.brohit.truecalc.ui.theme.TonalAmberDark
import com.brohit.truecalc.ui.theme.TonalBlue
import com.brohit.truecalc.ui.theme.TonalBlueDark
import com.brohit.truecalc.ui.theme.TonalGreen
import com.brohit.truecalc.ui.theme.TonalGreenDark
import com.brohit.truecalc.ui.theme.TonalPurple
import com.brohit.truecalc.ui.theme.TonalPurpleDark
import com.brohit.truecalc.ui.theme.TonalRed
import com.brohit.truecalc.ui.theme.TonalRedDark
import com.brohit.truecalc.ui.theme.WarningAmber

@Composable
fun getToolColors(name: String): Pair<Color, Color> {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    
    return when {
        name.contains("EMI", true) || name.contains("Interest", true) || name.contains("Deposit", true) -> {
            if (isDark) TonalBlueDark to Color(0xFF60A5FA) else TonalBlue to PrimaryBlue
        }
        name.contains("BMI", true) || name.contains("Health", true) || name.contains("Weight", true) || name.contains("Calorie", true) -> {
            if (isDark) TonalGreenDark to Color(0xFF34D399) else TonalGreen to SuccessGreen
        }
        name.contains("Converter", true) || name.contains("Unit", true) -> {
            if (isDark) TonalPurpleDark to Color(0xFFA78BFA) else TonalPurple to Color(0xFF8B5CF6)
        }
        name.contains("Math", true) || name.contains("Percentage", true) || name.contains("Ratio", true) -> {
            if (isDark) TonalAmberDark to Color(0xFFFBBF24) else TonalAmber to WarningAmber
        }
        else -> {
            if (isDark) TonalRedDark to Color(0xFFF87171) else TonalRed to Color(0xFFEF4444)
        }
    }
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
