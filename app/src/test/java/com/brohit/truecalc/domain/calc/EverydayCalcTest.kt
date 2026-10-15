package com.brohit.truecalc.domain.calc

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/** Percentage, discount, GST and split-bill maths. */
class EverydayCalcTest {

    private val eps = 1e-9

    // ---- Percentage -----------------------------------------------------------------------

    @Test
    fun percentOf() {
        assertEquals(360.0, PercentageCalc.percentOf(15.0, 2400.0), eps)
        assertEquals(0.0, PercentageCalc.percentOf(0.0, 500.0), eps)
        assertEquals(750.0, PercentageCalc.percentOf(150.0, 500.0), eps)
    }

    @Test
    fun whatPercent() {
        assertEquals(25.0, PercentageCalc.whatPercent(50.0, 200.0)!!, eps)
        assertEquals(200.0, PercentageCalc.whatPercent(400.0, 200.0)!!, eps)
        assertNull(PercentageCalc.whatPercent(5.0, 0.0))
    }

    @Test
    fun percentChange_signed() {
        assertEquals(25.0, PercentageCalc.percentChange(80.0, 100.0)!!, eps)
        assertEquals(-20.0, PercentageCalc.percentChange(100.0, 80.0)!!, eps)
        assertEquals(0.0, PercentageCalc.percentChange(50.0, 50.0)!!, eps)
        // Negative start: moving from -50 to -25 is an increase.
        assertEquals(50.0, PercentageCalc.percentChange(-50.0, -25.0)!!, eps)
        assertNull(PercentageCalc.percentChange(0.0, 10.0))
    }

    @Test
    fun applyChange() {
        assertEquals(1180.0, PercentageCalc.applyChange(1000.0, 18.0, ChangeDirection.Increase), eps)
        assertEquals(800.0, PercentageCalc.applyChange(1000.0, 20.0, ChangeDirection.Decrease), eps)
        assertEquals(0.0, PercentageCalc.applyChange(1000.0, 100.0, ChangeDirection.Decrease), eps)
    }

    @Test
    fun percentageModeKeys_roundTrip() {
        PercentageMode.entries.forEach { assertEquals(it, PercentageMode.fromKey(it.key)) }
        assertEquals(PercentageMode.PercentOf, PercentageMode.fromKey(null))
        assertEquals(PercentageMode.PercentOf, PercentageMode.fromKey("bogus"))
        assertEquals(ChangeDirection.Decrease, ChangeDirection.fromKey("decrease"))
    }

    // ---- Discount -------------------------------------------------------------------------

    @Test
    fun discount_single() {
        val r = DiscountCalc.calculate(1000.0, 20.0)!!
        assertEquals(800.0, r.finalPrice, eps)
        assertEquals(200.0, r.saved, eps)
        assertEquals(20.0, r.effectivePercent, eps)
        assertEquals(0.0, r.secondDiscountAmount, eps)
    }

    @Test
    fun discount_stackedAndFlat() {
        val r = DiscountCalc.calculate(1000.0, 20.0, 10.0, 50.0)!!
        assertEquals(670.0, r.finalPrice, eps)
        assertEquals(330.0, r.saved, eps)
        assertEquals(33.0, r.effectivePercent, eps)
        assertEquals(200.0, r.firstDiscountAmount, eps)
        assertEquals(80.0, r.secondDiscountAmount, eps)
        assertEquals(50.0, r.flatAmount, eps)
    }

    @Test
    fun discount_stackedIsNotAdditive() {
        // 50% + 50% stacked is 75% off, not 100%.
        val r = DiscountCalc.calculate(400.0, 50.0, 50.0)!!
        assertEquals(100.0, r.finalPrice, eps)
        assertEquals(75.0, r.effectivePercent, eps)
    }

    @Test
    fun discount_flatOffCappedAtPrice() {
        val r = DiscountCalc.calculate(100.0, 10.0, 0.0, 500.0)!!
        assertEquals(0.0, r.finalPrice, eps)
        assertEquals(90.0, r.flatAmount, eps)
        assertEquals(100.0, r.effectivePercent, eps)
    }

    @Test
    fun discount_invalid() {
        assertNull(DiscountCalc.calculate(0.0, 10.0))
        assertNull(DiscountCalc.calculate(100.0, 120.0))
        assertNull(DiscountCalc.calculate(100.0, -5.0))
        assertNull(DiscountCalc.calculate(100.0, 10.0, 101.0))
        assertNull(DiscountCalc.calculate(100.0, 10.0, 0.0, -1.0))
        assertNull(DiscountCalc.calculate("", "10"))
    }

    @Test
    fun discount_textEntry_blankOptionalsAreZero() {
        val r = DiscountCalc.calculate("1,000", "20", "", "")!!
        assertEquals(800.0, r.finalPrice, eps)
        assertNull(DiscountCalc.calculate("1000", "20", "abc", ""))
    }

    // ---- GST ------------------------------------------------------------------------------

    @Test
    fun gst_add() {
        val r = GstCalc.calculate(1000.0, 18.0, GstMode.Add)!!
        assertEquals(1000.0, r.net, eps)
        assertEquals(180.0, r.gst, eps)
        assertEquals(1180.0, r.gross, eps)
        assertEquals(90.0, r.cgst, eps)
        assertEquals(90.0, r.sgst, eps)
    }

    @Test
    fun gst_remove() {
        val r = GstCalc.calculate(1180.0, 18.0, GstMode.Remove)!!
        assertEquals(1000.0, r.net, 1e-6)
        assertEquals(180.0, r.gst, 1e-6)
        assertEquals(1180.0, r.gross, 1e-6)
        assertEquals(90.0, r.cgst, 1e-6)
    }

    @Test
    fun gst_addThenRemove_roundTrips() {
        for (rate in GstCalc.PRESET_RATES + 0.0) {
            val gross = GstCalc.calculate(2_499.0, rate, GstMode.Add)!!.gross
            assertEquals(2_499.0, GstCalc.calculate(gross, rate, GstMode.Remove)!!.net, 1e-6)
        }
    }

    @Test
    fun gst_presetsFollowGst2() {
        assertEquals(listOf(0.0, 0.25, 3.0, 5.0, 18.0, 40.0), GstCalc.PRESET_RATES)
        assertEquals(18.0, GstCalc.DEFAULT_RATE, 0.0)
        val luxury = GstCalc.calculate(1000.0, 40.0, GstMode.Add)!!
        assertEquals(1400.0, luxury.gross, eps)
        assertEquals(200.0, luxury.cgst, eps)
        assertEquals(0.0, GstCalc.calculate(1000.0, 0.0, GstMode.Remove)!!.gst, eps)
    }

    @Test
    fun gst_fractionalRate() {
        val r = GstCalc.calculate(100_000.0, 0.25, GstMode.Add)!!
        assertEquals(250.0, r.gst, eps)
        assertEquals(125.0, r.cgst, eps)
    }

    @Test
    fun gst_invalid() {
        assertNull(GstCalc.calculate(-1.0, 18.0, GstMode.Add))
        assertNull(GstCalc.calculate(100.0, 150.0, GstMode.Add))
        assertNotNull(GstCalc.calculate(0.0, 18.0, GstMode.Add))
        assertEquals(GstMode.Remove, GstMode.fromKey("remove"))
        assertEquals(GstMode.Add, GstMode.fromKey(null))
    }

    // ---- Split bill -----------------------------------------------------------------------

    @Test
    fun split_withTip() {
        val r = SplitBillCalc.calculate(2000.0, 4, 10.0)!!
        assertEquals(200.0, r.tip, eps)
        assertEquals(2200.0, r.total, eps)
        assertEquals(550.0, r.perPerson, eps)
        assertEquals(50.0, r.tipPerPerson, eps)
        assertEquals(0.0, r.roundingExtra, eps)
    }

    @Test
    fun split_roundUp() {
        val r = SplitBillCalc.calculate(1000.0, 3, 0.0, roundUp = true)!!
        assertEquals(334.0, r.perPerson, eps)
        assertEquals(1002.0, r.collected, eps)
        assertEquals(2.0, r.roundingExtra, 1e-6)
        val exact = SplitBillCalc.calculate(1000.0, 3, 0.0)!!
        assertEquals(333.3333333, exact.perPerson, 1e-6)
    }

    @Test
    fun split_roundUp_exactShareStays() {
        // 0.1-style floating error must not push 110 up to 111.
        val r = SplitBillCalc.calculate(100.0, 1, 10.0, roundUp = true)!!
        assertEquals(110.0, r.perPerson, eps)
        assertEquals(0.0, r.roundingExtra, 1e-6)
        assertEquals(0.0, SplitBillCalc.calculate(0.0, 2, 10.0, roundUp = true)!!.perPerson, eps)
    }

    @Test
    fun split_invalid() {
        assertNull(SplitBillCalc.calculate(100.0, 0, 10.0))
        assertNull(SplitBillCalc.calculate(-1.0, 2, 10.0))
        assertNull(SplitBillCalc.calculate(100.0, 2, -5.0))
    }

    // ---- Formatting -----------------------------------------------------------------------

    @Test
    fun calcFormat_number() {
        assertEquals("12,34,567.89", CalcFormat.number(1234567.891))
        assertEquals("360", CalcFormat.number(360.0))
        assertEquals("0.13", CalcFormat.number(0.125))
        assertEquals("-1,000.5", CalcFormat.number(-1000.5))
        assertEquals("1000", CalcFormat.number(1000.0, grouping = false))
        assertEquals("0", CalcFormat.number(-0.001))
        assertEquals("-", CalcFormat.number(Double.NaN))
    }

    @Test
    fun calcFormat_groupIndian() {
        assertEquals("999", CalcFormat.groupIndian("999"))
        assertEquals("1,000", CalcFormat.groupIndian("1000"))
        assertEquals("1,00,000", CalcFormat.groupIndian("100000"))
        assertEquals("1,00,00,000.25", CalcFormat.groupIndian("10000000.25"))
        assertEquals("-12,345", CalcFormat.groupIndian("-12345"))
    }

    @Test
    fun calcFormat_significant() {
        assertEquals("3.1415927", CalcFormat.significant(Math.PI, digits = 8))
        assertEquals("1.5E12", CalcFormat.significant(1.5e12))
        assertEquals("1E-7", CalcFormat.significant(1e-7))
        assertEquals("0.000001", CalcFormat.significant(1e-6))
        assertEquals("0", CalcFormat.significant(0.0))
    }
}
