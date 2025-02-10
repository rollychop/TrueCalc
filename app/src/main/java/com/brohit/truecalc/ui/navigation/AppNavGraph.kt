package com.brohit.truecalc.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.brohit.truecalc.ui.screens.emi.EmiCalculatorScreen
import com.brohit.truecalc.ui.screens.emi.EmiSettingsScreen
import com.brohit.truecalc.ui.screens.home.HomeScreen

fun NavGraphBuilder.createNavGraph(navigator: AppNavigator) {

    composable<Route.MainScreen> {
        HomeScreen(navigator)
    }

    composable<Route.Calculator> {
        EmiCalculatorScreen(navigator)
    }
    composable<Route.Settings> {
        EmiSettingsScreen(navigator)
    }
}