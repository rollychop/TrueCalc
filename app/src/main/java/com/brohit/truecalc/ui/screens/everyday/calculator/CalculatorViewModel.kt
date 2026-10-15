package com.brohit.truecalc.ui.screens.everyday.calculator

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.navigation.toRoute
import com.brohit.truecalc.domain.HistoryManager
import com.brohit.truecalc.domain.calc.AngleUnit
import com.brohit.truecalc.domain.calc.CalcFormat
import com.brohit.truecalc.domain.calc.CalcKey
import com.brohit.truecalc.domain.calc.CalculatorEditor
import com.brohit.truecalc.domain.calc.EvalError
import com.brohit.truecalc.domain.calc.EvalResult
import com.brohit.truecalc.domain.calc.ExpressionEvaluator
import com.brohit.truecalc.ui.navigation.Route
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.serialization.json.Json
import javax.inject.Inject

/** One finished calculation on the in-memory tape. */
@Immutable
data class TapeEntry(
    /** Expression as typed (display symbols, ungrouped). */
    val expression: String,
    /** Result for display (grouped). */
    val result: String,
    /** Result as expression text, for reuse. */
    val resultExpression: String,
)

@HiltViewModel
class CalculatorViewModel @Inject constructor(
    private val historyManager: HistoryManager,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    /** The expression line (display symbols, ungrouped; see [CalculatorEditor.displayText]). */
    var expression by mutableStateOf("")
        private set
    var scientific by mutableStateOf(false)
        private set
    var angleUnit by mutableStateOf(AngleUnit.Degrees)
        private set

    /** Error from the last "=", cleared by the next key press. */
    var error by mutableStateOf<EvalError?>(null)
        private set

    /** True right after "=": [expression] holds the result. */
    var justEvaluated by mutableStateOf(false)
        private set

    /** Last calculations, newest first (in memory only; History keeps the persistent log). */
    val tape: SnapshotStateList<TapeEntry> = mutableStateListOf()

    private var lastAnswer: String? = null

    init {
        val route: Route.Calculator.Basic = savedStateHandle.toRoute()
        val restorationJson = route.restorationJson
        if (!restorationJson.isNullOrEmpty()) {
            try {
                val data = Json.decodeFromString<Map<String, String>>(restorationJson)
                expression = data["expression"].orEmpty().take(CalculatorEditor.MAX_LENGTH)
                scientific = data["scientific"].toBoolean()
                angleUnit = AngleUnit.entries.firstOrNull { it.name == data["angle"] } ?: AngleUnit.Degrees
            } catch (e: Exception) {
                // Ignore malformed history
            }
        }
    }

    /** Live result while typing; null when the expression is empty, a bare number or invalid. */
    val preview: String? by derivedStateOf {
        val expr = expression
        if (justEvaluated || expr.isBlank() || isPlainNumber(expr)) return@derivedStateOf null
        when (val result = ExpressionEvaluator.evaluate(expr, angleUnit)) {
            is EvalResult.Success -> result.formatted(grouping = true)
            else -> null
        }
    }

    /** Text for Share: the latest "=" result, else the live preview. */
    val shareText: String? by derivedStateOf {
        val latest = tape.firstOrNull()
        val current = preview
        when {
            current != null -> "${CalculatorEditor.displayText(expression)} = $current"
            latest != null -> "${CalculatorEditor.displayText(latest.expression)} = ${latest.result}"
            else -> null
        }
    }

    fun onKey(key: CalcKey) {
        when (key) {
            CalcKey.Equals -> evaluate()
            CalcKey.AngleToggle -> angleUnit =
                if (angleUnit == AngleUnit.Degrees) AngleUnit.Radians else AngleUnit.Degrees
            else -> {
                expression = CalculatorEditor.apply(expression, key, justEvaluated, lastAnswer)
                justEvaluated = false
                error = null
            }
        }
    }

    fun onScientificChange(enabled: Boolean) {
        scientific = enabled
    }

    /** Puts a tape result back on the expression line. */
    fun useTapeEntry(entry: TapeEntry) {
        expression = entry.resultExpression
        lastAnswer = entry.resultExpression
        justEvaluated = true
        error = null
    }

    fun clearTape() = tape.clear()

    fun reset() {
        expression = ""
        error = null
        justEvaluated = false
        lastAnswer = null
        tape.clear()
    }

    private fun evaluate() {
        val expr = expression
        if (expr.isBlank() || justEvaluated) return
        when (val result = ExpressionEvaluator.evaluate(expr, angleUnit)) {
            is EvalResult.Success -> {
                val plain = result.formatted(grouping = false)
                val resultExpression = CalculatorEditor.toExpressionText(plain)
                val display = result.formatted(grouping = true)
                tape.add(0, TapeEntry(expr, display, resultExpression))
                while (tape.size > MAX_TAPE) tape.removeAt(tape.lastIndex)
                log(expr, display)
                lastAnswer = resultExpression
                expression = resultExpression
                justEvaluated = true
                error = null
            }
            is EvalResult.Failure -> error = result.error
            EvalResult.Empty -> Unit
        }
    }

    private fun log(expr: String, result: String) {
        historyManager.log(
            route = Route.Calculator.Basic(),
            name = "Calculator",
            inputs = mapOf("Expression" to CalculatorEditor.displayText(expr)),
            results = mapOf("Result" to result),
            restorationInputs = mapOf(
                "expression" to expr,
                "scientific" to scientific.toString(),
                "angle" to angleUnit.name,
            ),
        )
    }

    private fun isPlainNumber(expr: String): Boolean =
        CalcFormat.parse(expr.replace('−', '-')) != null

    companion object {
        const val MAX_TAPE = 50
    }
}
