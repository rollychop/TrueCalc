package com.brohit.truecalc.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.brohit.truecalc.ui.screens.compund_interest.CompoundInterestScreen
import com.brohit.truecalc.ui.screens.emi.EmiCalculatorScreen
import com.brohit.truecalc.ui.screens.emi.EmiSettingsScreen
import com.brohit.truecalc.ui.screens.home.HomeScreen
import com.brohit.truecalc.ui.screens.recurring_deposit.RecurringDepositScreen

fun NavGraphBuilder.createNavGraph(navigator: AppNavigator) {

    composable<Route.MainScreen> {
        HomeScreen(navigator)
    }

    composable<Route.EmiCalculator> {
        EmiCalculatorScreen(navigator)
    }
    composable<Route.EmiSettings> {
        EmiSettingsScreen(navigator)
    }

    composable<Route.CompoundInterestCalculator> {
        CompoundInterestScreen(navigator)
    }
    composable<Route.RecurringDepositCalculator> {
        RecurringDepositScreen(navigator)
    }
}