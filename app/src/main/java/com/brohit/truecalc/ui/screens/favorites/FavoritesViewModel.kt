package com.brohit.truecalc.ui.screens.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brohit.truecalc.data.data_source.local.room.entity.FavoriteTool
import com.brohit.truecalc.domain.FavoritesRepository
import com.brohit.truecalc.domain.UsageRepository
import com.brohit.truecalc.ui.navigation.Route
import com.brohit.truecalc.ui.tools.ToolRegistry
import com.brohit.truecalc.ui.tools.ToolSpec
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import javax.inject.Inject

/**
 * Pinned (favourite) tools and recently used tools.
 *
 * Favourites and usage are stored in Room keyed by the route JSON ([ToolSpec.routeKey]); that
 * format is unchanged, and stored keys are resolved to tools with [ToolRegistry.forRouteKey].
 */
@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val repository: FavoritesRepository,
    private val usageRepository: UsageRepository
) : ViewModel() {

    val favorites: StateFlow<List<FavoriteTool>> = repository.getAllFavorites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Pinned tools, most recently pinned first; unknown or duplicate entries are skipped. */
    val pinnedTools: StateFlow<List<ToolSpec>> = favorites
        .map { favs -> ToolRegistry.forRouteKeys(favs.map { it.route }) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Up to [RECENT_LIMIT] distinct tools, most recently opened first. */
    val recentTools: StateFlow<List<ToolSpec>> = usageRepository.getRecent(RECENT_QUERY_LIMIT)
        .map { routes -> ToolRegistry.forRouteKeys(routes, RECENT_LIMIT) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun isFavorite(route: Route): Flow<Boolean> = repository.isFavorite(Json.encodeToString(route))

    fun logUsage(tool: ToolSpec) {
        viewModelScope.launch {
            usageRepository.logUsage(tool.routeKey)
        }
    }

    /** Pins [tool], or unpins it if any stored favourite already points to it. */
    fun togglePin(tool: ToolSpec) {
        viewModelScope.launch {
            val matching = favoritesFor(tool.id)
            if (matching.isEmpty()) {
                repository.addFavorite(tool.toFavorite())
            } else {
                matching.forEach { repository.removeFavorite(it) }
            }
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

    /** Removes every stored favourite that points to the same tool as [route]. */
    fun removeFavorite(route: Route) {
        val canonical = Json.encodeToString(route)
        val toolId = ToolRegistry.forRoute(route)?.id
        viewModelScope.launch {
            val keys = buildSet {
                add(canonical)
                if (toolId != null) addAll(favoritesFor(toolId))
            }
            keys.forEach { repository.removeFavorite(it) }
        }
    }

    /** Stored favourite keys that resolve to the tool [toolId]. */
    private suspend fun favoritesFor(toolId: String): List<String> =
        repository.getAllFavorites().first()
            .map { it.route }
            .filter { ToolRegistry.forRouteKey(it)?.id == toolId }

    private fun ToolSpec.toFavorite() = FavoriteTool(
        route = routeKey,
        name = name,
        description = description,
        category = category.title,
    )

    private companion object {
        const val RECENT_LIMIT = 6

        /** Usage rows to read before de-duplicating by tool. */
        const val RECENT_QUERY_LIMIT = 20
    }
}
