package com.brohit.truecalc.ui

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.brohit.truecalc.ui.components.UpdateAppScreen
import com.brohit.truecalc.ui.navigation.Route
import com.brohit.truecalc.ui.navigation.createNavGraph
import com.brohit.truecalc.ui.navigation.rememberNavigationController
import com.brohit.truecalc.ui.theme.TrueCalcTheme

@Composable
fun MainAppScreen(viewModel: MainViewModel) {
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

            if (viewModel.isUpdateAvailable.collectAsStateWithLifecycle().value) {
                val activity = LocalActivity.current
                val context = LocalContext.current
                UpdateAppScreen(
                    onUpdate = {
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(
                                viewModel.downloadUrl
                                    ?: "https://github.com/rollychop/TrueCalc/releases"
                            )
                        ).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        context.startActivity(intent)
                    },
                    onCloseApp = {
                        activity?.finish()
                    },
                    releaseNote = viewModel.notes ?: "Release notes"
                )
            }


        }
    }
}

