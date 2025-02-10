package com.brohit.truecalc.data.data_source.local.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.brohit.truecalc.data.data_source.local.room.dao.FixedChargesDao
import com.brohit.truecalc.data.data_source.local.room.entity.FixedCharge

@Database(
    entities = [
        FixedCharge::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun fixedCharges(): FixedChargesDao
}