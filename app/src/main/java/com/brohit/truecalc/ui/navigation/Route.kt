package com.brohit.truecalc.ui.navigation

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
sealed class Route {
    @Serializable
    data object MainScreen : Route()

    @Serializable
    data object SearchScreen : Route()

    @Serializable
    sealed class Calculator : Route() {
        @Serializable
        @SerialName("EmiCalculator")
        data class Emi(val restorationJson: String? = null) : Calculator()

        @Serializable
        @SerialName("CompoundInterestCalculator")
        data class CompoundInterest(
            val calculationType: String,
            val restorationJson: String? = null
        ) : Calculator()

        @Serializable
        @SerialName("RecurringDepositCalculator")
        data class RecurringDeposit(val restorationJson: String? = null) : Calculator()

        @Serializable
        @SerialName("BmiCalculator")
        data class Bmi(val restorationJson: String? = null) : Calculator()

        @Serializable
        @SerialName("CalorieCalculator")
        data class Calorie(val restorationJson: String? = null) : Calculator()

        @Serializable
        @SerialName("IdealWeightCalculator")
        data class IdealWeight(val restorationJson: String? = null) : Calculator()

        // Everyday tools pack

        /** Basic / scientific calculator. */
        @Serializable
        @SerialName("BasicCalculator")
        data class Basic(val restorationJson: String? = null) : Calculator()

        @Serializable
        @SerialName("PercentageCalculator")
        data class Percentage(val restorationJson: String? = null) : Calculator()

        @Serializable
        @SerialName("DiscountCalculator")
        data class Discount(val restorationJson: String? = null) : Calculator()

        @Serializable
        @SerialName("GstCalculator")
        data class Gst(val restorationJson: String? = null) : Calculator()

        @Serializable
        @SerialName("SplitBillCalculator")
        data class SplitBill(val restorationJson: String? = null) : Calculator()

        @Serializable
        @SerialName("UnitConverter")
        data class UnitConverter(val restorationJson: String? = null) : Calculator()

        @Serializable
        @SerialName("AgeCalculator")
        data class Age(val restorationJson: String? = null) : Calculator()

        @Serializable
        @SerialName("DateCalculator")
        data class DateCalc(val restorationJson: String? = null) : Calculator()
    }

    @Serializable
    data object EmiSettings : Route()

    @Serializable
    data class HistoryScreen(val toolRouteFilter: String? = null) : Route()

    @Serializable
    data object SettingsScreen : Route()

    /** A category's tool list. [categoryName] is the stable `ToolCategory` enum name, e.g. "Finance". */
    @Serializable
    data class CategoryDetail(val categoryName: String) : Route()
}

/**
 * This calculator route carrying [restorationJson] (from a history entry), so the screen reopens
 * with those inputs. Non-calculator routes are returned unchanged.
 */
fun Route.withRestorationJson(restorationJson: String): Route = when (this) {
    is Route.Calculator.Emi -> copy(restorationJson = restorationJson)
    is Route.Calculator.CompoundInterest -> copy(restorationJson = restorationJson)
    is Route.Calculator.RecurringDeposit -> copy(restorationJson = restorationJson)
    is Route.Calculator.Bmi -> copy(restorationJson = restorationJson)
    is Route.Calculator.Calorie -> copy(restorationJson = restorationJson)
    is Route.Calculator.IdealWeight -> copy(restorationJson = restorationJson)
    is Route.Calculator.Basic -> copy(restorationJson = restorationJson)
    is Route.Calculator.Percentage -> copy(restorationJson = restorationJson)
    is Route.Calculator.Discount -> copy(restorationJson = restorationJson)
    is Route.Calculator.Gst -> copy(restorationJson = restorationJson)
    is Route.Calculator.SplitBill -> copy(restorationJson = restorationJson)
    is Route.Calculator.UnitConverter -> copy(restorationJson = restorationJson)
    is Route.Calculator.Age -> copy(restorationJson = restorationJson)
    is Route.Calculator.DateCalc -> copy(restorationJson = restorationJson)
    else -> this
}
