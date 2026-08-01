package com.brohit.truecalc.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brohit.truecalc.domain.FavoritesRepository
import com.brohit.truecalc.domain.HistoryRepository
import com.brohit.truecalc.domain.SettingsRepository
import com.brohit.truecalc.domain.ThemeMode
import com.brohit.truecalc.domain.UsageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val favoritesRepository: FavoritesRepository,
    private val usageRepository: UsageRepository,
    private val historyRepository: HistoryRepository
) : ViewModel() {

    val themeMode: StateFlow<ThemeMode> = settingsRepository.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ThemeMode.SYSTEM)

    val isDynamicColorEnabled: StateFlow<Boolean> = settingsRepository.isDynamicColorEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            settingsRepository.setThemeMode(mode)
        }
    }

    fun setDynamicColorEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setDynamicColorEnabled(enabled)
        }
    }

    fun clearFavorites() {
        viewModelScope.launch {
            favoritesRepository.clearAllFavorites()
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            usageRepository.clearHistory()
            historyRepository.clearAllHistory()
        }
    }
}
