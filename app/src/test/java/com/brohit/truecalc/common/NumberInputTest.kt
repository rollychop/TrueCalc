package com.brohit.truecalc.common

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NumberInputTest {

    // ---- filter ---------------------------------------------------------------------------

    @Test
    fun filter_digitsAndOneDecimalPoint() {
        assertEquals("1234.5", NumberInput.filter("1,234.5".replace(",", ""), allowDecimal = true))
        assertEquals("12.5", NumberInput.filter("12,5", allowDecimal = true)) // locale comma → '.'
        assertEquals("125", NumberInput.filter("12.5", allowDecimal = false))
        assertNull(NumberInput.filter("1.2.3", allowDecimal = true))
        assertEquals("12", NumberInput.filter("1a2 ", allowDecimal = true))
        assertEquals("", NumberInput.filter("", allowDecimal = true))
    }

    @Test
    fun filter_maxDecimals() {
        assertEquals("10.25", NumberInput.filter("10.25", allowDecimal = true, maxDecimals = 2))
        assertEquals("10.", NumberInput.filter("10.", allowDecimal = true, maxDecimals = 2))
        assertNull(NumberInput.filter("10.255", allowDecimal = true, maxDecimals = 2))
    }

    @Test
    fun filter_negative() {
        assertEquals("-40", NumberInput.filter("-40", allowDecimal = true, allowNegative = true))
        assertEquals("-40", NumberInput.filter("−40", allowDecimal = true, allowNegative = true))
        assertEquals("40", NumberInput.filter("-40", allowDecimal = true, allowNegative = false))
        assertEquals("410", NumberInput.filter("4-10", allowDecimal = true, allowNegative = true))
        assertEquals("-", NumberInput.filter("-", allowDecimal = true, allowNegative = true))
    }

    // ---- sign toggle ----------------------------------------------------------------------

    @Test
    fun toggleSign() {
        assertEquals("-5", NumberInput.toggleSign("5"))
        assertEquals("5", NumberInput.toggleSign("-5"))
        assertEquals("-12.5", NumberInput.toggleSign("12.5"))
        assertEquals("-", NumberInput.toggleSign(""))
        assertEquals("", NumberInput.toggleSign("-"))
        assertEquals("7", NumberInput.toggleSign(NumberInput.toggleSign("7")))
    }

    // ---- grouping + cursor mapping --------------------------------------------------------

    private fun text(raw: String, prefix: String = "₹ ") = NumberInput.grouped(raw, prefix).text

    @Test
    fun grouped_indianGroupingWithDecimals() {
        assertEquals("₹ 5", text("5"))
        assertEquals("₹ 999", text("999"))
        assertEquals("₹ 1,000", text("1000"))
        assertEquals("₹ 1,00,000", text("100000"))
        assertEquals("₹ 12,34,567.89", text("1234567.89"))
        assertEquals("₹ 1,000.5", text("1000.5"))
        assertEquals("₹ 1,000.", text("1000."))
        assertEquals("₹ .5", text(".5"))
        assertEquals("-12,345", text("-12345", prefix = ""))
    }

    @Test
    fun grouped_emptyStaysEmpty() {
        val g = NumberInput.grouped("", "₹ ")
        assertEquals("", g.text)
        assertEquals(0, g.originalToTransformed(0))
        assertEquals(0, g.transformedToOriginal(0))
    }

    @Test
    fun grouped_cursorMapping_endAndStart() {
        val g = NumberInput.grouped("1234567.89", "₹ ")
        // "₹ 12,34,567.89"
        assertEquals(g.text.length, g.originalToTransformed(10))
        assertEquals(10, g.transformedToOriginal(g.text.length))
        assertEquals(2, g.originalToTransformed(0)) // before '1', after the prefix
        assertEquals(0, g.transformedToOriginal(0)) // inside the prefix → start
        assertEquals(0, g.transformedToOriginal(1))
    }

    @Test
    fun grouped_cursorMapping_middle() {
        val g = NumberInput.grouped("1234567.89", "₹ ")
        // raw:  1 2 3 4 5 6 7 . 8 9
        // text: ₹ ␠ 1 2 , 3 4 , 5 6 7 . 8 9
        assertEquals(3, g.originalToTransformed(1)) // after '1'
        assertEquals(5, g.originalToTransformed(2)) // before '3' (after the comma)
        assertEquals(8, g.originalToTransformed(4)) // before '5'
        assertEquals(11, g.originalToTransformed(7)) // before '.'
        assertEquals(12, g.originalToTransformed(8)) // before '8'
        // A cursor right before a comma maps to the digit boundary.
        assertEquals(2, g.transformedToOriginal(4)) // between '2' and ','
        assertEquals(2, g.transformedToOriginal(5)) // between ',' and '3'
        assertEquals(8, g.transformedToOriginal(12))
    }

    @Test
    fun grouped_mappingIsConsistentForEveryOffset() {
        for (raw in listOf("1", "12", "1234", "1234567", "123456789.05", "100000.", "-98765")) {
            val g = NumberInput.grouped(raw, "₹ ")
            for (o in 0..raw.length) {
                val t = g.originalToTransformed(o)
                assertEquals("round trip $raw @$o", o, g.transformedToOriginal(t))
            }
            // Every transformed offset maps into the original text.
            for (t in 0..g.text.length) {
                val o = g.transformedToOriginal(t)
                assertEquals(true, o in 0..raw.length)
            }
            // Removing the separators and prefix gives the raw text back.
            assertEquals(raw, g.text.removePrefix("₹ ").replace(",", ""))
        }
    }

    @Test
    fun grouped_deletionShiftsGroups() {
        // Deleting the '4' in "1234567" (cursor after it) leaves "123567": the field re-groups
        // and the cursor (raw offset 3) lands just before the '5', past the separator.
        val before = NumberInput.grouped("1234567", "")
        assertEquals("12,34,567", before.text)
        val after = NumberInput.grouped("123567", "")
        assertEquals("1,23,567", after.text)
        assertEquals(5, after.originalToTransformed(3))
        assertEquals(3, after.transformedToOriginal(4))
        // Deleting back to empty.
        assertEquals("", NumberInput.grouped("", "₹ ").text)
    }
}
