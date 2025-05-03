package com.brohit.truecalc.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class Route {
    @Serializable
    data object MainScreen : Route()

    @Serializable
    data object SearchScreen : Route()


    @Serializable
    data object EmiCalculator : Route()

    @Serializable
    data object EmiSettings : Route()

    @Serializable
    data class CompoundInterestCalculator(val calculationType: String) : Route()


    @Serializable
    data object RecurringDepositCalculator : Route()

    //health

    @Serializable
    data object BmiCalculator : Route()

    @Serializable
    data object CalorieCalculator : Route()

    @Serializable
    data object IdealWeightCalculator : Route()


}