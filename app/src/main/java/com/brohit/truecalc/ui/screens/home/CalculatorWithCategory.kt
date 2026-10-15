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

fun Route.toCalculator(): Calculator? {
    return calculatorsWithCategories.flatMap { it.calculators }.find { it.route.isSameTool(this) }
}

/**
 * Identifies the tool a route points to, ignoring restoration state.
 * CompoundInterest backs several tools, so its calculationType is part of the identity.
 */
fun Route.isSameTool(other: Route): Boolean {
    if (this::class != other::class) return false
    if (this is Route.Calculator.CompoundInterest && other is Route.Calculator.CompoundInterest) {
        return calculationType == other.calculationType
    }
    return true
}

@Suppress("unused")
val calculatorsWithCategories1: List<CalculatorWithCategory> = listOf(
    CalculatorWithCategory(
        category = "Financial",
        calculators = listOf(
            Calculator(
                "EMI Calculator",
                "Calculate EMI payments",
                Route.Calculator.Emi(),
                Icons.Filled.AttachMoney
            ),
            Calculator(
                "Investment Calculator",
                "Calculate investment growth",
                Route.Calculator.Emi(),
                Icons.AutoMirrored.Filled.TrendingUp
            ),
            Calculator(
                "Savings Calculator",
                "Calculate savings goals",
                Route.Calculator.Emi(),
                Icons.Filled.Savings
            ),
            Calculator(
                "Mortgage Calculator",
                "Calculate mortgage payments",
                Route.Calculator.Emi(),
                Icons.Filled.Home
            ),
            Calculator(
                "Compound Interest Calculator",
                "Calculate compound interest",
                Route.Calculator.Emi(),
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
                Route.Calculator.Emi(),
                Icons.Filled.Accessibility
            ),
            Calculator(
                "Calorie Calculator",
                "Estimate daily calorie needs",
                Route.Calculator.Emi(),
                Icons.Filled.Restaurant
            ),
            Calculator(
                "Ideal Weight Calculator",
                "Calculate your ideal weight",
                Route.Calculator.Emi(),
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
                Route.Calculator.Emi(),
                Icons.Filled.CurrencyExchange
            ),
            Calculator(
                "Temperature Converter",
                "Convert between temperature units",
                Route.Calculator.Emi(),
                Icons.Filled.Thermostat
            ),
            Calculator(
                "Area Converter",
                "Convert between area units",
                Route.Calculator.Emi(),
                Icons.Filled.CropSquare
            ),
            Calculator(
                "Volume Converter",
                "Convert between volume units",
                Route.Calculator.Emi(),
                Icons.Filled.Dehaze
            ),
            Calculator(
                "Weight Converter",
                "Convert between weight units",
                Route.Calculator.Emi(),
                Icons.Filled.LineWeight
            ),
            Calculator(
                "Length Converter",
                "Convert between length units",
                Route.Calculator.Emi(),
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
                Route.Calculator.Emi(),
                Icons.Filled.Percent
            ),
            Calculator(
                "Ratio Calculator",
                "Calculate ratios",
                Route.Calculator.Emi(),
                Icons.Filled.Timeline
            ),
            Calculator(
                "Average Calculator",
                "Calculate average",
                Route.Calculator.Emi(),
                Icons.Filled.Functions
            ),
            Calculator(
                "Scientific Calculator",
                "Perform scientific calculations",
                Route.Calculator.Emi(),
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
                route = Route.Calculator.Emi(),
                icon = Icons.Filled.AttachMoney
            ),
            Calculator(
                name = "Compound Interest",
                description = "Calculates compound interest",
                route = Route.Calculator.CompoundInterest(calculationType = CalculationType.COMPOUND),
                icon = Icons.Filled.MonetizationOn
            ),
            Calculator(
                name = "Simple Interest",
                description = "Calculates simple interest",
                route = Route.Calculator.CompoundInterest(calculationType = CalculationType.SIMPLE),
                icon = Icons.Filled.Money
            ),
            Calculator(
                name = "Fixed Deposit",
                description = "Calculates fixed deposits",
                route = Route.Calculator.CompoundInterest(calculationType = CalculationType.FIXED),
                icon = Icons.Filled.AttachMoney
            ),
            Calculator(
                name = "Recurring Deposit",
                description = "Calculates recurring interest",
                route = Route.Calculator.RecurringDeposit(),
                icon = Icons.Filled.Timeline
            ),
        )
    ),
    CalculatorWithCategory(
        category = "Health & Fitness",
        calculators = listOf(
            Calculator(
                "BMI Calculator",
                "Calculate your Body Mass Index",
                Route.Calculator.Bmi(),
                Icons.Filled.Accessibility
            ),
            Calculator(
                "Calorie Calculator",
                "Estimate daily calorie needs",
                Route.Calculator.Calorie(),
                Icons.Filled.Restaurant
            ),
            Calculator(
                "Ideal Weight Calculator",
                "Calculate your ideal weight",
                Route.Calculator.IdealWeight(),
                Icons.Filled.Scale
            )
        )
    ),
)
