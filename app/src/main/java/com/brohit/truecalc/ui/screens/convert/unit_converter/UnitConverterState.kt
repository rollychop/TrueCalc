package com.brohit.truecalc.ui.screens.convert.unit_converter

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.brohit.truecalc.domain.calc.MeasureUnit
import com.brohit.truecalc.domain.calc.UnitCategory
import com.brohit.truecalc.domain.model.TextFieldState
import com.brohit.truecalc.ui.components.tool.BreakdownRow

private const val DEFAULT_VALUE = "1"

class UnitConverterInputState {
    var category by mutableStateOf(UnitCategory.Length)
        private set
    var from by mutableStateOf(UnitCategory.Length.defaultFrom)
    var to by mutableStateOf(UnitCategory.Length.defaultTo)
    val value = TextFieldState().apply { updateText(DEFAULT_VALUE) }

    /** Switches category and picks its default unit pair. */
    fun selectCategory(newCategory: UnitCategory) {
        if (newCategory == category) return
        category = newCategory
        from = newCategory.defaultFrom
        to = newCategory.defaultTo
    }

    /** Restores a saved selection; unknown unit ids fall back to the category defaults. */
    fun restore(categoryKey: String?, fromId: String?, toId: String?) {
        val c = UnitCategory.fromKey(categoryKey)
        category = c
        from = c.unit(fromId) ?: c.defaultFrom
        to = c.unit(toId) ?: c.defaultTo
    }

    fun swap() {
        val oldFrom = from
        from = to
        to = oldFrom
    }

    fun reset() {
        category = UnitCategory.Length
        from = UnitCategory.Length.defaultFrom
        to = UnitCategory.Length.defaultTo
        value.reset()
        value.updateText(DEFAULT_VALUE)
    }
}

/** "Kilometre (km)" for pickers. */
fun MeasureUnit.pickerLabel(): String = if (name.contains(symbol)) name else "$name ($symbol)"

data class UnitConverterResultState(
    /** "1 km =" */
    val label: String = "Result",
    /** Converted value with symbol; null when there is no result. */
    val value: String? = null,
    val caption: String? = null,
    /** The value in every unit of the category. */
    val allUnits: List<BreakdownRow> = emptyList(),
    val shareText: String? = null,
)
