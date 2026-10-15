package com.brohit.truecalc.domain.calc

import com.brohit.truecalc.core.MathUtils
import kotlin.math.pow

/** A fixed loan charge (processing fee, insurance…). Percentage charges are a % of the principal. */
data class EmiCharge(val amount: Double, val isPercentage: Boolean)

data class EmiResult(
    /** Monthly installment. */
    val monthlyPayment: Double = 0.0,
    /** Sum of all installments. */
    val totalPaid: Double = 0.0,
    /** Interest paid on (principal + charges). */
    val totalInterest: Double = 0.0,
    /** Sum of the fixed charges added to the loan. */
    val totalCharges: Double = 0.0,
    /** Annual % rate that makes the installment repay only the principal; NaN if it can't be solved. */
    val effectiveInterestRate: Double = 0.0,
) {
    companion object {
        val Empty = EmiResult()
    }
}

/**
 * EMI (equated monthly installment) maths, extracted verbatim from `EmiCalculatorViewModel`.
 *
 * Charges are added to the loan amount, the EMI is computed on (principal + charges), and the
 * effective interest rate is the rate at which that EMI would repay just the principal.
 */
object EmiCalc {

    /**
     * @param principal loan principal
     * @param annualRatePercent nominal annual rate in percent (e.g. 10.0 for 10%)
     * @param termMonths tenure in months
     *
     * No input guards beyond the original: a 0 term yields an infinite/NaN installment, exactly
     * like the ViewModel did (callers only log/show results when `monthlyPayment > 0`).
     */
    fun calculate(
        principal: Double,
        annualRatePercent: Double,
        termMonths: Int,
        charges: List<EmiCharge> = emptyList(),
    ): EmiResult {
        val nominalRate = annualRatePercent / 12 / 100

        val totalFixedCharges = charges.sumOf {
            if (it.isPercentage) (it.amount / 100) * principal else it.amount
        }
        val totalLoanAmount = principal + totalFixedCharges

        if (nominalRate == 0.0) {
            return EmiResult(
                monthlyPayment = totalLoanAmount / termMonths,
                totalPaid = totalLoanAmount,
                totalInterest = 0.0,
                totalCharges = totalFixedCharges,
                effectiveInterestRate = 0.0,
            )
        }

        val growth = (1 + nominalRate).pow(termMonths.toDouble())
        val emi = (totalLoanAmount * nominalRate * growth) / (growth - 1)
        val totalPaid = emi * termMonths
        val totalInterest = totalPaid - totalLoanAmount
        val eir = MathUtils.calculateInterestRate(principal, termMonths, emi) ?: Double.NaN

        return EmiResult(
            monthlyPayment = emi,
            totalPaid = totalPaid,
            totalInterest = totalInterest,
            totalCharges = totalFixedCharges,
            effectiveInterestRate = eir,
        )
    }

    /**
     * Text-field entry point matching the ViewModel's parsing: returns [EmiResult.Empty] when any
     * field doesn't parse (principal/rate as Double, term as Int). `term` is years when
     * [isTermInYears], else months.
     */
    fun calculate(
        principal: String,
        rate: String,
        term: String,
        isTermInYears: Boolean,
        charges: List<EmiCharge> = emptyList(),
    ): EmiResult {
        val p = principal.toDoubleOrNull() ?: return EmiResult.Empty
        val r = rate.toDoubleOrNull() ?: return EmiResult.Empty
        val months = term.toIntOrNull()?.let { if (isTermInYears) it * 12 else it }
            ?: return EmiResult.Empty
        return calculate(p, r, months, charges)
    }
}
