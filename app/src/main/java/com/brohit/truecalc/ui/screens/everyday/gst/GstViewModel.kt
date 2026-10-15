package com.brohit.truecalc.ui.screens.everyday.gst

import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.brohit.truecalc.core.TextUtils.toFixed2INR
import com.brohit.truecalc.domain.HistoryManager
import com.brohit.truecalc.domain.calc.CalcFormat
import com.brohit.truecalc.domain.calc.GstCalc
import com.brohit.truecalc.domain.calc.GstMode
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
class GstViewModel @Inject constructor(
    private val historyManager: HistoryManager,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val inputState = GstInputState()

    init {
        val route: Route.Calculator.Gst = savedStateHandle.toRoute()
        val restorationJson = route.restorationJson
        if (!restorationJson.isNullOrEmpty()) {
            try {
                val data = Json.decodeFromString<Map<String, String>>(restorationJson)
                inputState.mode = GstMode.fromKey(data["mode"])
                inputState.amount.updateText(data["amount"] ?: "")
                data["rate"]?.let { inputState.rate.updateText(it) }
            } catch (e: Exception) {
                // Ignore malformed history
            }
        }
    }

    fun reset() = inputState.reset()

    private data class Inputs(val mode: GstMode, val amount: String, val rate: String)

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<GstResultState> =
        snapshotFlow { Inputs(inputState.mode, inputState.amount.text, inputState.rate.text) }
            .mapLatest { inputs ->
                val result = calculate(inputs)
                if (result.heroValue != null) log(inputs, result)
                result
            }
            .flowOn(Dispatchers.Default)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), GstResultState())

    private fun calculate(inputs: Inputs): GstResultState {
        val heroLabel = if (inputs.mode == GstMode.Add) "Total incl. GST" else "Amount before GST"
        val amount = CalcFormat.parse(inputs.amount) ?: return GstResultState(heroLabel = heroLabel)
        val rate = CalcFormat.parse(inputs.rate)
            ?: return GstResultState(heroLabel = heroLabel, emptyText = "Choose a GST rate")
        val r = GstCalc.calculate(amount, rate, inputs.mode)
            ?: return GstResultState(heroLabel = heroLabel, emptyText = "The GST rate must be between 0 and 100%")

        val rateText = CalcFormat.number(rate, maxDecimals = 3) + "%"
        val halfRateText = CalcFormat.number(rate / 2, maxDecimals = 3) + "%"
        val hero = if (inputs.mode == GstMode.Add) r.gross else r.net
        val rows = listOf(
            BreakdownRow("Net amount (before GST)", r.net.toFixed2INR()),
            BreakdownRow("CGST ($halfRateText)", r.cgst.toFixed2INR()),
            BreakdownRow("SGST ($halfRateText)", r.sgst.toFixed2INR()),
            BreakdownRow("Total GST ($rateText)", r.gst.toFixed2INR()),
            BreakdownRow("Gross amount (incl. GST)", r.gross.toFixed2INR(), emphasized = true),
        )
        return GstResultState(
            heroLabel = heroLabel,
            heroValue = hero.toFixed2INR(),
            caption = "GST ${r.gst.toFixed2INR()} at $rateText",
            rows = rows,
            shareText = "Net ${r.net.toFixed2INR()} + GST $rateText ${r.gst.toFixed2INR()} " +
                    "(CGST ${r.cgst.toFixed2INR()} + SGST ${r.sgst.toFixed2INR()}) = " +
                    "${r.gross.toFixed2INR()} — via TrueCalc",
        )
    }

    private fun log(inputs: Inputs, result: GstResultState) {
        historyManager.log(
            route = Route.Calculator.Gst(),
            name = "GST Calculator",
            inputs = mapOf(
                "Mode" to inputs.mode.label,
                "Amount" to "₹${inputs.amount}",
                "GST rate" to "${inputs.rate}%",
            ),
            results = result.rows.associate { it.label to it.value },
            restorationInputs = mapOf(
                "mode" to inputs.mode.key,
                "amount" to inputs.amount,
                "rate" to inputs.rate,
            ),
        )
    }
}
