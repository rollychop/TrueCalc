package com.brohit.truecalc.ui.screens.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CropSquare
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Dehaze
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LineWeight
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Timeline
import com.brohit.truecalc.ui.navigation.Route

data class CalculatorWithCategory(
    val category: String,
    val calculators: List<Calculator>
)

@Suppress("unused")
val calculatorsWithCategories1: List<CalculatorWithCategory> = listOf(
    CalculatorWithCategory(
        category = "Financial",
        calculators = listOf(
            Calculator(
                "EMI Calculator",
                "Calculate EMI payments",
                Route.Calculator,
                Icons.Filled.AttachMoney
            ),
            Calculator(
                "Investment Calculator",
                "Calculate investment growth",
                Route.Calculator,
                Icons.AutoMirrored.Filled.TrendingUp
            ),
            Calculator(
                "Savings Calculator",
                "Calculate savings goals",
                Route.Calculator,
                Icons.Filled.Savings
            ),
            Calculator(
                "Mortgage Calculator",
                "Calculate mortgage payments",
                Route.Calculator,
                Icons.Filled.Home
            ),
            Calculator(
                "Compound Interest Calculator",
                "Calculate compound interest",
                Route.Calculator,
                Icons.Filled.MonetizationOn
            )
        )
    ),
    CalculatorWithCategory(
        category = "Health & Fitness",
        calculators = listOf(
            Calculator(
                "BMI Calculator",
                "Calculate your Body Mass Index",
                Route.Calculator,
                Icons.Filled.Accessibility
            ),
            Calculator(
                "Calorie Calculator",
                "Estimate daily calorie needs",
                Route.Calculator,
                Icons.Filled.Restaurant
            ),
            Calculator(
                "Ideal Weight Calculator",
                "Calculate your ideal weight",
                Route.Calculator,
                Icons.Filled.Scale
            )
        )
    ),
    CalculatorWithCategory(
        category = "Unit Conversion",
        calculators = listOf(
            Calculator(
                "Currency Converter",
                "Convert between currencies",
                Route.Calculator,
                Icons.Filled.CurrencyExchange
            ),
            Calculator(
                "Temperature Converter",
                "Convert between temperature units",
                Route.Calculator,
                Icons.Filled.Thermostat
            ),
            Calculator(
                "Area Converter",
                "Convert between area units",
                Route.Calculator,
                Icons.Filled.CropSquare
            ),
            Calculator(
                "Volume Converter",
                "Convert between volume units",
                Route.Calculator,
                Icons.Filled.Dehaze
            ),
            Calculator(
                "Weight Converter",
                "Convert between weight units",
                Route.Calculator,
                Icons.Filled.LineWeight
            ),
            Calculator(
                "Length Converter",
                "Convert between length units",
                Route.Calculator,
                Icons.Filled.Straighten
            )
        )
    ),
    CalculatorWithCategory(
        category = "Math",
        calculators = listOf(
            Calculator(
                "Percentage Calculator",
                "Calculate percentages",
                Route.Calculator,
                Icons.Filled.Percent
            ),
            Calculator(
                "Ratio Calculator",
                "Calculate ratios",
                Route.Calculator,
                Icons.Filled.Timeline
            ),
            Calculator(
                "Average Calculator",
                "Calculate average",
                Route.Calculator,
                Icons.Filled.Functions
            ),
            Calculator(
                "Scientific Calculator",
                "Perform scientific calculations",
                Route.Calculator,
                Icons.Filled.Science
            )
        )
    ),
)

val calculatorsWithCategories: List<CalculatorWithCategory> = listOf(
    CalculatorWithCategory(
        category = "Financial",
        calculators = listOf(
            Calculator(
                "EMI Calculator",
                "Calculate EMI payments",
                Route.Calculator,
                Icons.Filled.AttachMoney
            )
        )
    ),
)