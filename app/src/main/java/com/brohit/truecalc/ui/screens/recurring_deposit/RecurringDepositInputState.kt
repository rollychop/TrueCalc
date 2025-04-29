package com.brohit.truecalc.ui.screens.recurring_deposit

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.brohit.truecalc.domain.model.CompoundingFrequency
import com.brohit.truecalc.domain.model.TextFieldState

class RecurringDepositInputState {

    val principal = TextFieldState(
        validator = {
            it.isNotEmpty() && it.toDoubleOrNull() != null && it.toDouble() > 0
        },
        maxChar = 15
    )
    var recurringInterval by mutableStateOf(RecurringInterval.MONTHLY)

    val interestRate = TextFieldState(
        validator = {
            val rate = it.toDoubleOrNull()
            rate != null && rate in 0.01..100.0
        },
        maxChar = 6
    )

    var isTimeInYears by mutableStateOf(true)

    val time = TextFieldState(
        validator = {
            val value = it.toDoubleOrNull()
            value != null && value > 0
        },
        maxChar = 3
    )

    var compoundingFrequency by mutableStateOf(CompoundingFrequency.Quarterly)
}