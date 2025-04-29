package com.brohit.truecalc.ui.screens.recurring_deposit

import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brohit.truecalc.domain.model.CompoundingFrequency
import com.brohit.truecalc.domain.model.InvestmentResultState
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
class RecurringDepositViewModel : ViewModel() {
    val inputState = RecurringDepositInputState()

    private val stateFlow: StateFlow<InvestmentResultState> = kotlin.run {
        val interval = snapshotFlow { inputState.recurringInterval }
        val rate = snapshotFlow { inputState.interestRate.text }
        val principal = snapshotFlow { inputState.principal.text }
        val time = snapshotFlow { inputState.time.text }
        val isTimeInYears = snapshotFlow { inputState.isTimeInYears }
        val compoundingFrequency = snapshotFlow { inputState.compoundingFrequency }

        combine(
            interval, principal, rate, time, isTimeInYears, compoundingFrequency
        ) { ar ->
            RecurringDepositInput(
                interval = ar[0] as RecurringInterval,
                principal = ar[1] as String,
                rate = ar[2] as String,
                time = ar[3] as String,
                isTimeInYears = ar[4] as Boolean,
                compoundingFrequency = ar[5] as CompoundingFrequency
            )
        }.mapLatest { input ->
            calculateRecurringDeposit(input)
        }
            .flowOn(Dispatchers.Default)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), InvestmentResultState())
    val state: StateFlow<InvestmentResultState> = stateFlow

    private fun calculateRecurringDeposit(input: RecurringDepositInput): InvestmentResultState {
        val interval = input.interval
        val principal = input.principal.toDoubleOrNull() ?: return InvestmentResultState()
        val rate = input.rate.toDoubleOrNull()?.div(100) ?: return InvestmentResultState()
        val timeInput = input.time.toDoubleOrNull() ?: return InvestmentResultState()
        val n = input.compoundingFrequency.timesPerYear

        // Convert total time in years
        val timeInYears = if (input.isTimeInYears) timeInput else timeInput / 12

        val ppy =
            interval.paymentsPerYear // payments per year based on user interval (monthly, weekly, etc.)

        // Total number of installments
        val totalInstallments = (timeInYears * ppy).toInt()

        val r = rate / n // rate per compounding period

        // Effective interest calculation
        val onePlusR = 1 + r
        val factor = onePlusR.pow(n * timeInYears)
        val top = factor - 1
        val bottom = onePlusR.pow(-1.0 * n / ppy)

        val maturityAmount = principal * (top / (1 - bottom))
        val totalInvested = principal * totalInstallments
        val interest = maturityAmount - totalInvested

        return InvestmentResultState(
            amount = maturityAmount,
            interest = interest,
            principal = totalInvested
        )
    }


}

