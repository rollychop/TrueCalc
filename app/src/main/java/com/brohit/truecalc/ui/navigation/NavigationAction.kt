package com.brohit.truecalc.ui.navigation

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.withResumed
import androidx.navigation.NavHostController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

interface AppNavigator {
    fun navigateUp(): Boolean
    fun navigate(route: Route)
}

class FakeAppNavigator : AppNavigator {
    override fun navigateUp(): Boolean {
        return true
    }

    override fun navigate(route: Route) {
        return
    }
}


@Composable
fun rememberNavigationController(
    controller: NavHostController,
    coroutineScope: CoroutineScope = rememberCoroutineScope()
): NavigationController = remember(coroutineScope, controller) {
    NavigationController(coroutineScope, controller)
}


class NavigationController(
    private val coroutineScope: CoroutineScope,
    private val controller: NavHostController,
) {
    val navigator = object : AppNavigator {
        override fun navigateUp(): Boolean {
            return controller.navigateUp()
        }

        override fun navigate(route: Route) {
            coroutineScope.launch {
                controller.currentBackStackEntry?.withResumed {
                    controller.navigate(route)
                } ?: run {
                    Toast.makeText(
                        controller.context,
                        "Failed to navigate",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

    }
}