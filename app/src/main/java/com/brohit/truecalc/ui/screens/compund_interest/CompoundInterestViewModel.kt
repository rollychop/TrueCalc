package com.brohit.truecalc.ui.screens.compund_interest

import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.brohit.truecalc.domain.HistoryManager
import com.brohit.truecalc.domain.calc.CompoundingFrequency
import com.brohit.truecalc.domain.calc.InterestCalc
import com.brohit.truecalc.domain.calc.InterestResult
import com.brohit.truecalc.domain.model.InvestmentResultState
import com.brohit.truecalc.ui.navigation.Route
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.json.Json
import javax.inject.Inject


@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CompoundInterestViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val historyManager: HistoryManager
) : ViewModel() {

    private val route: Route.Calculator.CompoundInterest = savedStateHandle.toRoute()
    private val calculationType: String = route.calculationType
    val inputState = CompoundInterestInputState(calculationType)
    
    init {
        val restorationJson = route.restorationJson
        if (!restorationJson.isNullOrEmpty()) {
            try {
                val data = Json.decodeFromString<Map<String, String>>(restorationJson)
                inputState.principal.updateText(data["principal"] ?: "")
                inputState.interestRate.updateText(data["rate"] ?: "")
                inputState.time.updateText(data["time"] ?: "")
                inputState.isTimeInYears = data["isTimeInYears"]?.toBoolean() ?: true
                data["frequency"]?.let { freqName ->
                    inputState.compoundingFrequency = CompoundingFrequency.valueOf(freqName)
                }
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    /** Clears the inputs back to their defaults. */
    fun reset() = inputState.reset()

    private val stateFlow: StateFlow<InvestmentResultState> = kotlin.run {
        val type = snapshotFlow { inputState.calculationType }
        val rate = snapshotFlow { inputState.interestRate.text }
        val principal = snapshotFlow { inputState.principal.text }
        val time = snapshotFlow { inputState.time.text }
        val isTimeInYears = snapshotFlow { inputState.isTimeInYears }
        val compoundingFrequency = snapshotFlow { inputState.compoundingFrequency }

        combine<Any, CompoundInterestInput>(
            type, principal, rate, time, isTimeInYears, compoundingFrequency
        ) { ar ->
            CompoundInterestInput(
                type = ar[0] as String,
                principal = ar[1] as String,
                rate = ar[2] as String,
                time = ar[3] as String,
                isTimeInYears = ar[4] as Boolean,
                compoundingFrequency = ar[5] as CompoundingFrequency
            )
        }.mapLatest { input ->
            val result = calculateCompoundInterest(input)
            if (result.amount > 0) {
                logCalculation(input, result)
            }
            result
        }
            .flowOn(Dispatchers.Default)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), InvestmentResultState())
    val state: StateFlow<InvestmentResultState> = stateFlow

    private fun logCalculation(input: CompoundInterestInput, result: InvestmentResultState) {
        historyManager.log(
            route = Route.Calculator.CompoundInterest(input.type),
            name = input.type,
            inputs = mapOf(
                "Principal" to "₹${input.principal}",
                "Interest Rate" to "${input.rate}%",
                "Time" to "${input.time} ${if (input.isTimeInYears) "Years" else "Months"}",
                "Frequency" to input.compoundingFrequency.getLabelFor(input.type)
            ),
            results = mapOf(
                "Final Amount" to result.finalAmount,
                "Interest Earned" to result.interestEarned,
                "Total Invested" to result.totalInvestment
            ),
            restorationInputs = mapOf(
                "principal" to input.principal,
                "rate" to input.rate,
                "time" to input.time,
                "isTimeInYears" to input.isTimeInYears.toString(),
                "frequency" to input.compoundingFrequency.name
            )
        )
    }

    private fun calculateCompoundInterest(input: CompoundInterestInput): InvestmentResultState {
        val result = when (input.type) {
            CalculationType.COMPOUND -> InterestCalc.compound(
                input.principal, input.rate, input.time, input.isTimeInYears, input.compoundingFrequency
            )

            CalculationType.FIXED -> InterestCalc.fixedDeposit(
                input.principal, input.rate, input.time, input.isTimeInYears, input.compoundingFrequency
            )

            CalculationType.SIMPLE -> InterestCalc.simple(
                input.principal, input.rate, input.time, input.isTimeInYears
            )

            else -> InterestResult.Empty
        }
        val state = InvestmentResultState(
            amount = result.amount,
            interest = result.interest,
            principal = result.principal
        )
        return if (state.hasResult) state.copy(shareText = shareTextFor(input, state)) else state
    }

    private fun shareTextFor(input: CompoundInterestInput, state: InvestmentResultState): String {
        val unit = if (input.isTimeInYears) "years" else "months"
        val frequency = if (input.type == CalculationType.SIMPLE) ""
        else " (${input.compoundingFrequency.getLabelFor(input.type).lowercase()})"
        return "${input.type}: ${state.totalInvestment} at ${input.rate}%$frequency for " +
                "${input.time} $unit grows to ${state.finalAmount} " +
                "(interest ${state.interestEarned}) — via TrueCalc"
    }
}

