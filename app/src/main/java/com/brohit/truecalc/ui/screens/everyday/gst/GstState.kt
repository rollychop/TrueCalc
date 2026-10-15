package com.brohit.truecalc.ui.screens.everyday.gst

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.brohit.truecalc.domain.calc.CalcFormat
import com.brohit.truecalc.domain.calc.GstCalc
import com.brohit.truecalc.domain.calc.GstMode
import com.brohit.truecalc.domain.model.TextFieldState
import com.brohit.truecalc.ui.components.tool.BreakdownRow

/** Preset GST rates as chip values ("0.25", "3", … "28"). */
val GstRatePresets: List<String> = GstCalc.PRESET_RATES.map { CalcFormat.number(it, grouping = false) }

private val DefaultRateText: String = CalcFormat.number(GstCalc.DEFAULT_RATE, grouping = false)

class GstInputState {
    var mode by mutableStateOf(GstMode.Add)
    val amount = TextFieldState()

    /** Rate in percent as text; a preset chip or a custom value. */
    val rate = TextFieldState().apply { updateText(DefaultRateText) }

    fun reset() {
        mode = GstMode.Add
        amount.reset()
        rate.reset()
        rate.updateText(DefaultRateText)
    }
}

data class GstResultState(
    val heroLabel: String = "Total incl. GST",
    /** Formatted headline amount; null when there is no result. */
    val heroValue: String? = null,
    val caption: String? = null,
    val rows: List<BreakdownRow> = emptyList(),
    val emptyText: String = "Enter an amount to see the GST",
    val shareText: String? = null,
)
