package com.brohit.truecalc.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class Route {
    @Serializable
    data object MainScreen : Route()


    @Serializable
    data object EmiCalculator : Route()

    @Serializable
    data object EmiSettings : Route()

    @Serializable
    data object CompoundInterestCalculator : Route()
}