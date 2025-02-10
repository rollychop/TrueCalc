package com.brohit.truecalc.data.data_source.local.room.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.brohit.truecalc.data.data_source.local.room.entity.FixedCharge
import kotlinx.coroutines.flow.Flow

@Dao
interface FixedChargesDao {
    @Upsert
    suspend fun upsert(fixedCharges: FixedCharge)

    @Query("SELECT * FROM fixed_charges_table")
    fun getAllFixedCharges(): Flow<List<FixedCharge>>

    @Query("DELETE FROM fixed_charges_table WHERE id = :id")
    suspend fun delete(id: Int)

}