package com.brohit.truecalc.ui.screens.settings

import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.ColorLens
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brohit.truecalc.BuildConfig
import com.brohit.truecalc.domain.ThemeMode
import com.brohit.truecalc.ui.components.ConfirmationDialog
import com.brohit.truecalc.ui.components.SettingsClickableItem
import com.brohit.truecalc.ui.components.SettingsGroup
import com.brohit.truecalc.ui.components.SettingsInfoItem
import com.brohit.truecalc.ui.components.SettingsSwitchItem
import com.brohit.truecalc.ui.theme.Spacing
import com.brohit.truecalc.ui.utils.openUrl
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val isDynamicColorEnabled by viewModel.isDynamicColorEnabled.collectAsStateWithLifecycle()

    var showThemeDialog by remember { mutableStateOf(false) }
    var showClearFavoritesDialog by remember { mutableStateOf(false) }
    var showClearHistoryDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel.events) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is SettingsEvent.DataCleared -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(title = { Text("Settings") })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = Spacing.l, end = Spacing.l, bottom = Spacing.xl),
            verticalArrangement = Arrangement.spacedBy(Spacing.l)
        ) {
            SettingsGroup(title = "Appearance") {
                SettingsClickableItem(
                    title = "Theme",
                    subtitle = themeMode.label(),
                    icon = Icons.Rounded.Palette,
                    onClick = { showThemeDialog = true }
                )

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    SettingsSwitchItem(
                        title = "Dynamic color",
                        subtitle = "Tonal colors based on wallpaper",
                        icon = Icons.Rounded.ColorLens,
                        checked = isDynamicColorEnabled,
                        onCheckedChange = { viewModel.setDynamicColorEnabled(it) }
                    )
                }
            }

            SettingsGroup(title = "Data") {
                SettingsClickableItem(
                    title = "Clear pinned tools",
                    subtitle = "Unpin every tool on Home",
                    icon = Icons.Rounded.StarBorder,
                    onClick = { showClearFavoritesDialog = true }
                )

                SettingsClickableItem(
                    title = "Clear history",
                    subtitle = "Delete past calculations and recent tools",
                    icon = Icons.Rounded.DeleteSweep,
                    onClick = { showClearHistoryDialog = true }
                )
            }

            SettingsGroup(title = "About") {
                SettingsInfoItem(
                    title = "Version",
                    icon = Icons.Rounded.Info,
                    value = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
                )

                SettingsClickableItem(
                    title = "Source code",
                    subtitle = "View project on GitHub",
                    icon = Icons.Rounded.Code,
                    onClick = { context.openUrl("https://github.com/rollychop/TrueCalc") }
                )

                SettingsClickableItem(
                    title = "Developer",
                    subtitle = "Built with ❤️ by Rohit",
                    icon = Icons.Rounded.Person,
                    onClick = { context.openUrl("https://github.com/rollychop") }
                )
            }
        }
    }

    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("Theme") },
            text = {
                Column {
                    ThemeMode.entries.forEach { mode ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = themeMode == mode,
                                    onClick = {
                                        viewModel.setThemeMode(mode)
                                        showThemeDialog = false
                                    }
                                )
                                .padding(vertical = Spacing.m),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = themeMode == mode,
                                onClick = null
                            )
                            Spacer(modifier = Modifier.width(Spacing.l))
                            Text(mode.label())
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    ConfirmationDialog(
        show = showClearFavoritesDialog,
        onDismissRequest = { showClearFavoritesDialog = false },
        onConfirm = {
            viewModel.clearFavorites()
            showClearFavoritesDialog = false
        },
        title = "Clear pinned tools?",
        text = "This will unpin all your tools. This action cannot be undone.",
        confirmText = "Clear"
    )

    ConfirmationDialog(
        show = showClearHistoryDialog,
        onDismissRequest = { showClearHistoryDialog = false },
        onConfirm = {
            viewModel.clearHistory()
            showClearHistoryDialog = false
        },
        title = "Clear history?",
        text = "This will delete all past calculations and your recent tools. This action cannot be undone.",
        confirmText = "Clear"
    )
}

private fun ThemeMode.label(): String = when (this) {
    ThemeMode.SYSTEM -> "System default"
    ThemeMode.LIGHT -> "Light"
    ThemeMode.DARK -> "Dark"
}
