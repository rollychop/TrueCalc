package com.brohit.truecalc.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class Route {
    @Serializable
    data object MainScreen : Route()

    @Serializable
    data object SearchScreen : Route()


    @Serializable
    data class EmiCalculator(val restorationJson: String? = null) : Route()

    @Serializable
    data object EmiSettings : Route()

    @Serializable
    data class CompoundInterestCalculator(
        val calculationType: String,
        val restorationJson: String? = null
    ) : Route()


    @Serializable
    data class RecurringDepositCalculator(val restorationJson: String? = null) : Route()

    //health

    @Serializable
    data class BmiCalculator(val restorationJson: String? = null) : Route()

    @Serializable
    data class CalorieCalculator(val restorationJson: String? = null) : Route()

    @Serializable
    data class IdealWeightCalculator(val restorationJson: String? = null) : Route()

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