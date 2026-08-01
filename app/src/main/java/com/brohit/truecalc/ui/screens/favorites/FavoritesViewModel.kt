package com.brohit.truecalc.ui.screens.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brohit.truecalc.data.data_source.local.room.entity.FavoriteTool
import com.brohit.truecalc.domain.FavoritesRepository
import com.brohit.truecalc.domain.UsageRepository
import com.brohit.truecalc.ui.navigation.Route
import com.brohit.truecalc.ui.screens.home.Calculator
import com.brohit.truecalc.ui.screens.home.toCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val repository: FavoritesRepository,
    private val usageRepository: UsageRepository
) : ViewModel() {

    val favorites: StateFlow<List<FavoriteTool>> = repository.getAllFavorites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val suggestions: StateFlow<List<Calculator>> = combine(
        usageRepository.getMostUsed(10),
        favorites
    ) { mostUsed, currentFavorites ->
        val favoriteRoutes = currentFavorites.map { it.route }.toSet()
        mostUsed
            .filter { it !in favoriteRoutes }
            .mapNotNull { routeStr ->
                try {
                    Json.decodeFromString<Route>(routeStr).toCalculator()
                } catch (e: Exception) {
                    null
                }
            }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun isFavorite(route: Route) = repository.isFavorite(Json.encodeToString(route))

    fun logUsage(route: Route) {
        viewModelScope.launch {
            usageRepository.logUsage(Json.encodeToString(route))
        }
    }

    fun toggleFavorite(name: String, description: String, category: String, route: Route) {
        val routeStr = Json.encodeToString(route)
        viewModelScope.launch {
            // We'll handle this in the UI usually with a collectAsState,
            // but for a one-shot toggle we might need to know the current state.
            // For now, let's just provide add/remove and let the UI decide.
        }
    }

    fun addFavorite(name: String, description: String, category: String, route: Route) {
        viewModelScope.launch {
            repository.addFavorite(
                FavoriteTool(
                    route = Json.encodeToString(route),
                    name = name,
                    description = description,
                    category = category
                )
            )
        }
    }

    fun removeFavorite(route: Route) {
        viewModelScope.launch {
            repository.removeFavorite(Json.encodeToString(route))
        }
    }
}
