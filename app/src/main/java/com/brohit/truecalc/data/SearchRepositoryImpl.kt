package com.brohit.truecalc.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.brohit.truecalc.data.data_source.local.room.dao.CalculationHistoryDao
import com.brohit.truecalc.data.data_source.local.room.entity.CalculationHistory
import com.brohit.truecalc.domain.SearchRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SearchRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val calculationHistoryDao: CalculationHistoryDao,
) : SearchRepository {

    private object PreferencesKeys {
        val RECENT_QUERIES = stringPreferencesKey("recent_search_queries")
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
}
