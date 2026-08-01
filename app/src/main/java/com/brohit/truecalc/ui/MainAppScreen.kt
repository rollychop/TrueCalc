package com.brohit.truecalc.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.brohit.truecalc.domain.ThemeMode
import com.brohit.truecalc.ui.navigation.Route
import com.brohit.truecalc.ui.navigation.createNavGraph
import com.brohit.truecalc.ui.navigation.rememberNavigationController
import com.brohit.truecalc.ui.theme.TrueCalcTheme

@Composable
fun MainAppScreen(
    viewModel: MainViewModel = hiltViewModel()
) {
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val isDynamicColor by viewModel.isDynamicColorEnabled.collectAsStateWithLifecycle()
    
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    TrueCalcTheme(
        darkTheme = darkTheme,
        dynamicColor = isDynamicColor
    ) {
        val navController = rememberNavController()
        val navigationController = rememberNavigationController(navController)
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        val items = listOf(
            BottomNavItem("Home", Route.MainScreen, Icons.Rounded.Home),
            BottomNavItem("Categories", Route.CategoriesScreen, Icons.Rounded.Category),
            BottomNavItem("Favorites", Route.FavoritesScreen, Icons.Rounded.Favorite),
            BottomNavItem("History", Route.HistoryScreen(), Icons.Rounded.History),
            BottomNavItem("Settings", Route.SettingsScreen, Icons.Rounded.Settings)
        )

        Scaffold(
            bottomBar = {
                val showBottomBar = items.any { item ->
                    currentDestination?.hierarchy?.any { it.hasRoute(item.route::class) } == true
                }
                if (showBottomBar) {
                    NavigationBar {
                        items.forEach { item ->
                            val selected = currentDestination?.hierarchy?.any {
                                it.hasRoute(item.route::class)
                            } == true
                            NavigationBarItem(
                                icon = { Icon(item.icon, contentDescription = item.label) },
                                label = { Text(item.label) },
                                selected = selected,
                                onClick = {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                NavHost(
                    navController = navController,
                    startDestination = Route.MainScreen
                ) {
                    createNavGraph(navigationController.navigator)
                }
            }
        }
    }
}

private data class BottomNavItem(
    val label: String,
    val route: Route,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)
