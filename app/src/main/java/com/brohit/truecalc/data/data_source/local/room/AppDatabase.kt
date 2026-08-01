package com.brohit.truecalc.data.data_source.local.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.brohit.truecalc.data.data_source.local.room.dao.FavoriteToolDao
import com.brohit.truecalc.data.data_source.local.room.dao.FixedChargesDao
import com.brohit.truecalc.data.data_source.local.room.dao.ToolUsageDao
import com.brohit.truecalc.data.data_source.local.room.entity.FavoriteTool
import com.brohit.truecalc.data.data_source.local.room.entity.FixedCharge
import com.brohit.truecalc.data.data_source.local.room.entity.ToolUsage

@Database(
    entities = [
        FixedCharge::class,
        FavoriteTool::class,
        ToolUsage::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun fixedCharges(): FixedChargesDao
    abstract fun favoriteTools(): FavoriteToolDao
    abstract fun toolUsage(): ToolUsageDao
}
