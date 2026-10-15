package com.brohit.truecalc.data.data_source.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.brohit.truecalc.data.data_source.local.room.entity.ToolUsage
import kotlinx.coroutines.flow.Flow

@Dao
interface ToolUsageDao {
    @Insert
    suspend fun insertUsage(usage: ToolUsage)

    @Query("SELECT route FROM tool_usage_table GROUP BY route ORDER BY COUNT(route) DESC LIMIT :limit")
    fun getMostUsed(limit: Int): Flow<List<String>>

    /** Distinct routes, most recently used first. */
    @Query("SELECT route FROM tool_usage_table GROUP BY route ORDER BY MAX(timestamp) DESC LIMIT :limit")
    fun getRecent(limit: Int): Flow<List<String>>

    @Query("DELETE FROM tool_usage_table")
    suspend fun clearHistory()
}
