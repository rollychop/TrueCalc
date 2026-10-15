package com.brohit.truecalc.ui.screens.everyday.percentage

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.brohit.truecalc.domain.calc.ChangeDirection
import com.brohit.truecalc.domain.calc.PercentageMode
import com.brohit.truecalc.domain.model.TextFieldState
import com.brohit.truecalc.ui.components.tool.BreakdownRow

class PercentageInputState {
    var mode by mutableStateOf(PercentageMode.PercentOf)

    /** X: the percentage (X% of Y), the part (X of Y), the start value (change) or the value (apply). */
    val first = TextFieldState()

    /** Y: the value, the whole, the end value, or the percentage to apply. */
    val second = TextFieldState()
    var direction by mutableStateOf(ChangeDirection.Increase)

    fun reset() {
        mode = PercentageMode.PercentOf
        first.reset()
        second.reset()
        direction = ChangeDirection.Increase
    }
}

/** Field labels and suffixes for a mode. */
data class PercentageFieldLabels(
    val first: String,
    val firstSuffix: String?,
    val second: String,
    val secondSuffix: String?,
)

fun PercentageMode.fieldLabels(): PercentageFieldLabels = when (this) {
    PercentageMode.PercentOf -> PercentageFieldLabels("Percentage", "%", "Of value", null)
    PercentageMode.WhatPercent -> PercentageFieldLabels("Value", null, "Total", null)
    PercentageMode.Change -> PercentageFieldLabels("From", null, "To", null)
    PercentageMode.ApplyChange -> PercentageFieldLabels("Value", null, "Percentage", "%")
}

data class PercentageResultState(
    val label: String = "Result",
    /** Formatted answer; null when there is no result. */
    val value: String? = null,
    val caption: String? = null,
    val rows: List<BreakdownRow> = emptyList(),
    /** Shown in the hero when the inputs are complete but the answer is undefined. */
    val emptyText: String = "Enter both values to see the result",
    val shareText: String? = null,
)
