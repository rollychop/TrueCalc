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
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Timeline
import com.brohit.truecalc.ui.navigation.Route
import com.brohit.truecalc.ui.screens.compund_interest.CalculationType

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
                Route.EmiCalculator,
                Icons.Filled.AttachMoney
            ),
            Calculator(
                "Investment Calculator",
                "Calculate investment growth",
                Route.EmiCalculator,
                Icons.AutoMirrored.Filled.TrendingUp
            ),
            Calculator(
                "Savings Calculator",
                "Calculate savings goals",
                Route.EmiCalculator,
                Icons.Filled.Savings
            ),
            Calculator(
                "Mortgage Calculator",
                "Calculate mortgage payments",
                Route.EmiCalculator,
                Icons.Filled.Home
            ),
            Calculator(
                "Compound Interest Calculator",
                "Calculate compound interest",
                Route.EmiCalculator,
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
                Route.EmiCalculator,
                Icons.Filled.Accessibility
            ),
            Calculator(
                "Calorie Calculator",
                "Estimate daily calorie needs",
                Route.EmiCalculator,
                Icons.Filled.Restaurant
            ),
            Calculator(
                "Ideal Weight Calculator",
                "Calculate your ideal weight",
                Route.EmiCalculator,
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
                Route.EmiCalculator,
                Icons.Filled.CurrencyExchange
            ),
            Calculator(
                "Temperature Converter",
                "Convert between temperature units",
                Route.EmiCalculator,
                Icons.Filled.Thermostat
            ),
            Calculator(
                "Area Converter",
                "Convert between area units",
                Route.EmiCalculator,
                Icons.Filled.CropSquare
            ),
            Calculator(
                "Volume Converter",
                "Convert between volume units",
                Route.EmiCalculator,
                Icons.Filled.Dehaze
            ),
            Calculator(
                "Weight Converter",
                "Convert between weight units",
                Route.EmiCalculator,
                Icons.Filled.LineWeight
            ),
            Calculator(
                "Length Converter",
                "Convert between length units",
                Route.EmiCalculator,
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
                Route.EmiCalculator,
                Icons.Filled.Percent
            ),
            Calculator(
                "Ratio Calculator",
                "Calculate ratios",
                Route.EmiCalculator,
                Icons.Filled.Timeline
            ),
            Calculator(
                "Average Calculator",
                "Calculate average",
                Route.EmiCalculator,
                Icons.Filled.Functions
            ),
            Calculator(
                "Scientific Calculator",
                "Perform scientific calculations",
                Route.EmiCalculator,
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
                name = "EMI Calculator",
                description = "Calculate EMI payments",
                route = Route.EmiCalculator,
                icon = Icons.Filled.AttachMoney
            ),
            Calculator(
                name = "Compound Interest",
                description = "Calculates compound interest",
                route = Route.CompoundInterestCalculator(calculationType = CalculationType.COMPOUND),
                icon = Icons.Filled.MonetizationOn
            ),
            Calculator(
                name = "Simple Interest",
                description = "Calculates simple interest",
                route = Route.CompoundInterestCalculator(calculationType = CalculationType.SIMPLE),
                icon = Icons.Filled.Money
            ),
            Calculator(
                name = "Fixed Deposit",
                description = "Calculates fixed deposits",
                route = Route.CompoundInterestCalculator(calculationType = CalculationType.FIXED),
                icon = Icons.Filled.AttachMoney
            ),
            Calculator(
                name = "Recurring Deposit",
                description = "Calculates recurring interest",
                route = Route.RecurringDepositCalculator,
                icon = Icons.Filled.Timeline
            ),
        )
    ),
)