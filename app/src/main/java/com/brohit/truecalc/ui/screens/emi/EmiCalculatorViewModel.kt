package com.brohit.truecalc.ui.screens.emi

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brohit.truecalc.core.MathUtils.calculateInterestRate
import com.brohit.truecalc.core.TextUtils.toFixed2
import com.brohit.truecalc.core.TextUtils.toFixed2INR
import com.brohit.truecalc.data.data_source.local.room.entity.FixedCharge
import com.brohit.truecalc.domain.FixedChargesRepository
import com.brohit.truecalc.domain.model.TextFieldState
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
import javax.inject.Inject
import kotlin.math.pow

class EmiCalculatorInputState {
    val principal = TextFieldState(
        validator = {
            it.isDigitsOnly() && it.isNotEmpty() && it.toLongOrNull() in 1..999_999_999_999_999
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
) {
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
    private val repository: FixedChargesRepository
) : ViewModel() {


    val inputState = EmiCalculatorInputState()

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
                calculateEmi(emiInput)
            }
            .flowOn(Dispatchers.Default)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), EmiCalculatorState())

    private fun calculateEmi(input: EmiInput): EmiCalculatorState {
        val fixedCharges = input.fixedCharges

        val principal = input.principal.toDoubleOrNull() ?: return EmiCalculatorState()
        val nominalRate =
            input.rate.toDoubleOrNull()?.div(12)?.div(100) ?: return EmiCalculatorState()
        val term = input.term.toIntOrNull()?.let { if (input.isTermInYears) it * 12 else it }
            ?: return EmiCalculatorState()

        // Calculate Fixed Charges
        val totalFixedCharges = fixedCharges.sumOf {
            if (it.isPercentage) (it.amount / 100) * principal else it.amount
        }

        // Total Loan Amount (Principal + Fixed Charges)
        val totalLoanAmount = principal + totalFixedCharges

        if (nominalRate == 0.0) {
            val monthlyPayment = totalLoanAmount / term
            return EmiCalculatorState(
                mp = monthlyPayment,
                tp = totalLoanAmount,
                ti = 0.0,
                tc = totalFixedCharges,
                eir = 0.0
            )
        }

        // Standard EMI Calculation (Using Total Loan Amount)
        val emi = (totalLoanAmount * nominalRate * (1 + nominalRate).pow(term.toDouble())) /
                ((1 + nominalRate).pow(term.toDouble()) - 1)

        val totalPaid = emi * term
        val totalInterest = totalPaid - totalLoanAmount  // Interest paid on (Principal + Charges)

        val eir = calculateInterestRate(principal, term, emi) ?: Double.NaN
        return EmiCalculatorState(
            mp = emi,
            tp = totalPaid,
            ti = totalInterest,
            tc = totalFixedCharges,
            eir = eir
        )
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
