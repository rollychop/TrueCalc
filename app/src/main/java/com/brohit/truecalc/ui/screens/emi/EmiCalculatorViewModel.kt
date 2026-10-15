package com.brohit.truecalc.ui.screens.emi

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.brohit.truecalc.core.TextUtils.toFixed2
import com.brohit.truecalc.core.TextUtils.toFixed2INR
import com.brohit.truecalc.data.data_source.local.room.entity.FixedCharge
import com.brohit.truecalc.domain.FixedChargesRepository
import com.brohit.truecalc.domain.HistoryManager
import com.brohit.truecalc.domain.calc.EmiCalc
import com.brohit.truecalc.domain.calc.EmiCharge
import com.brohit.truecalc.domain.model.TextFieldState
import com.brohit.truecalc.ui.navigation.Route
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import javax.inject.Inject

class EmiCalculatorInputState {
    val principal = TextFieldState(
        validator = {
            it.isDigitsOnly() && it.isNotEmpty() && it.toLongOrNull() in 1L..999_999_999_999_999L
        },
        maxChar = 15
    )

    val interestRate = TextFieldState(
        validator = {
            val rate = it.toDoubleOrNull()
            rate != null && rate in 0.01..999.0
        },
        maxChar = 6
    )

    var isTermInYears by mutableStateOf(true)

    val term = TextFieldState(
        validator = {
            val value = it.toIntOrNull()
            if (value == null || value <= 0) return@TextFieldState false

            if (isTermInYears) value in 1..999
            else value in 1..9999
        },
        maxChar = 4
    )
}


data class EmiCalculatorState(
    val mp: Double = 0.0,
    val tp: Double = 0.0,
    val ti: Double = 0.0,
    val tc: Double = 0.0,
    val eir: Double = 0.0,
    /** Text for the Share action; null until there is a valid result. */
    val shareText: String? = null,
) {
    /** True when the inputs produced a usable installment. */
    val hasResult: Boolean get() = mp > 0 && mp.isFinite()

    val monthlyPayment: String = mp.toFixed2INR()
    val totalPaid: String = tp.toFixed2INR()
    val totalInterest: String = ti.toFixed2INR()
    val totalCharges: String = tc.toFixed2INR()
    val effectiveInterestRate: String = eir.toFixed2() + "%"
}

data class EmiInput(
    val principal: String,
    val rate: String,
    val term: String,
    val isTermInYears: Boolean,
    val fixedCharges: List<FixedCharge> = emptyList()
)


@HiltViewModel
class EmiCalculatorViewModel @Inject constructor(
    private val repository: FixedChargesRepository,
    private val historyManager: HistoryManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {


    val inputState = EmiCalculatorInputState()
    
    init {
        val route: Route.Calculator.Emi = savedStateHandle.toRoute()
        val restorationJson: String? = route.restorationJson
        if (!restorationJson.isNullOrEmpty()) {
            try {
                val data = Json.decodeFromString<Map<String, String>>(restorationJson)
                inputState.principal.updateText(data["principal"] ?: "")
                inputState.interestRate.updateText(data["rate"] ?: "")
                inputState.term.updateText(data["term"] ?: "")
                inputState.isTermInYears = data["isTermInYears"]?.toBoolean() ?: true
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    /** Clears the inputs back to their defaults. */
    fun reset() {
        inputState.principal.reset()
        inputState.interestRate.reset()
        inputState.term.reset()
        inputState.isTermInYears = true
    }

    val allFixedCharges: StateFlow<List<FixedCharge>> = repository.allFixedCharges
        .flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val state: StateFlow<EmiCalculatorState> = kotlin.run {
        val rate = snapshotFlow { inputState.interestRate.text }
        val principal = snapshotFlow { inputState.principal.text }
        val term = snapshotFlow { inputState.term.text }
        val isTermInYears = snapshotFlow { inputState.isTermInYears }

        combine(
            rate,
            principal,
            term,
            isTermInYears,
            allFixedCharges
        ) { r, p, t, i, fc ->
            EmiInput(
                principal = p,
                rate = r,
                term = t,
                isTermInYears = i,
                fc
            )
        }
            .mapLatest { emiInput ->
                val result = calculateEmi(emiInput)
                if (result.mp > 0) {
                    logCalculation(emiInput, result)
                }
                result
            }
            .flowOn(Dispatchers.Default)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), EmiCalculatorState())

    private fun logCalculation(input: EmiInput, result: EmiCalculatorState) {
        historyManager.log(
            route = Route.Calculator.Emi(),
            name = "EMI Calculator",
            inputs = mapOf(
                "Principal" to "₹${input.principal}",
                "Interest Rate" to "${input.rate}%",
                "Term" to "${input.term} ${if (input.isTermInYears) "Years" else "Months"}"
            ),
            results = mapOf(
                "Monthly EMI" to result.monthlyPayment,
                "Total Interest" to result.totalInterest,
                "Total Payment" to result.totalPaid
            ),
            restorationInputs = mapOf(
                "principal" to input.principal,
                "rate" to input.rate,
                "term" to input.term,
                "isTermInYears" to input.isTermInYears.toString()
            )
        )
    }

    private fun calculateEmi(input: EmiInput): EmiCalculatorState {
        val result = EmiCalc.calculate(
            principal = input.principal,
            rate = input.rate,
            term = input.term,
            isTermInYears = input.isTermInYears,
            charges = input.fixedCharges.map { EmiCharge(amount = it.amount, isPercentage = it.isPercentage) }
        )
        val state = EmiCalculatorState(
            mp = result.monthlyPayment,
            tp = result.totalPaid,
            ti = result.totalInterest,
            tc = result.totalCharges,
            eir = result.effectiveInterestRate
        )
        return if (state.hasResult) state.copy(shareText = shareTextFor(input, state)) else state
    }

    private fun shareTextFor(input: EmiInput, state: EmiCalculatorState): String {
        val principal = input.principal.toDoubleOrNull()?.toFixed2INR() ?: "₹${input.principal}"
        val unit = if (input.isTermInYears) "years" else "months"
        return "EMI ${state.monthlyPayment}/month for $principal at ${input.rate}% over " +
                "${input.term} $unit (total interest ${state.totalInterest}) — via TrueCalc"
    }


    fun insertFixedCharges(fixedCharges: FixedCharge) {
        viewModelScope.launch {
            repository.insert(fixedCharges)
        }
    }

    fun updateFixedCharge(copy: FixedCharge) {
        viewModelScope.launch {
            repository.insert(copy)
        }
    }

    fun deleteFixedCharge(it: FixedCharge) {
        viewModelScope.launch {
            repository.delete(it)
        }
    }
}
