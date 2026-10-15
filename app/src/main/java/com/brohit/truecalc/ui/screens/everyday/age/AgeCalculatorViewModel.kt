package com.brohit.truecalc.ui.screens.everyday.age

import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.brohit.truecalc.domain.HistoryManager
import com.brohit.truecalc.domain.calc.AgeCalc
import com.brohit.truecalc.domain.calc.CalcFormat
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
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import javax.inject.Inject

@HiltViewModel
class AgeCalculatorViewModel @Inject constructor(
    private val historyManager: HistoryManager,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val inputState = AgeInputState()

    init {
        val route: Route.Calculator.Age = savedStateHandle.toRoute()
        val restorationJson = route.restorationJson
        if (!restorationJson.isNullOrEmpty()) {
            try {
                val data = Json.decodeFromString<Map<String, String>>(restorationJson)
                data["dob"]?.takeIf { it.isNotBlank() }?.let { inputState.dateOfBirth = LocalDate.parse(it) }
                data["asOf"]?.takeIf { it.isNotBlank() }?.let { inputState.asOf = LocalDate.parse(it) }
            } catch (e: Exception) {
                // Ignore malformed history
            }
        }
    }

    fun reset() = inputState.reset()

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<AgeResultState> =
        snapshotFlow { inputState.dateOfBirth to inputState.asOf }
            .mapLatest { (dob, asOf) ->
                val result = calculate(dob, asOf)
                if (dob != null && result.headline != null) log(dob, asOf, result)
                result
            }
            .flowOn(Dispatchers.Default)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), AgeResultState())

    private fun plural(n: Long, unit: String): String =
        "${CalcFormat.number(n.toDouble())} $unit${if (n == 1L) "" else "s"}"

    private fun DayOfWeek.label(): String = getDisplayName(TextStyle.FULL, java.util.Locale.ENGLISH)

    private fun calculate(dob: LocalDate?, asOf: LocalDate): AgeResultState {
        if (dob == null) return AgeResultState()
        val r = AgeCalc.calculate(dob, asOf)
            ?: return AgeResultState(emptyText = "The date of birth is after the \"as of\" date")

        val headline = plural(r.years.toLong(), "year")
        val rest = "${plural(r.months.toLong(), "month")} and ${plural(r.days.toLong(), "day")}"
        val details = listOf(
            BreakdownRow("Born on", r.bornOn.label()),
            BreakdownRow("Total months", plural(r.totalMonths, "month")),
            BreakdownRow(
                "Total weeks",
                plural(r.totalWeeks, "week") + if (r.remainderDays > 0) ", ${plural(r.remainderDays, "day")}" else "",
            ),
            BreakdownRow("Total days", plural(r.totalDays, "day")),
        )
        val next = listOf(
            BreakdownRow("Date", r.nextBirthday.toToolText(withWeekday = true)),
            BreakdownRow(
                "Days to go",
                if (r.isBirthdayToday) "Today" else plural(r.daysToNextBirthday, "day"),
            ),
            BreakdownRow("Turning", plural(r.nextAge.toLong(), "year")),
        )
        return AgeResultState(
            headline = headline,
            caption = if (r.isBirthdayToday) "$rest · Happy birthday!" else rest,
            details = details,
            nextBirthday = next,
            shareText = "Age on ${asOf.toToolText()}: ${r.years} years, ${r.months} months and " +
                    "${r.days} days (born on a ${r.bornOn.label()}) — via TrueCalc",
        )
    }

    private fun log(dob: LocalDate, asOf: LocalDate, result: AgeResultState) {
        historyManager.log(
            route = Route.Calculator.Age(),
            name = "Age Calculator",
            inputs = mapOf(
                "Date of birth" to dob.toToolText(),
                "As of" to asOf.toToolText(),
            ),
            results = mapOf(
                "Age" to "${result.headline}, ${result.caption}",
            ) + result.nextBirthday.associate { "Next birthday: ${it.label}" to it.value },
            restorationInputs = mapOf(
                "dob" to dob.toString(),
                "asOf" to asOf.toString(),
            ),
        )
    }
}
