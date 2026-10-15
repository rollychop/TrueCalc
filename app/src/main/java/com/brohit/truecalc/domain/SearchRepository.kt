package com.brohit.truecalc.domain

import com.brohit.truecalc.data.data_source.local.room.entity.CalculationHistory
import kotlinx.coroutines.flow.Flow

/**
 * Recent search queries and search over past calculations. Tool search itself is
 * `ToolRegistry.search`.
 */
interface SearchRepository {
    fun getRecentQueries(): Flow<List<String>>
    suspend fun addRecentQuery(query: String)
    suspend fun removeRecentQuery(query: String)
    suspend fun clearRecentQueries()

    fun searchCalculationHistory(query: String): Flow<List<CalculationHistory>>
}
