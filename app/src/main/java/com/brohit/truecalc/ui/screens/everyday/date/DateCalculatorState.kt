package com.brohit.truecalc.ui.screens.everyday.date

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.brohit.truecalc.domain.calc.DateCalcMode
import com.brohit.truecalc.domain.calc.DateUnit
import com.brohit.truecalc.domain.model.TextFieldState
import com.brohit.truecalc.ui.components.tool.BreakdownRow
import java.time.LocalDate

/** Largest amount accepted for add/subtract (keeps dates in a sensible range). */
const val MAX_DATE_AMOUNT_DIGITS = 6

class DateCalculatorInputState {
    var mode by mutableStateOf(DateCalcMode.Between)
    var start by mutableStateOf(LocalDate.now())
    var end by mutableStateOf(LocalDate.now().plusDays(30))
    var includeEndDate by mutableStateOf(false)

    val amount = TextFieldState(maxChar = MAX_DATE_AMOUNT_DIGITS)
    var unit by mutableStateOf(DateUnit.Days)
    var subtract by mutableStateOf(false)

    fun reset() {
        mode = DateCalcMode.Between
        start = LocalDate.now()
        end = LocalDate.now().plusDays(30)
        includeEndDate = false
        amount.reset()
        unit = DateUnit.Days
        subtract = false
    }
}

data class DateCalculatorResultState(
    val heroLabel: String = "Duration",
    /** Headline; null when there is no result. */
    val heroValue: String? = null,
    val caption: String? = null,
    val rows: List<BreakdownRow> = emptyList(),
    val emptyText: String = "Pick the dates to see the difference",
    val shareText: String? = null,
)
