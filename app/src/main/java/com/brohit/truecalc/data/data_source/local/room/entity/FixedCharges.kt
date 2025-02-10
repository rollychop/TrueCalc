package com.brohit.truecalc.data.data_source.local.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fixed_charges_table")
data class FixedCharge(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val amount: Double,
    val isPercentage: Boolean
)