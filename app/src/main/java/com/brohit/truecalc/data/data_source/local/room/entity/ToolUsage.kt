package com.brohit.truecalc.data.data_source.local.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tool_usage_table")
data class ToolUsage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val route: String,
    val timestamp: Long = System.currentTimeMillis()
)
