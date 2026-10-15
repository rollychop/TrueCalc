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
    else -> this
}
