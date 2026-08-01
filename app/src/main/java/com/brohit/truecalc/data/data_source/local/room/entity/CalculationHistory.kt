package com.brohit.truecalc.data.data_source.local.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "calculation_history_table")
data class CalculationHistory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val toolRoute: String,
    val toolName: String,
    val inputsJson: String,
    val resultsJson: String,
    val restorationJson: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
