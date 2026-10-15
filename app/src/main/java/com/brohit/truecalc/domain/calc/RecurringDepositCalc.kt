package com.brohit.truecalc.domain.calc

import kotlin.math.pow

/**
 * Recurring deposit maths, extracted verbatim from `RecurringDepositViewModel`.
 *
 * Maturity = I × ((1 + r/n)^(n t) − 1) / (1 − (1 + r/n)^(−n/ppy)), where I is the installment,
 * n the compounding (payout) frequency and ppy the installments per year.
 * `principal` in the result is the total invested: I × floor(t × ppy).
 */
object RecurringDepositCalc {

    fun calculate(
        installment: Double,
        annualRatePercent: Double,
        years: Double,
        interval: RecurringInterval,
        frequency: CompoundingFrequency,
    ): InterestResult {
        val rate = annualRatePercent / 100
        val n = frequency.timesPerYear
        val ppy = interval.paymentsPerYear

        val totalInstallments = (years * ppy).toInt()
        val r = rate / n
        val onePlusR = 1 + r
        val factor = onePlusR.pow(n * years)
        val top = factor - 1
        val bottom = onePlusR.pow(-1.0 * n / ppy)

        val maturityAmount = installment * (top / (1 - bottom))
        val totalInvested = installment * totalInstallments
        return InterestResult(
            amount = maturityAmount,
            interest = maturityAmount - totalInvested,
            principal = totalInvested,
        )
    }

    /** Text-field entry point; [InterestResult.Empty] if any field doesn't parse. */
    fun calculate(
        installment: String,
        rate: String,
        time: String,
        isTimeInYears: Boolean,
        interval: RecurringInterval,
        frequency: CompoundingFrequency,
    ): InterestResult {
        val p = installment.toDoubleOrNull() ?: return InterestResult.Empty
        val r = rate.toDoubleOrNull() ?: return InterestResult.Empty
        val t = time.toDoubleOrNull() ?: return InterestResult.Empty
        return calculate(p, r, InterestCalc.toYears(t, isTimeInYears), interval, frequency)
    }
}
