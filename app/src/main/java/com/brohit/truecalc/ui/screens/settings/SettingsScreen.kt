package com.brohit.truecalc.ui.screens.settings

import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brohit.truecalc.BuildConfig
import com.brohit.truecalc.domain.ThemeMode
import com.brohit.truecalc.ui.components.SettingsClickableItem
import com.brohit.truecalc.ui.components.SettingsInfoItem
import com.brohit.truecalc.ui.components.SettingsSectionHeader
import com.brohit.truecalc.ui.components.SettingsSwitchItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val isDynamicColorEnabled by viewModel.isDynamicColorEnabled.collectAsStateWithLifecycle()
    
    var showThemeDialog by remember { mutableStateOf(false) }

    Scaffold(
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
                onClick = { viewModel.clearFavorites() }
            )

            SettingsClickableItem(
                title = "Clear History",
                subtitle = "Reset usage analytics & suggestions",
                icon = Icons.Rounded.History,
                onClick = { viewModel.clearHistory() }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            SettingsSectionHeader(title = "About")

            SettingsInfoItem(
                title = "Version",
                icon = Icons.Rounded.Info,
                value = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
            )

            SettingsClickableItem(
                title = "Developer",
                subtitle = "Built with ❤️ by Rohit",
                icon = Icons.Rounded.Code,
                onClick = { /* Could open GitHub profile */ }
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
}
