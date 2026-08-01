package com.brohit.truecalc.domain

import com.brohit.truecalc.data.data_source.local.room.dao.CalculationHistoryDao
import com.brohit.truecalc.data.data_source.local.room.entity.CalculationHistory
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class HistoryRepository @Inject constructor(
    private val calculationHistoryDao: CalculationHistoryDao
) {
    fun getAllHistory(): Flow<List<CalculationHistory>> = calculationHistoryDao.getAllHistory()

    suspend fun addHistory(history: CalculationHistory) {
        calculationHistoryDao.insertHistory(history)
        calculationHistoryDao.pruneHistory(history.toolRoute, 100)
    }

    suspend fun deleteHistory(id: Long) {
        calculationHistoryDao.deleteById(id)
    }

    suspend fun clearAllHistory() {
        calculationHistoryDao.clearAll()
    }
}
