package com.brohit.truecalc.ui.screens.recurring_deposit

import com.brohit.truecalc.domain.calc.CompoundingFrequency
import com.brohit.truecalc.domain.calc.RecurringInterval

data class RecurringDepositInput(
    val interval: RecurringInterval,
    val principal: String,
    val rate: String,
    val time: String,
    val isTimeInYears: Boolean,
    val compoundingFrequency: CompoundingFrequency
)