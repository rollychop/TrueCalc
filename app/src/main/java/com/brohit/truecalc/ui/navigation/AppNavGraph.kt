package com.brohit.truecalc.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.brohit.truecalc.ui.screens.compund_interest.CompoundInterestScreen
import com.brohit.truecalc.ui.screens.emi.EmiCalculatorScreen
import com.brohit.truecalc.ui.screens.emi.EmiSettingsScreen
import com.brohit.truecalc.ui.screens.health.bmi.BMICalculatorScreen
import com.brohit.truecalc.ui.screens.health.calorie.CaloriesCalculatorScreen
import com.brohit.truecalc.ui.screens.health.ideal_weight.IdealWeightCalculatorScreen
import com.brohit.truecalc.ui.screens.history.HistoryScreen
import com.brohit.truecalc.ui.screens.home.CategoryDetailScreen
import com.brohit.truecalc.ui.screens.home.HomeScreen
import com.brohit.truecalc.ui.screens.recurring_deposit.RecurringDepositScreen
import com.brohit.truecalc.ui.screens.search.SearchScreen
import com.brohit.truecalc.ui.screens.settings.SettingsScreen

fun NavGraphBuilder.createNavGraph(navigator: AppNavigator) {

    composable<Route.MainScreen> {
        HomeScreen(navigator)
    }
    composable<Route.SearchScreen> {
        SearchScreen(navigator)
    }

    composable<Route.Calculator.Emi> {
        EmiCalculatorScreen(navigator)
    }
    composable<Route.EmiSettings> {
        EmiSettingsScreen(navigator)
    }

    composable<Route.Calculator.CompoundInterest> {
        CompoundInterestScreen(navigator)
    }
    composable<Route.Calculator.RecurringDeposit> {
        RecurringDepositScreen(navigator)
    }

    //health screens
    composable<Route.Calculator.Bmi> {
        BMICalculatorScreen(navigator)
    }
    composable<Route.Calculator.Calorie> {
        CaloriesCalculatorScreen(navigator)
    }
    composable<Route.Calculator.IdealWeight> {
        IdealWeightCalculatorScreen(navigator)
    }

    composable<Route.HistoryScreen> { backStackEntry ->
        val route: Route.HistoryScreen = backStackEntry.toRoute()
        HistoryScreen(
            navigator = navigator,
            toolRouteFilter = route.toolRouteFilter
        )
    }
    composable<Route.SettingsScreen> {
        SettingsScreen()
    }

    composable<Route.CategoryDetail> { backStackEntry ->
        val route: Route.CategoryDetail = backStackEntry.toRoute()
        CategoryDetailScreen(
            categoryKey = route.categoryName,
            navigator = navigator
        )
    }
}
