package com.brohit.truecalc.ui.screens.compund_interest

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brohit.truecalc.core.TextUtils.toFixed2INR
import com.brohit.truecalc.domain.model.TextFieldState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlin.math.pow

class CompoundInterestInputState {
    val principal = TextFieldState(
        validator = {
            it.isNotEmpty() && it.toDoubleOrNull() != null && it.toDouble() > 0
        },
        maxChar = 15
    )

    val interestRate = TextFieldState(
        validator = {
            val rate = it.toDoubleOrNull()
            rate != null && rate in 0.01..100.0
        },
        maxChar = 6
    )

    var isTimeInYears by mutableStateOf(true)

    val time = TextFieldState(
        validator = {
            val value = it.toDoubleOrNull()
            value != null && value > 0
        },
        maxChar = 3
    )

    var compoundingFrequency by mutableStateOf(CompoundingFrequency.Annually)
}

data class CompoundInterestState(
    val amount: Double = 0.0,
    val interest: Double = 0.0,
    val principal: Double = 0.0
) {
    val finalAmount: String = if (amount == 0.0) "₹0.00" else amount.toFixed2INR()
    val interestEarned: String = if (interest == 0.0) "₹0.00" else interest.toFixed2INR()
    val totalInvestment: String = if (principal == 0.0) "₹0.00" else principal.toFixed2INR()
}

data class CompoundInterestInput(
    val principal: String,
    val rate: String,
    val time: String,
    val isTimeInYears: Boolean,
    val compoundingFrequency: CompoundingFrequency
)

@OptIn(ExperimentalCoroutinesApi::class)
class CompoundInterestViewModel : ViewModel() {

    val inputState = CompoundInterestInputState()

    private val stateFlow: StateFlow<CompoundInterestState> = kotlin.run {
        val rate = snapshotFlow { inputState.interestRate.text }
        val principal = snapshotFlow { inputState.principal.text }
        val time = snapshotFlow { inputState.time.text }
        val isTimeInYears = snapshotFlow { inputState.isTimeInYears }
        val compoundingFrequency = snapshotFlow { inputState.compoundingFrequency }

        combine(
            principal, rate, time, isTimeInYears, compoundingFrequency
        ) { p, r, t, i, f ->
            CompoundInterestInput(
                principal = p,
                rate = r,
                time = t,
                isTimeInYears = i,
                compoundingFrequency = f
            )
        }
            .mapLatest { input ->
                calculateCompoundInterest(input)
            }
            .flowOn(Dispatchers.Default)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), CompoundInterestState())

    val state: StateFlow<CompoundInterestState> = stateFlow

    private fun calculateCompoundInterest(input: CompoundInterestInput): CompoundInterestState {
        val principal = input.principal.toDoubleOrNull() ?: return CompoundInterestState()
        val rate = input.rate.toDoubleOrNull()?.div(100) ?: return CompoundInterestState()
        val timeInput = input.time.toDoubleOrNull() ?: return CompoundInterestState()
        val frequency = input.compoundingFrequency.timesPerYear

        // Convert time to years if it's given in months
        val timeInYears = if (input.isTimeInYears) timeInput else timeInput / 12

        val amount = principal * (1 + rate / frequency).pow(frequency * timeInYears)
        val interest = amount - principal

        return CompoundInterestState(
            amount = amount,
            interest = interest,
            principal = principal
        )
    }


}

enum class CompoundingFrequency(val timesPerYear: Int) {
    Annually(1),
    SemiAnnually(2),
    Quarterly(4),
    Monthly(12),
    Daily(365)
}
