package com.brohit.truecalc.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.brohit.truecalc.ui.screens.HistoryScreen
import com.brohit.truecalc.ui.screens.settings.SettingsScreen
import com.brohit.truecalc.ui.screens.categories.CategoriesScreen
import com.brohit.truecalc.ui.screens.compund_interest.CompoundInterestScreen
import com.brohit.truecalc.ui.screens.emi.EmiCalculatorScreen
import com.brohit.truecalc.ui.screens.emi.EmiSettingsScreen
import com.brohit.truecalc.ui.screens.health.bmi.BMICalculatorScreen
import com.brohit.truecalc.ui.screens.health.calorie.CaloriesCalculatorScreen
import com.brohit.truecalc.ui.screens.health.ideal_weight.IdealWeightCalculatorScreen
import com.brohit.truecalc.ui.screens.home.CategoryDetailScreen
import com.brohit.truecalc.ui.screens.home.HomeScreen
import com.brohit.truecalc.ui.screens.favorites.FavoritesScreen
import com.brohit.truecalc.ui.screens.recurring_deposit.RecurringDepositScreen
import com.brohit.truecalc.ui.screens.search.SearchScreen

fun NavGraphBuilder.createNavGraph(navigator: AppNavigator) {

    composable<Route.MainScreen> {
        HomeScreen(navigator)
    }
    composable<Route.SearchScreen> {
        SearchScreen(navigator)
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

    //health screens
    composable<Route.BmiCalculator> {
        BMICalculatorScreen(navigator)
    }
    composable<Route.CalorieCalculator> {
        CaloriesCalculatorScreen(navigator)
    }
    composable<Route.IdealWeightCalculator> {
        IdealWeightCalculatorScreen(navigator)
    }

    composable<Route.CategoriesScreen> {
        CategoriesScreen(navigator)
    }
    composable<Route.FavoritesScreen> {
        FavoritesScreen(navigator)
    }
    composable<Route.HistoryScreen> {
        HistoryScreen()
    }
    composable<Route.SettingsScreen> {
        SettingsScreen()
    }

    composable<Route.CategoryDetail> { backStackEntry ->
        val route: Route.CategoryDetail = backStackEntry.toRoute()
        CategoryDetailScreen(
            categoryName = route.categoryName,
            navigator = navigator
        )
    }
}
