package com.brohit.truecalc.domain.calc

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.pow

class EmiCalcTest {

    /** Independent textbook EMI: P r (1+r)^n / ((1+r)^n - 1). */
    private fun referenceEmi(p: Double, annualPct: Double, months: Int): Double {
        val r = annualPct / 1200
        val g = (1 + r).pow(months)
        return p * r * g / (g - 1)
    }

    @Test
    fun knownValue_tenLakhAt10PercentFor20Years() {
        val result = EmiCalc.calculate(1_000_000.0, 10.0, 240)
        assertEquals(9650.22, result.monthlyPayment, 0.005)
        assertEquals(referenceEmi(1_000_000.0, 10.0, 240), result.monthlyPayment, 1e-9)
        assertEquals(result.monthlyPayment * 240, result.totalPaid, 1e-6)
        assertEquals(result.totalPaid - 1_000_000.0, result.totalInterest, 1e-6)
        assertEquals(0.0, result.totalCharges, 0.0)
        // Without charges the effective rate equals the nominal rate.
        assertEquals(10.0, result.effectiveInterestRate, 1e-3)
    }

    @Test
    fun textEntryPoint_yearsAndMonthsAreEquivalent() {
        val years = EmiCalc.calculate("500000", "8.5", "2", isTermInYears = true)
        val months = EmiCalc.calculate("500000", "8.5", "24", isTermInYears = false)
        assertEquals(years, months)
        assertEquals(referenceEmi(500_000.0, 8.5, 24), months.monthlyPayment, 1e-9)
    }

    @Test
    fun fixedCharges_areAddedToTheLoanAndRaiseTheEffectiveRate() {
        val charges = listOf(EmiCharge(1.0, isPercentage = true), EmiCharge(500.0, isPercentage = false))
        val result = EmiCalc.calculate(100_000.0, 12.0, 12, charges)
        assertEquals(1_500.0, result.totalCharges, 1e-9)
        assertEquals(referenceEmi(101_500.0, 12.0, 12), result.monthlyPayment, 1e-9)
        assertEquals(result.totalPaid - 101_500.0, result.totalInterest, 1e-6)
        assertTrue(result.effectiveInterestRate > 12.0)
        // The effective rate must make the installment repay exactly the principal.
        val r = result.effectiveInterestRate / 1200
        val pv = result.monthlyPayment * (1 - (1 + r).pow(-12)) / r
        assertEquals(100_000.0, pv, 0.01)
    }

    @Test
    fun zeroRate_splitsLoanEvenly() {
        val result = EmiCalc.calculate(120_000.0, 0.0, 12, listOf(EmiCharge(1_200.0, false)))
        assertEquals(10_100.0, result.monthlyPayment, 1e-9)
        assertEquals(121_200.0, result.totalPaid, 1e-9)
        assertEquals(0.0, result.totalInterest, 0.0)
        assertEquals(1_200.0, result.totalCharges, 0.0)
        assertEquals(0.0, result.effectiveInterestRate, 0.0)
    }

    @Test
    fun blankOrInvalidInput_returnsEmpty() {
        assertEquals(EmiResult.Empty, EmiCalc.calculate("", "10", "20", true))
        assertEquals(EmiResult.Empty, EmiCalc.calculate("100000", "", "20", true))
        assertEquals(EmiResult.Empty, EmiCalc.calculate("100000", "10", "", true))
        // Term is parsed as Int, like the original ViewModel.
        assertEquals(EmiResult.Empty, EmiCalc.calculate("100000", "10", "1.5", true))
        assertEquals(EmiResult.Empty, EmiCalc.calculate("abc", "10", "20", true))
    }

    @Test
    fun zeroTerm_isNotGuarded_likeTheOriginal() {
        // The ViewModel never guarded a 0 term; callers only show/log when monthlyPayment > 0.
        assertTrue(EmiCalc.calculate(100_000.0, 10.0, 0).monthlyPayment.isInfinite())
        assertTrue(EmiCalc.calculate(100_000.0, 0.0, 0).monthlyPayment.isInfinite())
    }

    @Test
    fun zeroPrincipal_givesZeroPaymentAndUnsolvableEffectiveRate() {
        val result = EmiCalc.calculate(0.0, 10.0, 12)
        assertEquals(0.0, result.monthlyPayment, 0.0)
        assertTrue(result.effectiveInterestRate.isNaN())
    }
}
