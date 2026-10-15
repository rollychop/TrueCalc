package com.brohit.truecalc.ui.screens.everyday.date

import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.brohit.truecalc.domain.HistoryManager
import com.brohit.truecalc.domain.calc.CalcFormat
import com.brohit.truecalc.domain.calc.DateCalc
import com.brohit.truecalc.domain.calc.DateCalcMode
import com.brohit.truecalc.domain.calc.DateUnit
import com.brohit.truecalc.ui.components.tool.BreakdownRow
import com.brohit.truecalc.ui.components.tool.toToolText
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
import java.time.DateTimeException
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject

@HiltViewModel
class DateCalculatorViewModel @Inject constructor(
    private val historyManager: HistoryManager,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val inputState = DateCalculatorInputState()

    init {
        val route: Route.Calculator.DateCalc = savedStateHandle.toRoute()
        val restorationJson = route.restorationJson
        if (!restorationJson.isNullOrEmpty()) {
            try {
                val data = Json.decodeFromString<Map<String, String>>(restorationJson)
                inputState.mode = DateCalcMode.fromKey(data["mode"])
                data["start"]?.takeIf { it.isNotBlank() }?.let { inputState.start = LocalDate.parse(it) }
                data["end"]?.takeIf { it.isNotBlank() }?.let { inputState.end = LocalDate.parse(it) }
                inputState.includeEndDate = data["includeEnd"].toBoolean()
                inputState.amount.updateText(data["amount"] ?: "")
                inputState.unit = DateUnit.fromKey(data["unit"])
                inputState.subtract = data["subtract"].toBoolean()
            } catch (e: Exception) {
                // Ignore malformed history
            }
        }
    }

    fun reset() = inputState.reset()

    private data class Inputs(
        val mode: DateCalcMode,
        val start: LocalDate,
        val end: LocalDate,
        val includeEnd: Boolean,
        val amount: String,
        val unit: DateUnit,
        val subtract: Boolean,
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<DateCalculatorResultState> =
        snapshotFlow {
            Inputs(
                inputState.mode, inputState.start, inputState.end, inputState.includeEndDate,
                inputState.amount.text, inputState.unit, inputState.subtract,
            )
        }
            .mapLatest { inputs ->
                val result = calculate(inputs)
                if (result.heroValue != null) log(inputs, result)
                result
            }
            .flowOn(Dispatchers.Default)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), DateCalculatorResultState())

    private fun plural(n: Long, unit: String): String =
        "${CalcFormat.number(n.toDouble())} $unit${if (n == 1L) "" else "s"}"

    private fun calculate(inputs: Inputs): DateCalculatorResultState = when (inputs.mode) {
        DateCalcMode.Between -> between(inputs)
        DateCalcMode.AddSubtract -> addSubtract(inputs)
    }

    private fun between(inputs: Inputs): DateCalculatorResultState {
        val r = DateCalc.between(inputs.start, inputs.end, inputs.includeEnd)
        val rows = listOf(
            BreakdownRow("Weeks and days", "${plural(r.weeks, "week")}, ${plural(r.weekRemainderDays, "day")}"),
            BreakdownRow("Months and days", "${plural(r.months, "month")}, ${plural(r.monthRemainderDays.toLong(), "day")}"),
            BreakdownRow(
                "Years, months and days",
                "${plural(r.years.toLong(), "year")}, ${plural(r.yearRemainderMonths.toLong(), "month")}, " +
                        plural(r.monthRemainderDays.toLong(), "day"),
            ),
            BreakdownRow("Working days (Mon–Fri)", plural(r.workingDays, "day"), emphasized = true),
            BreakdownRow("Weekend days", plural(r.weekendDays, "day")),
        )
        val range = "${r.start.toToolText(withWeekday = true)} to ${r.end.toToolText(withWeekday = true)}"
        val caption = buildString {
            append(range)
            if (inputs.includeEnd) append(", end date included")
            if (r.reversed) append(" (end date is before start)")
        }
        val total = plural(r.totalDays, "day")
        return DateCalculatorResultState(
            heroLabel = "Duration",
            heroValue = total,
            caption = caption,
            rows = rows,
            shareText = "$range: $total (${plural(r.workingDays, "working day")}) — via TrueCalc",
        )
    }

    private fun addSubtract(inputs: Inputs): DateCalculatorResultState {
        val amount = inputs.amount.toLongOrNull()
            ?: return DateCalculatorResultState(heroLabel = "Result date", emptyText = "Enter how many to add or subtract")
        val result = try {
            DateCalc.add(inputs.start, amount, inputs.unit, inputs.subtract)
        } catch (_: DateTimeException) {
            return DateCalculatorResultState(heroLabel = "Result date", emptyText = "That date is out of range")
        } catch (_: ArithmeticException) {
            return DateCalculatorResultState(heroLabel = "Result date", emptyText = "That date is out of range")
        }
        val unitWord = inputs.unit.label.lowercase().removeSuffix("s")
        val direction = if (inputs.subtract) "before" else "after"
        val caption = "${plural(amount, unitWord)} $direction ${inputs.start.toToolText(withWeekday = true)}"
        val dayDiff = ChronoUnit.DAYS.between(inputs.start, result)
        val rows = listOf(
            BreakdownRow("Day of the week", result.dayOfWeek.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.ENGLISH)),
            BreakdownRow("Days from start date", (if (dayDiff > 0) "+" else "") + CalcFormat.number(dayDiff.toDouble())),
        )
        val resultText = result.toToolText(withWeekday = true)
        return DateCalculatorResultState(
            heroLabel = "Result date",
            heroValue = resultText,
            caption = caption,
            rows = rows,
            shareText = "$caption is $resultText — via TrueCalc",
        )
    }

    private fun log(inputs: Inputs, result: DateCalculatorResultState) {
        val inputMap = when (inputs.mode) {
            DateCalcMode.Between -> mapOf(
                "Mode" to inputs.mode.label,
                "Start date" to inputs.start.toToolText(),
                "End date" to inputs.end.toToolText(),
                "Include end date" to if (inputs.includeEnd) "Yes" else "No",
            )
            DateCalcMode.AddSubtract -> mapOf(
                "Mode" to inputs.mode.label,
                "Start date" to inputs.start.toToolText(),
                (if (inputs.subtract) "Subtract" else "Add") to "${inputs.amount} ${inputs.unit.label.lowercase()}",
            )
        }
        historyManager.log(
            route = Route.Calculator.DateCalc(),
            name = "Date Calculator",
            inputs = inputMap,
            results = mapOf(result.heroLabel to result.heroValue.orEmpty()) +
                    result.rows.associate { it.label to it.value },
            restorationInputs = mapOf(
                "mode" to inputs.mode.key,
                "start" to inputs.start.toString(),
                "end" to inputs.end.toString(),
                "includeEnd" to inputs.includeEnd.toString(),
                "amount" to inputs.amount,
                "unit" to inputs.unit.key,
                "subtract" to inputs.subtract.toString(),
            ),
        )
    }
}
