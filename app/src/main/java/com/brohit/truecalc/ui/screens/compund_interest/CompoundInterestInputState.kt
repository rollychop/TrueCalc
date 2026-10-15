package com.brohit.truecalc.ui.screens.compund_interest

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.brohit.truecalc.domain.calc.CompoundingFrequency
import com.brohit.truecalc.domain.model.TextFieldState

class CompoundInterestInputState(type: String = CalculationType.COMPOUND) {

    var calculationType by mutableStateOf(type)
        private set

    fun onCalculationTypeChange(type: String) {
        if (type in CalculationType.entries)
            calculationType = type
    }


    val principal = TextFieldState(
        validator = {
            it.isNotEmpty() && it.toDoubleOrNull() != null && it.toDouble() > 0
        },
        maxChar = 15
    )

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

    var compoundingFrequency by mutableStateOf(defaultFrequencyFor(type))

    /** Clears the inputs back to defaults; keeps the selected calculation type. */
    fun reset() {
        principal.reset()
        interestRate.reset()
        time.reset()
        isTimeInYears = true
        compoundingFrequency = defaultFrequencyFor(calculationType)
    }

    companion object {
        /** Indian bank FDs pay/compound quarterly; compound interest defaults to yearly. */
        fun defaultFrequencyFor(type: String): CompoundingFrequency =
            if (type == CalculationType.FIXED) CompoundingFrequency.Quarterly
            else CompoundingFrequency.Annually
    }
}