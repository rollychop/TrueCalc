package com.brohit.truecalc.ui.screens.recurring_deposit

import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.brohit.truecalc.domain.HistoryManager
import com.brohit.truecalc.domain.model.CompoundingFrequency
import com.brohit.truecalc.domain.model.InvestmentResultState
import com.brohit.truecalc.ui.navigation.Route
import com.brohit.truecalc.ui.screens.compund_interest.CalculationType
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
import kotlin.math.pow


@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class RecurringDepositViewModel @Inject constructor(
    private val historyManager: HistoryManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    val inputState = RecurringDepositInputState()

    init {
        val route: Route.RecurringDepositCalculator = savedStateHandle.toRoute()
        val restorationJson = route.restorationJson
        if (!restorationJson.isNullOrEmpty()) {
            try {
                val data = Json.decodeFromString<Map<String, String>>(restorationJson)
                inputState.principal.updateText(data["principal"] ?: "")
                inputState.interestRate.updateText(data["rate"] ?: "")
                inputState.time.updateText(data["time"] ?: "")
                inputState.isTimeInYears = data["isTimeInYears"]?.toBoolean() ?: true
                data["interval"]?.let { intervalName ->
                    inputState.recurringInterval = RecurringInterval.valueOf(intervalName)
                }
                data["frequency"]?.let { freqName ->
                    inputState.compoundingFrequency = CompoundingFrequency.valueOf(freqName)
                }
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    private val stateFlow: StateFlow<InvestmentResultState> = kotlin.run {
        val interval = snapshotFlow { inputState.recurringInterval }
        val rate = snapshotFlow { inputState.interestRate.text }
        val principal = snapshotFlow { inputState.principal.text }
        val time = snapshotFlow { inputState.time.text }
        val isTimeInYears = snapshotFlow { inputState.isTimeInYears }
        val compoundingFrequency = snapshotFlow { inputState.compoundingFrequency }

        combine<Any, RecurringDepositInput>(
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
            val result = calculateRecurringDeposit(input)
            if (result.amount > 0) {
                logCalculation(input, result)
            }
            result
        }
            .flowOn(Dispatchers.Default)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), InvestmentResultState())
    val state: StateFlow<InvestmentResultState> = stateFlow

    private fun logCalculation(input: RecurringDepositInput, result: InvestmentResultState) {
        historyManager.log(
            route = Route.RecurringDepositCalculator(),
            name = "Recurring Deposit",
            inputs = mapOf(
                "Installment" to "₹${input.principal}",
                "Interval" to input.interval.label,
                "Interest Rate" to "${input.rate}%",
                "Time" to "${input.time} ${if (input.isTimeInYears) "Years" else "Months"}",
                "Payout" to input.compoundingFrequency.getLabelFor(CalculationType.FIXED)
            ),
            results = mapOf(
                "Maturity Amount" to result.finalAmount,
                "Interest Earned" to result.interestEarned,
                "Total Invested" to result.totalInvestment
            ),
            restorationInputs = mapOf(
                "principal" to input.principal,
                "rate" to input.rate,
                "time" to input.time,
                "isTimeInYears" to input.isTimeInYears.toString(),
                "interval" to input.interval.name,
                "frequency" to input.compoundingFrequency.name
            )
        )
    }

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

