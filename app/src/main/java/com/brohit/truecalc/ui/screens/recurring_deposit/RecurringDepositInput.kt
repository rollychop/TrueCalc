package com.brohit.truecalc.ui.screens.recurring_deposit

import com.brohit.truecalc.domain.model.CompoundingFrequency

data class RecurringDepositInput(
    val interval: RecurringInterval,
    val principal: String,
    val rate: String,
    val time: String,
    val isTimeInYears: Boolean,
    val compoundingFrequency: CompoundingFrequency
)