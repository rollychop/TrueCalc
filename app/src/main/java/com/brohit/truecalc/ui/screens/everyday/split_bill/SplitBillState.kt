package com.brohit.truecalc.ui.screens.everyday.split_bill

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.brohit.truecalc.domain.calc.CalcFormat
import com.brohit.truecalc.domain.calc.SplitBillCalc
import com.brohit.truecalc.domain.model.TextFieldState
import com.brohit.truecalc.ui.components.tool.BreakdownRow

/** Tip chips as text ("0", "5", "10", "15"). */
val TipPresets: List<String> = SplitBillCalc.PRESET_TIPS.map { CalcFormat.number(it, grouping = false) }

private const val DEFAULT_PEOPLE = "2"
private const val DEFAULT_TIP = "0"

class SplitBillInputState {
    val bill = TextFieldState()
    val people = TextFieldState(
        validator = { text -> text.isEmpty() || (text.toIntOrNull() ?: 0) >= 1 },
        errorFor = { "At least 1 person" },
    ).apply { updateText(DEFAULT_PEOPLE) }
    val tip = TextFieldState().apply { updateText(DEFAULT_TIP) }
    var roundUp by mutableStateOf(false)

    fun reset() {
        bill.reset()
        people.reset()
        people.updateText(DEFAULT_PEOPLE)
        tip.reset()
        tip.updateText(DEFAULT_TIP)
        roundUp = false
    }
}

data class SplitBillResultState(
    /** Formatted share per person; null when there is no result. */
    val perPerson: String? = null,
    val caption: String? = null,
    val rows: List<BreakdownRow> = emptyList(),
    val emptyText: String = "Enter the bill amount to split it",
    val shareText: String? = null,
)
