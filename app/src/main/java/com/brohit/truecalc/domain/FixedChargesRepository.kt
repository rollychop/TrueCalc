package com.brohit.truecalc.domain

import com.brohit.truecalc.data.data_source.local.room.dao.FixedChargesDao
import com.brohit.truecalc.data.data_source.local.room.entity.FixedCharge
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FixedChargesRepository @Inject constructor(private val fixedChargesDao: FixedChargesDao) {

    val allFixedCharges: Flow<List<FixedCharge>> = fixedChargesDao.getAllFixedCharges()

    suspend fun insert(fixedCharges: FixedCharge) {
        fixedChargesDao.upsert(fixedCharges)
    }

    suspend fun delete(fixedCharge: FixedCharge) {
        fixedChargesDao.delete(fixedCharge.id)
    }
}