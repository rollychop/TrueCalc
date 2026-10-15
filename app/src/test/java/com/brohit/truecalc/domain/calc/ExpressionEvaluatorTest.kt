package com.brohit.truecalc.domain.calc

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExpressionEvaluatorTest {

    private fun eval(expr: String, angle: AngleUnit = AngleUnit.Degrees): String {
        val result = ExpressionEvaluator.evaluate(expr, angle)
        assertTrue("Expected success for '$expr' but was $result", result is EvalResult.Success)
        return (result as EvalResult.Success).formatted()
    }

    private fun error(expr: String, angle: AngleUnit = AngleUnit.Degrees): EvalError {
        val result = ExpressionEvaluator.evaluate(expr, angle)
        assertTrue("Expected failure for '$expr' but was $result", result is EvalResult.Failure)
        return (result as EvalResult.Failure).error
    }

    // ---- basics & precedence --------------------------------------------------------------

    @Test
    fun basicOperators() {
        assertEquals("5", eval("2+3"))
        assertEquals("-1", eval("2−3"))
        assertEquals("6", eval("2×3"))
        assertEquals("2.5", eval("5÷2"))
        assertEquals("6", eval("2*3"))
        assertEquals("2.5", eval("5/2"))
        assertEquals("-1", eval("2-3"))
    }

    @Test
    fun precedence_multiplicationBeforeAddition() {
        assertEquals("14", eval("2+3×4"))
        assertEquals("10", eval("2×3+4"))
        assertEquals("7", eval("10−6÷2"))
        assertEquals("1", eval("8÷4÷2")) // left-associative
        assertEquals("3", eval("10−5−2")) // left-associative
    }

    @Test
    fun parentheses() {
        assertEquals("20", eval("(2+3)×4"))
        assertEquals("2", eval("((1+1))"))
        assertEquals("21", eval("(1+2)×(3+4)"))
        assertEquals("-5", eval("−(2+3)"))
    }

    @Test
    fun unclosedParentheses_areAutoClosed() {
        assertEquals("5", eval("(2+3"))
        assertEquals("20", eval("4×(2+3"))
        assertEquals("1", eval("sin(90"))
    }

    @Test
    fun implicitMultiplication() {
        assertEquals("6", eval("2(3)"))
        assertEquals("6", eval("(2)(3)"))
        assertEquals("6.283185307", eval("2π"))
        assertEquals("1", eval("2sin(30)"))
        assertEquals("6", eval("2√9"))
    }

    @Test
    fun unaryMinus() {
        assertEquals("-5", eval("−5"))
        assertEquals("5", eval("−−5"))
        assertEquals("-6", eval("2×−3"))
        assertEquals("-4", eval("−2^2")) // power binds tighter than unary minus
        assertEquals("4", eval("(−2)^2"))
        assertEquals("1", eval("−2+3"))
    }

    @Test
    fun decimals_useExactArithmetic() {
        assertEquals("0.3", eval("0.1+0.2"))
        assertEquals("0.1", eval("0.3−0.2"))
        assertEquals("0.07", eval("0.1×0.7"))
        assertEquals("1.1", eval("3.3÷3"))
        assertEquals("0.5", eval(".5"))
        assertEquals("5", eval("5."))
    }

    @Test
    fun groupingCommasAndSpacesIgnored() {
        assertEquals("1234568", eval("12,34,567 + 1"))
    }

    // ---- percent --------------------------------------------------------------------------

    @Test
    fun percent_afterAddOrSubtract_isPercentOfLeftSide() {
        assertEquals("55", eval("50+10%"))
        assertEquals("45", eval("50−10%"))
        assertEquals("150", eval("200−25%"))
        assertEquals("118", eval("100+18%"))
        // Applies to the whole left side.
        assertEquals("33", eval("10+20+10%"))
    }

    @Test
    fun percent_elsewhere_isDivideBy100() {
        assertEquals("0.1", eval("10%"))
        assertEquals("20", eval("200×10%"))
        assertEquals("2000", eval("200÷10%"))
        assertEquals("0.1", eval("(5+5)%"))
        assertEquals("50.2", eval("50+10%×2")) // the term 10%×2 is no longer a bare percent
    }

    // ---- scientific -----------------------------------------------------------------------

    @Test
    fun trigInDegrees() {
        assertEquals("0.5", eval("sin(30)"))
        assertEquals("1", eval("sin(90)"))
        assertEquals("0", eval("sin(180)"))
        assertEquals("-1", eval("sin(270)"))
        assertEquals("0.5", eval("cos(60)"))
        assertEquals("0", eval("cos(90)"))
        assertEquals("-1", eval("cos(180)"))
        assertEquals("1", eval("tan(45)"))
        assertEquals("0", eval("tan(180)"))
        assertEquals("-0.5", eval("sin(−30)"))
        assertEquals("0.5", eval("sin(390)"))
        assertEquals("0.8660254038", eval("sin(60)"))
    }

    @Test
    fun tanOf90Degrees_isMathError() {
        assertEquals(EvalError.Domain, error("tan(90)"))
        assertEquals(EvalError.Domain, error("tan(270)"))
        assertEquals(EvalError.Domain, error("tan(−90)"))
    }

    @Test
    fun trigInRadians() {
        assertEquals("0", eval("sin(π)", AngleUnit.Radians))
        assertEquals("1", eval("sin(π÷2)", AngleUnit.Radians))
        assertEquals("-1", eval("cos(π)", AngleUnit.Radians))
        assertEquals("0.8414709848", eval("sin(1)", AngleUnit.Radians))
    }

    @Test
    fun logsRootsPowers() {
        assertEquals("2", eval("log(100)"))
        assertEquals("1", eval("ln(e)"))
        assertEquals("3", eval("√9"))
        assertEquals("1.414213562", eval("√2"))
        assertEquals("4", eval("√(16)"))
        assertEquals("25", eval("5²"))
        assertEquals("1024", eval("2^10"))
        assertEquals("0.25", eval("2^−2"))
        assertEquals("512", eval("2^3^2")) // right-associative
        assertEquals("3", eval("9^0.5"))
        assertEquals("1", eval("7^0"))
    }

    @Test
    fun factorial() {
        assertEquals("120", eval("5!"))
        assertEquals("1", eval("0!"))
        assertEquals("3628800", eval("10!"))
        assertEquals("6", eval("3!"))
        assertEquals("9", eval("3!+3"))
        assertEquals(EvalError.Domain, error("2.5!"))
        assertEquals(EvalError.Domain, error("(−3)!"))
        assertEquals(EvalError.Overflow, error("1001!"))
    }

    @Test
    fun constants() {
        assertEquals("3.141592654", eval("π"))
        assertEquals("2.718281828", eval("e"))
        assertEquals("3.141592654", eval("pi"))
        assertEquals("3", eval("sqrt(9)"))
    }

    // ---- errors ---------------------------------------------------------------------------

    @Test
    fun divideByZero() {
        assertEquals(EvalError.DivideByZero, error("5÷0"))
        assertEquals(EvalError.DivideByZero, error("1÷(2−2)"))
        assertEquals(EvalError.DivideByZero, error("0^−1"))
    }

    @Test
    fun domainErrors() {
        assertEquals(EvalError.Domain, error("√(−4)"))
        assertEquals(EvalError.Domain, error("log(0)"))
        assertEquals(EvalError.Domain, error("ln(−1)"))
        assertEquals(EvalError.Domain, error("(−8)^0.5"))
    }

    @Test
    fun malformedInput() {
        assertEquals(EvalError.Malformed, error("2+"))
        assertEquals(EvalError.Malformed, error("×2"))
        assertEquals(EvalError.Malformed, error("2+×3"))
        assertEquals(EvalError.Malformed, error("()"))
        assertEquals(EvalError.Malformed, error("2)"))
        assertEquals(EvalError.Malformed, error("1.2.3"))
        assertEquals(EvalError.Malformed, error("2#3"))
        assertEquals(EvalError.Malformed, error("sin"))
        assertEquals(EvalError.Malformed, error("."))
        assertEquals(EvalError.Malformed, error("foo(2)"))
    }

    @Test
    fun overflow() {
        assertEquals(EvalError.Overflow, error("10^99999"))
        assertEquals(EvalError.Overflow, error("99^9999"))
    }

    @Test
    fun empty() {
        assertEquals(EvalResult.Empty, ExpressionEvaluator.evaluate(""))
        assertEquals(EvalResult.Empty, ExpressionEvaluator.evaluate("   "))
    }

    // ---- display formatting ---------------------------------------------------------------

    @Test
    fun formatting_tenSignificantDigits_noTrailingZeros() {
        assertEquals("0.3333333333", eval("1÷3"))
        assertEquals("0.6666666667", eval("2÷3"))
        assertEquals("2", eval("4÷2"))
        assertEquals("1.5", eval("3÷2"))
        assertEquals("1234567890", eval("1234567890"))
        assertEquals("1.234567891E10", eval("12345678912"))
        assertEquals("1E-8", eval("1÷100000000"))
        assertEquals("0.0000001", eval("1÷10000000"))
        assertEquals("1E100", eval("10^100"))
    }

    @Test
    fun formatting_hidesRoundingNoise() {
        assertEquals("1", eval("1÷3×3"))
        assertEquals("1", eval("1/3*3"))
        assertEquals("2", eval("√2²"))
        assertEquals("2", eval("(√2)²"))
        assertEquals("2", eval("√2×√2"))
        assertEquals("1", eval("(2÷3)×1.5"))
    }

    @Test
    fun formatting_withGrouping() {
        val result = ExpressionEvaluator.evaluate("1234567.5") as EvalResult.Success
        assertEquals("12,34,567.5", result.formatted(grouping = true))
    }

    @Test
    fun exponentNotation_canBeReEvaluated() {
        assertEquals("1.5E12", eval("1.5E12"))
        assertEquals("3E12", eval("1.5E12×2"))
        assertEquals("1.2E-8", eval("1.2E−8"))
        assertEquals("0.00000012", eval("1.2E−7"))
    }
}
