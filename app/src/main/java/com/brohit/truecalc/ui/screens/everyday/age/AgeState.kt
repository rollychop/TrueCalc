package com.brohit.truecalc.ui.screens.everyday.age

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.brohit.truecalc.ui.components.tool.BreakdownRow
import java.time.LocalDate

class AgeInputState {
    var dateOfBirth by mutableStateOf<LocalDate?>(null)
    var asOf by mutableStateOf(LocalDate.now())

    fun reset() {
        dateOfBirth = null
        asOf = LocalDate.now()
    }
}

data class AgeResultState(
    /** "36 years"; null when there is no result. */
    val headline: String? = null,
    val caption: String? = null,
    val details: List<BreakdownRow> = emptyList(),
    val nextBirthday: List<BreakdownRow> = emptyList(),
    val emptyText: String = "Pick a date of birth to see the exact age",
    val shareText: String? = null,
)
