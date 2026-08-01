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
    data object CategoriesScreen : Route()

    @Serializable
    data object FavoritesScreen : Route()

    @Serializable
    data class HistoryScreen(val toolRouteFilter: String? = null) : Route()

    @Serializable
    data object SettingsScreen : Route()

    @Serializable
    data class CategoryDetail(val categoryName: String) : Route()


}