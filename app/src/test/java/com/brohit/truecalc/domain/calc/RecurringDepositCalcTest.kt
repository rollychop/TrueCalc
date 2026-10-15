package com.brohit.truecalc.domain.calc

import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.math.pow

class RecurringDepositCalcTest {

    /**
     * Independent reference: each installment j (1 = last, N = first) grows for j installment
     * periods, i.e. (1 + r/n)^(n * j / ppy).
     */
    private fun referenceMaturity(installment: Double, pct: Double, years: Double, ppy: Int, n: Int): Double {
        val count = (years * ppy).toInt()
        return (1..count).sumOf { j -> installment * (1 + pct / 100 / n).pow(n.toDouble() * j / ppy) }
    }

    @Test
    fun monthlyQuarterly_matchesInstallmentByInstallmentSum() {
        val result = RecurringDepositCalc.calculate(
            1_000.0, 7.5, 1.0, RecurringInterval.MONTHLY, CompoundingFrequency.Quarterly
        )
        assertEquals(referenceMaturity(1_000.0, 7.5, 1.0, 12, 4), result.amount, 1e-6)
        assertEquals(12_000.0, result.principal, 0.0)
        assertEquals(result.amount - 12_000.0, result.interest, 1e-9)
        // Sanity: about ₹12,4xx for ₹1,000/month at 7.5% for a year.
        assertEquals(12_498.0, result.amount, 5.0)
    }

    @Test
    fun monthsInput_isEquivalentToYears() {
        val months = RecurringDepositCalc.calculate(
            "5000", "6.8", "60", false, RecurringInterval.MONTHLY, CompoundingFrequency.Quarterly
        )
        val years = RecurringDepositCalc.calculate(
            "5000", "6.8", "5", true, RecurringInterval.MONTHLY, CompoundingFrequency.Quarterly
        )
        assertEquals(years, months)
        assertEquals(300_000.0, months.principal, 0.0)
        assertEquals(referenceMaturity(5_000.0, 6.8, 5.0, 12, 4), months.amount, 1e-6)
    }

    @Test
    fun yearlyInstallmentsYearlyCompounding() {
        val result = RecurringDepositCalc.calculate(
            10_000.0, 10.0, 3.0, RecurringInterval.YEARLY, CompoundingFrequency.Annually
        )
        // 10000 * (1.1 + 1.21 + 1.331)
        assertEquals(36_410.0, result.amount, 1e-6)
        assertEquals(30_000.0, result.principal, 0.0)
    }

    @Test
    fun totalInstallments_truncatesPartialPeriods() {
        // 1.5 years of yearly installments counts 1 installment invested, as in the original.
        val result = RecurringDepositCalc.calculate(
            1_000.0, 5.0, 1.5, RecurringInterval.YEARLY, CompoundingFrequency.Annually
        )
        assertEquals(1_000.0, result.principal, 0.0)
    }

    @Test
    fun blankInput_returnsEmpty() {
        assertEquals(
            InterestResult.Empty,
            RecurringDepositCalc.calculate("", "7", "1", true, RecurringInterval.MONTHLY, CompoundingFrequency.Quarterly)
        )
        assertEquals(
            InterestResult.Empty,
            RecurringDepositCalc.calculate("100", "x", "1", true, RecurringInterval.MONTHLY, CompoundingFrequency.Quarterly)
        )
    }
}
