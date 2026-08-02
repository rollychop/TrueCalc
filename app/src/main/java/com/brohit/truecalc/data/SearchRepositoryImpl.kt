package com.brohit.truecalc.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.brohit.truecalc.data.data_source.local.room.dao.CalculationHistoryDao
import com.brohit.truecalc.data.data_source.local.room.dao.ToolUsageDao
import com.brohit.truecalc.data.data_source.local.room.entity.CalculationHistory
import com.brohit.truecalc.domain.SearchRepository
import com.brohit.truecalc.domain.model.SearchRoute
import com.brohit.truecalc.ui.navigation.Route
import com.brohit.truecalc.ui.screens.home.calculatorsWithCategories
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import javax.inject.Inject

class SearchRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val calculationHistoryDao: CalculationHistoryDao,
    private val toolUsageDao: ToolUsageDao
) : SearchRepository {

    private object PreferencesKeys {
        val RECENT_QUERIES = stringPreferencesKey("recent_search_queries")
    }

    private val routes = calculatorsWithCategories.flatMap { item ->
        item.calculators.map {
            SearchRoute(
                route = it.route,
                category = item.category,
                label = it.name,
                description = it.description,
                icon = it.icon
            )
        }
    }


    override fun searchRoutes(query: String): Flow<List<SearchRoute>> = flow {
        val trimmedQuery = query.trim().lowercase()
        if (trimmedQuery.isEmpty()) {
            emit(routes)
        } else {
            emit(
                routes.filter {
                    it.label.lowercase().contains(trimmedQuery) ||
                            it.category.lowercase().contains(trimmedQuery)
                }
            )
        }
    }

    override fun getRecentQueries(): Flow<List<String>> {
        return dataStore.data.map { preferences ->
            val queriesString = preferences[PreferencesKeys.RECENT_QUERIES] ?: ""
            if (queriesString.isEmpty()) emptyList()
            else queriesString.split("|").filter { it.isNotBlank() }
        }
    }

    override suspend fun addRecentQuery(query: String) {
        val trimmedQuery = query.trim()
        if (trimmedQuery.isEmpty()) return

        dataStore.edit { preferences ->
            val currentQueries = (preferences[PreferencesKeys.RECENT_QUERIES] ?: "")
                .split("|")
                .filter { it.isNotBlank() && it != trimmedQuery }
                .toMutableList()

            currentQueries.add(0, trimmedQuery)
            val updatedQueries = currentQueries.take(10).joinToString("|")
            preferences[PreferencesKeys.RECENT_QUERIES] = updatedQueries
        }
    }

    override suspend fun removeRecentQuery(query: String) {
        dataStore.edit { preferences ->
            val updatedQueries = (preferences[PreferencesKeys.RECENT_QUERIES] ?: "")
                .split("|")
                .filter { it.isNotBlank() && it != query }
                .joinToString("|")
            preferences[PreferencesKeys.RECENT_QUERIES] = updatedQueries
        }
    }

    override suspend fun clearRecentQueries() {
        dataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.RECENT_QUERIES)
        }
    }

    override fun searchCalculationHistory(query: String): Flow<List<CalculationHistory>> {
        return calculationHistoryDao.getAllHistory().map { history ->
            val trimmedQuery = query.trim().lowercase()
            if (trimmedQuery.isEmpty()) emptyList()
            else history.filter {
                it.toolName.lowercase().contains(trimmedQuery) ||
                        it.inputsJson.lowercase().contains(trimmedQuery) ||
                        it.resultsJson.lowercase().contains(trimmedQuery)
            }
        }
    }

    override fun getMostUsedRoutes(limit: Int): Flow<List<SearchRoute>> {
        return toolUsageDao.getMostUsed(limit).map { usedRoutes ->
            usedRoutes.mapNotNull { routeString ->
                routes.find { it.route.toString() == routeString }
            }.ifEmpty {
                routes.take(limit)
            }
        }
    }
}
