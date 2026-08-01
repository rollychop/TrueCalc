package com.brohit.truecalc.data.data_source.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.brohit.truecalc.data.data_source.local.room.entity.CalculationHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface CalculationHistoryDao {
    @Insert
    suspend fun insertHistory(history: CalculationHistory)

    @Query("SELECT * FROM calculation_history_table ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<CalculationHistory>>

    @Query("DELETE FROM calculation_history_table WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM calculation_history_table")
    suspend fun clearAll()

    @Query("DELETE FROM calculation_history_table WHERE toolRoute = :toolRoute AND id NOT IN (SELECT id FROM calculation_history_table WHERE toolRoute = :toolRoute ORDER BY timestamp DESC LIMIT :limit)")
    suspend fun pruneHistory(toolRoute: String, limit: Int)
}
