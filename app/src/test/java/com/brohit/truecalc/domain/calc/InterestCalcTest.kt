package com.brohit.truecalc.domain.calc

import org.junit.Assert.assertEquals
import org.junit.Test

class InterestCalcTest {

    @Test
    fun compound_annually_knownValue() {
        // 1,00,000 at 10% for 5 years compounded yearly = 1.1^5 * 1e5 = 1,61,051
        val result = InterestCalc.compound(100_000.0, 10.0, 5.0, CompoundingFrequency.Annually)
        assertEquals(161_051.0, result.amount, 1e-6)
        assertEquals(61_051.0, result.interest, 1e-6)
        assertEquals(100_000.0, result.principal, 0.0)
    }

    @Test
    fun compound_quarterly_knownValue() {
        // 1.025^4 = 1.103812890625
        val result = InterestCalc.compound(100_000.0, 10.0, 1.0, CompoundingFrequency.Quarterly)
        assertEquals(110_381.2890625, result.amount, 1e-6)
    }

    @Test
    fun compound_monthsAreConvertedToYears() {
        val months = InterestCalc.compound("100000", "10", "18", false, CompoundingFrequency.Monthly)
        val years = InterestCalc.compound("100000", "10", "1.5", true, CompoundingFrequency.Monthly)
        assertEquals(years.amount, months.amount, 1e-6)
        // Independent: monthly compounding for 18 months
        var expected = 100_000.0
        repeat(18) { expected *= 1 + 0.10 / 12 }
        assertEquals(expected, months.amount, 1e-6)
    }

    @Test
    fun simple_knownValues() {
        val result = InterestCalc.simple(100_000.0, 10.0, 5.0)
        assertEquals(50_000.0, result.interest, 1e-9)
        assertEquals(150_000.0, result.amount, 1e-9)
        val sixMonths = InterestCalc.simple("100000", "12", "6", isTimeInYears = false)
        assertEquals(6_000.0, sixMonths.interest, 1e-9)
    }

    @Test
    fun fixedDeposit_isCompoundAtPayoutFrequency() {
        val fd = InterestCalc.fixedDeposit("250000", "7", "3", true, CompoundingFrequency.Quarterly)
        val ci = InterestCalc.compound("250000", "7", "3", true, CompoundingFrequency.Quarterly)
        assertEquals(ci, fd)
    }

    @Test
    fun zeroRate_earnsNothing() {
        assertEquals(0.0, InterestCalc.compound(5_000.0, 0.0, 3.0, CompoundingFrequency.Daily).interest, 0.0)
        assertEquals(0.0, InterestCalc.simple(5_000.0, 0.0, 3.0).interest, 0.0)
    }

    @Test
    fun blankInput_returnsEmpty() {
        assertEquals(InterestResult.Empty, InterestCalc.compound("", "10", "5", true, CompoundingFrequency.Annually))
        assertEquals(InterestResult.Empty, InterestCalc.simple("1000", "", "5", true))
        assertEquals(InterestResult.Empty, InterestCalc.fixedDeposit("1000", "5", "", true, CompoundingFrequency.Annually))
    }
}
