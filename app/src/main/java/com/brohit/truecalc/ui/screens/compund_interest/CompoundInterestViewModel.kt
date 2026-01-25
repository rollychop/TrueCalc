package com.brohit.truecalc.ui.screens.compund_interest

import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.brohit.truecalc.domain.model.CompoundingFrequency
import com.brohit.truecalc.domain.model.InvestmentResultState
import com.brohit.truecalc.ui.navigation.Route
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlin.math.pow


@OptIn(ExperimentalCoroutinesApi::class)
class CompoundInterestViewModel(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val calculationType: String =
        savedStateHandle.toRoute<Route.CompoundInterestCalculator>().calculationType
    val inputState = CompoundInterestInputState(calculationType)

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
            calculateCompoundInterest(input)
        }
            .flowOn(Dispatchers.Default)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), InvestmentResultState())
    val state: StateFlow<InvestmentResultState> = stateFlow

    private fun calculateCompoundInterest(input: CompoundInterestInput): InvestmentResultState {
        val principal = input.principal.toDoubleOrNull() ?: return InvestmentResultState()
        val rate = input.rate.toDoubleOrNull()?.div(100) ?: return InvestmentResultState()
        val timeInput = input.time.toDoubleOrNull() ?: return InvestmentResultState()
        val frequency = input.compoundingFrequency.timesPerYear

        // Convert time to years if it's given in months
        val timeInYears = if (input.isTimeInYears) timeInput else timeInput / 12

        val amount: Double
        val interest: Double
        when (input.type) {
            CalculationType.COMPOUND, CalculationType.FIXED -> {
                amount = principal * (1 + rate / frequency).pow(frequency * timeInYears)
                interest = amount - principal
            }

            CalculationType.SIMPLE -> {
                interest = principal * rate * timeInYears
                amount = interest + principal
            }

            else -> return InvestmentResultState()
        }

        return InvestmentResultState(
            amount = amount,
            interest = interest,
            principal = principal
        )
    }


}

