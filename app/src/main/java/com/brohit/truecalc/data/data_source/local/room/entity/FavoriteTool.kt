package com.brohit.truecalc.data.data_source.local.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_tools_table")
data class FavoriteTool(
    @PrimaryKey val route: String, // Serialized Route object
    val name: String,
    val description: String,
    val category: String,
    val timestamp: Long = System.currentTimeMillis()
)
