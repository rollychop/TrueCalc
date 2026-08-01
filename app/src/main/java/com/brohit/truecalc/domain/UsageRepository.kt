package com.brohit.truecalc.domain

import com.brohit.truecalc.data.data_source.local.room.dao.ToolUsageDao
import com.brohit.truecalc.data.data_source.local.room.entity.ToolUsage
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UsageRepository @Inject constructor(
    private val toolUsageDao: ToolUsageDao
) {
    suspend fun logUsage(route: String) {
        toolUsageDao.insertUsage(ToolUsage(route = route))
    }

    fun getMostUsed(limit: Int): Flow<List<String>> = toolUsageDao.getMostUsed(limit)

    fun getRecent(limit: Int): Flow<List<String>> = toolUsageDao.getRecent(limit)

    suspend fun clearHistory() {
        toolUsageDao.clearHistory()
    }
}
