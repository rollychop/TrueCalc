package com.brohit.truecalc.ui.screens.compund_interest

import com.brohit.truecalc.domain.model.CompoundingFrequency

data class CompoundInterestInput(
    val type: String,
    val principal: String,
    val rate: String,
    val time: String,
    val isTimeInYears: Boolean,
    val compoundingFrequency: CompoundingFrequency
)