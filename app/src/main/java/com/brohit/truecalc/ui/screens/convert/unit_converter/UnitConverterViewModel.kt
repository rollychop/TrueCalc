package com.brohit.truecalc.ui.screens.convert.unit_converter

import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.brohit.truecalc.domain.HistoryManager
import com.brohit.truecalc.domain.calc.CalcFormat
import com.brohit.truecalc.domain.calc.MeasureUnit
import com.brohit.truecalc.domain.calc.UnitCategory
import com.brohit.truecalc.domain.calc.UnitConverter
import com.brohit.truecalc.ui.components.tool.BreakdownRow
import com.brohit.truecalc.ui.navigation.Route
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.json.Json
import javax.inject.Inject

@HiltViewModel
class UnitConverterViewModel @Inject constructor(
    private val historyManager: HistoryManager,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val inputState = UnitConverterInputState()

    init {
        val route: Route.Calculator.UnitConverter = savedStateHandle.toRoute()
        val restorationJson = route.restorationJson
        if (!restorationJson.isNullOrEmpty()) {
            try {
                val data = Json.decodeFromString<Map<String, String>>(restorationJson)
                inputState.restore(data["category"], data["from"], data["to"])
                data["value"]?.let { inputState.value.updateText(it) }
            } catch (e: Exception) {
                // Ignore malformed history
            }
        }
    }

    fun reset() = inputState.reset()

    private data class Inputs(
        val category: UnitCategory,
        val from: MeasureUnit,
        val to: MeasureUnit,
        val value: String,
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<UnitConverterResultState> =
        snapshotFlow { Inputs(inputState.category, inputState.from, inputState.to, inputState.value.text) }
            .mapLatest { inputs ->
                val result = calculate(inputs)
                if (result.value != null) log(inputs, result)
                result
            }
            .flowOn(Dispatchers.Default)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), UnitConverterResultState())

    private fun withSymbol(value: Double, unit: MeasureUnit): String =
        "${UnitConverter.format(value)} ${unit.symbol}"

    private fun calculate(inputs: Inputs): UnitConverterResultState {
        // The unit pair can briefly belong to the previous category while switching.
        if (inputs.from !in inputs.category.units || inputs.to !in inputs.category.units) {
            return UnitConverterResultState()
        }
        val value = CalcFormat.parse(inputs.value) ?: return UnitConverterResultState()
        val converted = UnitConverter.convert(value, inputs.from, inputs.to)
        val fromText = withSymbol(value, inputs.from)
        val toText = withSymbol(converted, inputs.to)
        return UnitConverterResultState(
            label = "$fromText =",
            value = toText,
            caption = inputs.to.name,
            allUnits = UnitConverter.convertToAll(value, inputs.from, inputs.category).map { (unit, v) ->
                BreakdownRow(unit.pickerLabel(), withSymbol(v, unit), emphasized = unit == inputs.to)
            },
            shareText = "$fromText = $toText — via TrueCalc",
        )
    }

    private fun log(inputs: Inputs, result: UnitConverterResultState) {
        historyManager.log(
            route = Route.Calculator.UnitConverter(),
            name = "Unit Converter",
            inputs = mapOf(
                "Category" to inputs.category.label,
                "Value" to "${inputs.value} ${inputs.from.symbol}",
                "To" to inputs.to.name,
            ),
            results = mapOf("Result" to result.value.orEmpty()),
            restorationInputs = mapOf(
                "category" to inputs.category.key,
                "from" to inputs.from.id,
                "to" to inputs.to.id,
                "value" to inputs.value,
            ),
        )
    }
}
