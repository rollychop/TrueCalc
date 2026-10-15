package com.brohit.truecalc.ui.screens.everyday.discount

import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.brohit.truecalc.core.TextUtils.toFixed2INR
import com.brohit.truecalc.domain.HistoryManager
import com.brohit.truecalc.domain.calc.CalcFormat
import com.brohit.truecalc.domain.calc.DiscountCalc
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
class DiscountViewModel @Inject constructor(
    private val historyManager: HistoryManager,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val inputState = DiscountInputState()

    init {
        val route: Route.Calculator.Discount = savedStateHandle.toRoute()
        val restorationJson = route.restorationJson
        if (!restorationJson.isNullOrEmpty()) {
            try {
                val data = Json.decodeFromString<Map<String, String>>(restorationJson)
                inputState.price.updateText(data["price"] ?: "")
                inputState.discount.updateText(data["discount"] ?: "")
                inputState.secondDiscount.updateText(data["secondDiscount"] ?: "")
                inputState.flatOff.updateText(data["flatOff"] ?: "")
            } catch (e: Exception) {
                // Ignore malformed history
            }
        }
    }

    fun reset() = inputState.reset()

    private data class Inputs(val price: String, val discount: String, val second: String, val flat: String)

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<DiscountResultState> =
        snapshotFlow {
            Inputs(
                inputState.price.text,
                inputState.discount.text,
                inputState.secondDiscount.text,
                inputState.flatOff.text,
            )
        }
            .mapLatest { inputs ->
                val result = calculate(inputs)
                if (result.finalPrice != null) log(inputs, result)
                result
            }
            .flowOn(Dispatchers.Default)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), DiscountResultState())

    private fun pct(value: Double): String = CalcFormat.number(value, maxDecimals = 2) + "%"

    private fun calculate(inputs: Inputs): DiscountResultState {
        if (inputs.price.isBlank() || inputs.discount.isBlank()) return DiscountResultState()
        val r = DiscountCalc.calculate(inputs.price, inputs.discount, inputs.second, inputs.flat)
            ?: return DiscountResultState(emptyText = "Discounts must be between 0 and 100%")
        val rows = buildList {
            add(BreakdownRow("Original price", r.originalPrice.toFixed2INR()))
            add(BreakdownRow("Discount (${inputs.discount}%)", "− " + r.firstDiscountAmount.toFixed2INR()))
            if (r.secondDiscountAmount > 0.0) {
                add(BreakdownRow("Extra discount (${inputs.second}%)", "− " + r.secondDiscountAmount.toFixed2INR()))
            }
            if (r.flatAmount > 0.0) {
                add(BreakdownRow("Flat off", "− " + r.flatAmount.toFixed2INR()))
            }
            add(BreakdownRow("You save", r.saved.toFixed2INR()))
            add(BreakdownRow("Effective discount", pct(r.effectivePercent)))
            add(BreakdownRow("Final price", r.finalPrice.toFixed2INR(), emphasized = true))
        }
        val finalText = r.finalPrice.toFixed2INR()
        return DiscountResultState(
            finalPrice = finalText,
            saved = r.saved.toFixed2INR(),
            effectivePercent = pct(r.effectivePercent),
            rows = rows,
            shareText = "${r.originalPrice.toFixed2INR()} with ${pct(r.effectivePercent)} off is " +
                    "$finalText (save ${r.saved.toFixed2INR()}) — via TrueCalc",
        )
    }

    private fun log(inputs: Inputs, result: DiscountResultState) {
        historyManager.log(
            route = Route.Calculator.Discount(),
            name = "Discount",
            inputs = buildMap {
                put("Price", "₹${inputs.price}")
                put("Discount", "${inputs.discount}%")
                if (inputs.second.isNotBlank()) put("Extra discount", "${inputs.second}%")
                if (inputs.flat.isNotBlank()) put("Flat off", "₹${inputs.flat}")
            },
            results = mapOf(
                "Final price" to result.finalPrice.orEmpty(),
                "You save" to result.saved,
                "Effective discount" to result.effectivePercent,
            ),
            restorationInputs = mapOf(
                "price" to inputs.price,
                "discount" to inputs.discount,
                "secondDiscount" to inputs.second,
                "flatOff" to inputs.flat,
            ),
        )
    }
}
