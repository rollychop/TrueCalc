package com.brohit.truecalc.ui.screens.everyday.split_bill

import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.brohit.truecalc.core.TextUtils.toFixed2INR
import com.brohit.truecalc.domain.HistoryManager
import com.brohit.truecalc.domain.calc.CalcFormat
import com.brohit.truecalc.domain.calc.SplitBillCalc
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
class SplitBillViewModel @Inject constructor(
    private val historyManager: HistoryManager,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val inputState = SplitBillInputState()

    init {
        val route: Route.Calculator.SplitBill = savedStateHandle.toRoute()
        val restorationJson = route.restorationJson
        if (!restorationJson.isNullOrEmpty()) {
            try {
                val data = Json.decodeFromString<Map<String, String>>(restorationJson)
                inputState.bill.updateText(data["bill"] ?: "")
                data["people"]?.let { inputState.people.updateText(it) }
                data["tip"]?.let { inputState.tip.updateText(it) }
                inputState.roundUp = data["roundUp"].toBoolean()
            } catch (e: Exception) {
                // Ignore malformed history
            }
        }
    }

    fun reset() = inputState.reset()

    private data class Inputs(val bill: String, val people: String, val tip: String, val roundUp: Boolean)

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<SplitBillResultState> =
        snapshotFlow {
            Inputs(inputState.bill.text, inputState.people.text, inputState.tip.text, inputState.roundUp)
        }
            .mapLatest { inputs ->
                val result = calculate(inputs)
                if (result.perPerson != null) log(inputs, result)
                result
            }
            .flowOn(Dispatchers.Default)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), SplitBillResultState())

    private fun calculate(inputs: Inputs): SplitBillResultState {
        val bill = CalcFormat.parse(inputs.bill) ?: return SplitBillResultState()
        val people = inputs.people.toIntOrNull()
            ?: return SplitBillResultState(emptyText = "Enter how many people are splitting")
        val tipPercent = if (inputs.tip.isBlank()) 0.0 else CalcFormat.parse(inputs.tip)
            ?: return SplitBillResultState()
        val r = SplitBillCalc.calculate(bill, people, tipPercent, inputs.roundUp)
            ?: return SplitBillResultState(emptyText = "At least 1 person has to pay")

        val tipText = CalcFormat.number(tipPercent) + "%"
        val rows = buildList {
            add(BreakdownRow("Bill", r.bill.toFixed2INR()))
            add(BreakdownRow("Tip ($tipText)", r.tip.toFixed2INR()))
            add(BreakdownRow("Total with tip", r.total.toFixed2INR()))
            add(BreakdownRow("People", r.people.toString()))
            if (r.tip > 0.0) add(BreakdownRow("Tip per person", r.tipPerPerson.toFixed2INR()))
            if (inputs.roundUp) {
                add(BreakdownRow("Collected (rounded up)", r.collected.toFixed2INR()))
                add(BreakdownRow("Extra from rounding", r.roundingExtra.toFixed2INR()))
            }
            add(BreakdownRow("Each person pays", r.perPerson.toFixed2INR(), emphasized = true))
        }
        val perPerson = r.perPerson.toFixed2INR()
        return SplitBillResultState(
            perPerson = perPerson,
            caption = "Total ${r.total.toFixed2INR()} incl. ${r.tip.toFixed2INR()} tip, " +
                    "split ${r.people} ways",
            rows = rows,
            shareText = "Bill ${r.total.toFixed2INR()} (incl. $tipText tip) split ${r.people} ways: " +
                    "$perPerson each — via TrueCalc",
        )
    }

    private fun log(inputs: Inputs, result: SplitBillResultState) {
        historyManager.log(
            route = Route.Calculator.SplitBill(),
            name = "Split Bill & Tip",
            inputs = mapOf(
                "Bill" to "₹${inputs.bill}",
                "People" to inputs.people,
                "Tip" to "${inputs.tip.ifBlank { "0" }}%",
                "Round up" to if (inputs.roundUp) "Yes" else "No",
            ),
            results = result.rows.associate { it.label to it.value },
            restorationInputs = mapOf(
                "bill" to inputs.bill,
                "people" to inputs.people,
                "tip" to inputs.tip,
                "roundUp" to inputs.roundUp.toString(),
            ),
        )
    }
}
