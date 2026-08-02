package com.brohit.truecalc.ui.screens.settings

import android.os.Build
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
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brohit.truecalc.BuildConfig
import com.brohit.truecalc.domain.ThemeMode
import com.brohit.truecalc.ui.components.ConfirmationDialog
import com.brohit.truecalc.ui.components.SettingsClickableItem
import com.brohit.truecalc.ui.components.SettingsInfoItem
import com.brohit.truecalc.ui.components.SettingsSectionHeader
import com.brohit.truecalc.ui.components.SettingsSwitchItem
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
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Settings",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            SettingsSectionHeader(title = "Appearance")
            
            SettingsClickableItem(
                title = "Theme",
                subtitle = themeMode.name.lowercase().replaceFirstChar { it.uppercase() },
                icon = Icons.Rounded.Palette,
                onClick = { showThemeDialog = true }
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                SettingsSwitchItem(
                    title = "Dynamic Color",
                    subtitle = "Tonal colors based on wallpaper",
                    icon = Icons.Rounded.ColorLens,
                    checked = isDynamicColorEnabled,
                    onCheckedChange = { viewModel.setDynamicColorEnabled(it) }
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            SettingsSectionHeader(title = "Data Management")

            SettingsClickableItem(
                title = "Clear Favorites",
                subtitle = "Remove all bookmarked tools",
                icon = Icons.Rounded.DeleteSweep,
                onClick = { showClearFavoritesDialog = true }
            )

            SettingsClickableItem(
                title = "Clear History",
                subtitle = "Reset usage analytics & suggestions",
                icon = Icons.Rounded.History,
                onClick = { showClearHistoryDialog = true }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            SettingsSectionHeader(title = "About")

            SettingsInfoItem(
                title = "Version",
                icon = Icons.Rounded.Info,
                value = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
            )

            SettingsClickableItem(
                title = "Source Code",
                subtitle = "View project on GitHub",
                icon = Icons.Rounded.Code,
                onClick = { context.openUrl("https://github.com/rollychop/TrueCalc") }
            )

            SettingsClickableItem(
                title = "Developer",
                subtitle = "Built with ❤️ by Rohit",
                icon = Icons.Rounded.Code,
                onClick = { context.openUrl("https://github.com/rollychop") }
            )
        }
    }

    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("Select Theme") },
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
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = themeMode == mode,
                                onClick = null
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(mode.name.lowercase().replaceFirstChar { it.uppercase() })
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
        title = "Clear Favorites?",
        text = "This will remove all your bookmarked tools. This action cannot be undone.",
        confirmText = "Clear"
    )

    ConfirmationDialog(
        show = showClearHistoryDialog,
        onDismissRequest = { showClearHistoryDialog = false },
        onConfirm = {
            viewModel.clearHistory()
            showClearHistoryDialog = false
        },
        title = "Clear History?",
        text = "This will reset all usage analytics and history. This action cannot be undone.",
        confirmText = "Clear"
    )
}
