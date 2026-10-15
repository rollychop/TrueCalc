package com.brohit.truecalc.domain.calc

import org.junit.Assert.assertEquals
import org.junit.Test

class CalculatorEditorTest {

    /** Presses [keys] in order from [start]. */
    private fun press(vararg keys: CalcKey, start: String = ""): String =
        keys.fold(start) { expr, key -> CalculatorEditor.apply(expr, key) }

    @Test
    fun digitsAndOperators() {
        assertEquals("12+3", press(CalcKey.One, CalcKey.Two, CalcKey.Add, CalcKey.Three))
        assertEquals("7×8÷2", press(CalcKey.Seven, CalcKey.Multiply, CalcKey.Eight, CalcKey.Divide, CalcKey.Two))
    }

    @Test
    fun leadingZeroIsReplaced() {
        assertEquals("5", press(CalcKey.Zero, CalcKey.Five))
        assertEquals("1+5", press(CalcKey.One, CalcKey.Add, CalcKey.Zero, CalcKey.Five))
        assertEquals("0.5", press(CalcKey.Zero, CalcKey.Dot, CalcKey.Five))
        assertEquals("10", press(CalcKey.One, CalcKey.Zero))
    }

    @Test
    fun dot_onlyOncePerNumber_andGetsLeadingZero() {
        assertEquals("0.", press(CalcKey.Dot))
        assertEquals("1.5", press(CalcKey.One, CalcKey.Dot, CalcKey.Dot, CalcKey.Five, CalcKey.Dot))
        assertEquals("2+0.", press(CalcKey.Two, CalcKey.Add, CalcKey.Dot))
    }

    @Test
    fun operatorReplacesTrailingOperator() {
        assertEquals("5×", press(CalcKey.Five, CalcKey.Add, CalcKey.Multiply))
        assertEquals("5+", press(CalcKey.Five, CalcKey.Multiply, CalcKey.Subtract, CalcKey.Add))
    }

    @Test
    fun minusAfterMultiplyMakesNegativeOperand() {
        assertEquals("5×−3", press(CalcKey.Five, CalcKey.Multiply, CalcKey.Subtract, CalcKey.Three))
        assertEquals("−", press(CalcKey.Subtract))
        assertEquals("", press(CalcKey.Multiply))
        assertEquals("(−", press(CalcKey.OpenParen, CalcKey.Subtract))
        assertEquals("(", press(CalcKey.OpenParen, CalcKey.Add))
    }

    @Test
    fun smartParentheses() {
        assertEquals("(2+3)", press(CalcKey.Parens, CalcKey.Two, CalcKey.Add, CalcKey.Three, CalcKey.Parens))
        assertEquals("2×(", press(CalcKey.Two, CalcKey.Parens))
        assertEquals("(2+(", press(CalcKey.Parens, CalcKey.Two, CalcKey.Add, CalcKey.Parens))
        assertEquals(")", press(CalcKey.CloseParen, start = "(2+3").takeLast(1))
        assertEquals("2", press(CalcKey.CloseParen, start = "2")) // nothing to close
    }

    @Test
    fun implicitMultiplicationInserted() {
        assertEquals("2×π", press(CalcKey.Two, CalcKey.Pi))
        assertEquals("π×2", press(CalcKey.Pi, CalcKey.Two))
        assertEquals("(1)×2", press(CalcKey.Two, start = "(1)"))
        assertEquals("3×sin(", press(CalcKey.Three, CalcKey.Sin))
        assertEquals("3×√", press(CalcKey.Three, CalcKey.Sqrt))
    }

    @Test
    fun postfixNeedsAnOperand() {
        assertEquals("", press(CalcKey.Percent))
        assertEquals("5%", press(CalcKey.Five, CalcKey.Percent))
        assertEquals("5+", press(CalcKey.Five, CalcKey.Add, CalcKey.Percent))
        assertEquals("3!", press(CalcKey.Three, CalcKey.Factorial))
        assertEquals("4²", press(CalcKey.Four, CalcKey.Square))
    }

    @Test
    fun functionsAndPower() {
        assertEquals("sin(30)", press(CalcKey.Sin, CalcKey.Three, CalcKey.Zero, CalcKey.Parens))
        assertEquals("2^3", press(CalcKey.Two, CalcKey.Power, CalcKey.Three))
        assertEquals("ln(e", press(CalcKey.Ln, CalcKey.E))
    }

    @Test
    fun backspace_removesCharOrWholeFunction() {
        assertEquals("12", press(CalcKey.Backspace, start = "123"))
        assertEquals("2+", press(CalcKey.Backspace, start = "2+sin("))
        assertEquals("", press(CalcKey.Backspace, start = "ln("))
        assertEquals("", press(CalcKey.Backspace))
    }

    @Test
    fun clear() {
        assertEquals("", press(CalcKey.Clear, start = "12+3"))
    }

    @Test
    fun afterEquals_digitStartsNew_operatorContinues() {
        assertEquals("7", CalculatorEditor.apply("42", CalcKey.Seven, justEvaluated = true))
        assertEquals("42+", CalculatorEditor.apply("42", CalcKey.Add, justEvaluated = true))
        assertEquals("42%", CalculatorEditor.apply("42", CalcKey.Percent, justEvaluated = true))
        assertEquals("sin(", CalculatorEditor.apply("42", CalcKey.Sin, justEvaluated = true))
    }

    @Test
    fun ans_insertsLastAnswer() {
        assertEquals("2+−1.5", CalculatorEditor.apply("2+", CalcKey.Ans, lastAnswer = "-1.5"))
        assertEquals("2×12", CalculatorEditor.apply("2", CalcKey.Ans, lastAnswer = "12"))
        assertEquals("2+", CalculatorEditor.apply("2+", CalcKey.Ans, lastAnswer = null))
    }

    @Test
    fun equalsAndAngleToggleDontEdit() {
        assertEquals("1+1", press(CalcKey.Equals, CalcKey.AngleToggle, start = "1+1"))
    }

    @Test
    fun lengthIsCapped() {
        val long = "1".repeat(CalculatorEditor.MAX_LENGTH)
        assertEquals(long, press(CalcKey.Two, start = long))
    }

    @Test
    fun editedExpressionsEvaluate() {
        val expr = press(
            CalcKey.Five, CalcKey.Zero, CalcKey.Add, CalcKey.One, CalcKey.Zero, CalcKey.Percent,
        )
        assertEquals("55", (ExpressionEvaluator.evaluate(expr) as EvalResult.Success).formatted())
        val sci = press(CalcKey.Two, CalcKey.Sin, CalcKey.Three, CalcKey.Zero)
        assertEquals("1", (ExpressionEvaluator.evaluate(sci) as EvalResult.Success).formatted())
    }

    @Test
    fun displayText_groupsIntegerParts() {
        assertEquals("12,34,567+0.12345", CalculatorEditor.displayText("1234567+0.12345"))
        assertEquals("1,000×2,500", CalculatorEditor.displayText("1000×2500"))
        assertEquals("1.5E12", CalculatorEditor.displayText("1.5E12"))
        assertEquals("1.2E−10000", CalculatorEditor.displayText("1.2E−10000"))
        assertEquals("sin(30)+1,00,000", CalculatorEditor.displayText("sin(30)+100000"))
        assertEquals("", CalculatorEditor.displayText(""))
    }

    @Test
    fun layouts_haveFixedColumnCounts() {
        CalculatorLayouts.basic.forEach { assertEquals(4, it.size) }
        CalculatorLayouts.scientific.forEach { assertEquals(5, it.size) }
        // Every basic key is also on the scientific pad.
        val sciKeys = CalculatorLayouts.scientific.flatten().toSet()
        CalculatorLayouts.basic.flatten().forEach { assertEquals(true, it in sciKeys) }
    }
}
