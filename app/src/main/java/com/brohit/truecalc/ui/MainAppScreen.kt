package com.brohit.truecalc.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.brohit.truecalc.ui.navigation.Route
import com.brohit.truecalc.ui.navigation.createNavGraph
import com.brohit.truecalc.ui.navigation.rememberNavigationController
import com.brohit.truecalc.ui.theme.TrueCalcTheme

@Composable
fun MainAppScreen() {
    TrueCalcTheme(darkTheme = false) {
        Surface {
            val navController = rememberNavController()
            val navigationController = rememberNavigationController(navController)
            NavHost(
                navController = navController,
                startDestination = Route.MainScreen,
                modifier = Modifier.fillMaxSize()
            ) {
                createNavGraph(navigationController.navigator)
            }

        }
    }
}

