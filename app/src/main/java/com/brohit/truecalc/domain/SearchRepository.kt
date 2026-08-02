package com.brohit.truecalc.domain

import com.brohit.truecalc.data.data_source.local.room.entity.CalculationHistory
import com.brohit.truecalc.domain.model.SearchRoute
import kotlinx.coroutines.flow.Flow

interface SearchRepository {
    fun searchRoutes(query: String): Flow<List<SearchRoute>>
    
    fun getRecentQueries(): Flow<List<String>>
    suspend fun addRecentQuery(query: String)
    suspend fun removeRecentQuery(query: String)
    suspend fun clearRecentQueries()
    
    fun searchCalculationHistory(query: String): Flow<List<CalculationHistory>>
    
    fun getMostUsedRoutes(limit: Int): Flow<List<SearchRoute>>
}
