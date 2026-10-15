package com.brohit.truecalc.ui.screens.everyday.percentage

import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.brohit.truecalc.domain.HistoryManager
import com.brohit.truecalc.domain.calc.CalcFormat
import com.brohit.truecalc.domain.calc.ChangeDirection
import com.brohit.truecalc.domain.calc.PercentageCalc
import com.brohit.truecalc.domain.calc.PercentageMode
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
class PercentageViewModel @Inject constructor(
    private val historyManager: HistoryManager,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val inputState = PercentageInputState()

    init {
        val route: Route.Calculator.Percentage = savedStateHandle.toRoute()
        val restorationJson = route.restorationJson
        if (!restorationJson.isNullOrEmpty()) {
            try {
                val data = Json.decodeFromString<Map<String, String>>(restorationJson)
                inputState.mode = PercentageMode.fromKey(data["mode"])
                inputState.first.updateText(data["first"] ?: "")
                inputState.second.updateText(data["second"] ?: "")
                inputState.direction = ChangeDirection.fromKey(data["direction"])
            } catch (e: Exception) {
                // Ignore malformed history
            }
        }
    }

    fun reset() = inputState.reset()

    private data class Inputs(
        val mode: PercentageMode,
        val first: String,
        val second: String,
        val direction: ChangeDirection,
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<PercentageResultState> =
        snapshotFlow {
            Inputs(inputState.mode, inputState.first.text, inputState.second.text, inputState.direction)
        }
            .mapLatest { inputs ->
                val result = calculate(inputs)
                if (result.value != null) log(inputs, result)
                result
            }
            .flowOn(Dispatchers.Default)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), PercentageResultState())

    private fun fmt(value: Double): String = CalcFormat.number(value, maxDecimals = 4)

    private fun calculate(inputs: Inputs): PercentageResultState {
        val x = CalcFormat.parse(inputs.first) ?: return PercentageResultState()
        val y = CalcFormat.parse(inputs.second) ?: return PercentageResultState()
        return when (inputs.mode) {
            PercentageMode.PercentOf -> {
                val r = PercentageCalc.percentOf(x, y)
                PercentageResultState(
                    label = "${fmt(x)}% of ${fmt(y)}",
                    value = fmt(r),
                    rows = listOf(
                        BreakdownRow("Remaining (${fmt(100 - x)}%)", fmt(y - r)),
                        BreakdownRow("${fmt(y)} + ${fmt(x)}%", fmt(y + r)),
                    ),
                    shareText = "${fmt(x)}% of ${fmt(y)} is ${fmt(r)} — via TrueCalc",
                )
            }
            PercentageMode.WhatPercent -> {
                val r = PercentageCalc.whatPercent(x, y)
                    ?: return PercentageResultState(emptyText = "The total can't be 0")
                PercentageResultState(
                    label = "${fmt(x)} out of ${fmt(y)}",
                    value = "${fmt(r)}%",
                    rows = listOf(BreakdownRow("Remaining", "${fmt(y - x)} (${fmt(100 - r)}%)")),
                    shareText = "${fmt(x)} is ${fmt(r)}% of ${fmt(y)} — via TrueCalc",
                )
            }
            PercentageMode.Change -> {
                val r = PercentageCalc.percentChange(x, y)
                    ?: return PercentageResultState(emptyText = "The starting value can't be 0")
                val word = when {
                    r > 0 -> "increase"
                    r < 0 -> "decrease"
                    else -> "no change"
                }
                val sign = if (r > 0) "+" else ""
                val diff = y - x
                val diffSign = if (diff > 0) "+" else ""
                PercentageResultState(
                    label = "Change from ${fmt(x)} to ${fmt(y)}",
                    value = "$sign${fmt(r)}%",
                    caption = word.replaceFirstChar { it.uppercase() },
                    rows = listOf(
                        BreakdownRow("Difference", "$diffSign${fmt(diff)}"),
                        BreakdownRow("New value as % of old", "${fmt(y / x * 100)}%"),
                    ),
                    shareText = "From ${fmt(x)} to ${fmt(y)} is a $sign${fmt(r)}% $word — via TrueCalc",
                )
            }
            PercentageMode.ApplyChange -> {
                val r = PercentageCalc.applyChange(x, y, inputs.direction)
                val change = r - x
                val verb = if (inputs.direction == ChangeDirection.Increase) "plus" else "minus"
                PercentageResultState(
                    label = "${fmt(x)} $verb ${fmt(y)}%",
                    value = fmt(r),
                    rows = listOf(
                        BreakdownRow("Change", (if (change > 0) "+" else "") + fmt(change)),
                    ),
                    shareText = "${fmt(x)} $verb ${fmt(y)}% is ${fmt(r)} — via TrueCalc",
                )
            }
        }
    }

    private fun log(inputs: Inputs, result: PercentageResultState) {
        val labels = inputs.mode.fieldLabels()
        historyManager.log(
            route = Route.Calculator.Percentage(),
            name = "Percentage",
            inputs = buildMap {
                put("Mode", inputs.mode.label)
                put(labels.first, inputs.first + (labels.firstSuffix ?: ""))
                put(labels.second, inputs.second + (labels.secondSuffix ?: ""))
                if (inputs.mode == PercentageMode.ApplyChange) put("Direction", inputs.direction.label)
            },
            results = mapOf(result.label to result.value.orEmpty()),
            restorationInputs = mapOf(
                "mode" to inputs.mode.key,
                "first" to inputs.first,
                "second" to inputs.second,
                "direction" to inputs.direction.key,
            ),
        )
    }
}
