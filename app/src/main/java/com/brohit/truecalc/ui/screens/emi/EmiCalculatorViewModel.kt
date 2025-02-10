package com.brohit.truecalc.ui.screens.emi

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale
import javax.inject.Inject
import kotlin.math.pow

class EmiCalculatorInputState {
    val principal =
        TextFieldState(validator = {
            it.isDigitsOnly() && it.isNotEmpty() && it.length <= 15
        }, maxChar = 15)
    val interestRate = TextFieldState(
        validator = {
            it.isDigitsOnly() && it.isNotEmpty() && it.length <= 4
        },
        maxChar = 4
    )
    var isTermInYears by mutableStateOf(true)
    val term = TextFieldState(
        validator = {
            it.isDigitsOnly() && it.isNotEmpty() && it.length <= 3
        },
        maxChar = 3
    )

}

data class EmiCalculatorState(
    val monthlyPayment: String = "₹0.00",
    val totalPaid: String = "₹0.00",
    val totalInterest: String = "₹0.00",
    val totalCharges: String = "₹0.00"
)

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

    private val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    val inputState = EmiCalculatorInputState()

    val allFixedCharges: StateFlow<List<FixedCharge>> = repository.allFixedCharges
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
            repository.allFixedCharges
        ) { r, p, t, i, fc ->
            EmiInput(
                principal = p,
                rate = r,
                term = t,
                isTermInYears = i,
                fc
            )
        }
            .debounce(300)
            .mapLatest { emiInput ->
                calculateEmi(emiInput)
            }
            .flowOn(Dispatchers.Default)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), EmiCalculatorState())

    private fun calculateEmi(input: EmiInput): EmiCalculatorState {
        val fixedCharges = input.fixedCharges

        val principal = input.principal.toDoubleOrNull() ?: return EmiCalculatorState()
        val rate = input.rate.toDoubleOrNull()?.div(12)?.div(100) ?: return EmiCalculatorState()
        val term = input.term.toIntOrNull()?.let { if (input.isTermInYears) it * 12 else it }
            ?: return EmiCalculatorState()

        val totalFixedCharges = fixedCharges.sumOf {
            if (it.isPercentage) (it.amount / 100) * principal else it.amount
        }
        val totalLoanAmount = principal + totalFixedCharges

        if (rate == 0.0) {
            val monthlyPayment = totalLoanAmount / term
            return EmiCalculatorState(
                monthlyPayment = monthlyPayment.toFixed2(),
                totalPaid = totalLoanAmount.toFixed2(),
                totalInterest = "₹0.00",
                totalCharges = totalFixedCharges.toFixed2()
            )
        }

        // EMI Formula
        val emi = (totalLoanAmount * rate * (1 + rate).pow(term.toDouble())) /
                ((1 + rate).pow(term.toDouble()) - 1)

        val totalPaid = emi * term
        val totalInterest = totalPaid - totalLoanAmount

        return EmiCalculatorState(
            monthlyPayment = emi.toFixed2(),
            totalPaid = totalPaid.toFixed2(),
            totalInterest = totalInterest.toFixed2(),
            totalCharges = totalFixedCharges.toFixed2()
        )
    }


    private fun Double.toFixed2(): String {
        if (isNaN()) return "-"
        if (isInfinite()) return "∞"
        return formatter.format(this)
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
