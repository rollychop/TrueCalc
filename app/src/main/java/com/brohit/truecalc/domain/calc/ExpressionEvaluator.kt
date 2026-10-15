package com.brohit.truecalc.domain.calc

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

enum class AngleUnit(val label: String) { Degrees("DEG"), Radians("RAD") }

/** Why an expression could not be evaluated, with a message for the display. */
enum class EvalError(val message: String) {
    Malformed("Invalid expression"),
    DivideByZero("Can't divide by zero"),
    Domain("Math error"),
    Overflow("Number too large"),
}

sealed interface EvalResult {
    data class Success(val value: BigDecimal) : EvalResult {
        /** The value for display: up to 10 significant digits, no trailing zeros. */
        fun formatted(grouping: Boolean = false): String = ExpressionEvaluator.format(value, grouping)
    }

    data class Failure(val error: EvalError) : EvalResult
    data object Empty : EvalResult
}

/**
 * Recursive-descent evaluator for calculator expressions (no scripting engine).
 *
 * Grammar, lowest to highest precedence:
 * ```
 * expression := term (('+' | '−') term)*
 * term       := unary (('×' | '÷') unary | <implicit ×> unary)*
 * unary      := ('−' | '+') unary | power
 * power      := postfix ('^' unary)?            // right-associative: 2^3^2 = 2^9
 * postfix    := primary ('!' | '²' | '%')*
 * primary    := number | 'π' | 'e' | '(' expression ')' | function primary-ish | '√' postfix
 * ```
 * So `−2^2 = −4`, `2^−1 = 0.5`, `2π` and `2(3)` multiply, and unclosed parentheses are closed at
 * the end (like phone calculators) so a live preview works while typing.
 *
 * **Percent** follows phone calculators: after `+` or `−`, `X%` is X percent *of the left side*
 * (`50+10% = 55`, `200−25% = 150`); anywhere else `X%` is X/100 (`200×10% = 20`, `10% = 0.1`).
 *
 * + − × ÷ and integer powers use [BigDecimal] (34 significant digits), so `0.1+0.2 = 0.3`.
 * Trig, logs, roots and fractional powers use Double, rounded to 15 significant digits so
 * `sin(30°)` is exactly 0.5. In degrees, sin/cos/tan of exact multiples of 90° are exact and
 * `tan(90°)` is a [EvalError.Domain] error.
 *
 * Accepted symbols: digits, `.`, `+ - − * × / ÷ % ^ ! ² √ ( ) π e`, `sin cos tan log ln sqrt pi`,
 * grouping commas and spaces (ignored), and `E` exponents inside numbers (`1.5E12`).
 */
object ExpressionEvaluator {

    private val MC: MathContext = MathContext.DECIMAL128
    private const val MAX_EXPONENT = 9_999
    private const val MAX_FACTORIAL = 1_000
    private val DOUBLE_ROUNDING = MathContext(15, RoundingMode.HALF_EVEN)

    fun evaluate(expression: String, angleUnit: AngleUnit = AngleUnit.Degrees): EvalResult {
        if (expression.isBlank()) return EvalResult.Empty
        return try {
            val tokens = tokenize(expression)
            if (tokens.isEmpty()) return EvalResult.Empty
            val value = Parser(tokens, angleUnit).parseAll()
            EvalResult.Success(value)
        } catch (e: EvalException) {
            EvalResult.Failure(e.error)
        } catch (_: ArithmeticException) {
            EvalResult.Failure(EvalError.Domain)
        } catch (_: NumberFormatException) {
            EvalResult.Failure(EvalError.Malformed)
        }
    }

    /** Display text for a result: up to 10 significant digits, no trailing zeros, E for huge/tiny. */
    fun format(value: BigDecimal, grouping: Boolean = false): String =
        CalcFormat.significant(value, digits = 10, grouping = grouping, maxPlainExponent = 10, minPlainExponent = -7)

    // ---- tokens ---------------------------------------------------------------------------

    private sealed interface Token {
        data class Num(val value: BigDecimal) : Token
        data class Op(val symbol: Char) : Token        // + - × ÷ ^
        data class Postfix(val symbol: Char) : Token   // % ! ²
        data class Func(val name: String) : Token      // sin cos tan log ln √
        data class Const(val value: BigDecimal) : Token
        data object LParen : Token
        data object RParen : Token
    }

    private class EvalException(val error: EvalError) : Exception()

    private fun fail(error: EvalError = EvalError.Malformed): Nothing = throw EvalException(error)

    private val PI: BigDecimal = BigDecimal("3.141592653589793238462643383279503")
    private val E: BigDecimal = BigDecimal("2.718281828459045235360287471352662")

    private fun tokenize(input: String): List<Token> {
        val tokens = ArrayList<Token>()
        var i = 0
        val s = input
        while (i < s.length) {
            val c = s[i]
            when {
                c.isWhitespace() || c == ',' -> i++
                c.isDigit() || c == '.' -> {
                    val start = i
                    var dots = 0
                    while (i < s.length && (s[i].isDigit() || s[i] == '.' || s[i] == ',')) {
                        if (s[i] == '.') dots++
                        i++
                    }
                    // Exponent: E, optional sign, digits (only uppercase E; lowercase e is Euler).
                    if (i < s.length && s[i] == 'E') {
                        var j = i + 1
                        if (j < s.length && (s[j] == '+' || s[j] == '-' || s[j] == '−')) j++
                        if (j < s.length && s[j].isDigit()) {
                            while (j < s.length && s[j].isDigit()) j++
                            i = j
                        }
                    }
                    if (dots > 1) fail()
                    val text = s.substring(start, i).replace(",", "").replace('−', '-')
                    if (text == "." || text.startsWith(".E") ) fail()
                    tokens += Token.Num(BigDecimal(text))
                }
                c == '+' -> { tokens += Token.Op('+'); i++ }
                c == '-' || c == '−' || c == '–' -> { tokens += Token.Op('-'); i++ }
                c == '*' || c == '×' || c == '·' -> { tokens += Token.Op('×'); i++ }
                c == '/' || c == '÷' -> { tokens += Token.Op('÷'); i++ }
                c == '^' -> { tokens += Token.Op('^'); i++ }
                c == '%' || c == '!' || c == '²' -> { tokens += Token.Postfix(c); i++ }
                c == '(' -> { tokens += Token.LParen; i++ }
                c == ')' -> { tokens += Token.RParen; i++ }
                c == '√' -> { tokens += Token.Func("√"); i++ }
                c == 'π' -> { tokens += Token.Const(PI); i++ }
                c.isLetter() -> {
                    val start = i
                    while (i < s.length && s[i].isLetter() && s[i] != 'π') i++
                    tokens += lettersToTokens(s.substring(start, i))
                }
                else -> fail()
            }
        }
        return tokens
    }

    /** Splits a run of letters into functions and constants ("pie" → π e, "sin" → sin). */
    private fun lettersToTokens(word: String): List<Token> {
        val out = ArrayList<Token>()
        var rest = word.lowercase()
        val names = listOf("sqrt", "sin", "cos", "tan", "log", "ln", "pi", "e")
        while (rest.isNotEmpty()) {
            val name = names.firstOrNull { rest.startsWith(it) } ?: fail()
            out += when (name) {
                "pi" -> Token.Const(PI)
                "e" -> Token.Const(E)
                "sqrt" -> Token.Func("√")
                else -> Token.Func(name)
            }
            rest = rest.substring(name.length)
        }
        return out
    }

    // ---- parser ---------------------------------------------------------------------------

    /** A value plus whether it came straight from a trailing `%` (for phone-style percent). */
    private class Value(val number: BigDecimal, val isPercent: Boolean = false)

    private class Parser(private val tokens: List<Token>, private val angleUnit: AngleUnit) {
        private var pos = 0

        private fun peek(): Token? = tokens.getOrNull(pos)
        private fun next(): Token = tokens.getOrNull(pos++) ?: fail()

        fun parseAll(): BigDecimal {
            val value = parseExpression()
            // A stray ')' is malformed; missing ')' at the end are auto-closed in parsePrimary.
            if (pos != tokens.size) fail()
            return value.number
        }

        private fun parseExpression(): Value {
            var left = parseTerm()
            while (true) {
                val t = peek()
                if (t is Token.Op && (t.symbol == '+' || t.symbol == '-')) {
                    pos++
                    val right = parseTerm()
                    val amount = if (right.isPercent) {
                        checked(left.number.multiply(right.number, MC))
                    } else right.number
                    left = Value(
                        checked(
                            if (t.symbol == '+') left.number.add(amount, MC)
                            else left.number.subtract(amount, MC)
                        )
                    )
                } else return left
            }
        }

        private fun parseTerm(): Value {
            var left = parseUnary()
            while (true) {
                val t = peek()
                when {
                    t is Token.Op && (t.symbol == '×' || t.symbol == '÷') -> {
                        pos++
                        val right = parseUnary()
                        left = Value(
                            if (t.symbol == '×') checked(left.number.multiply(right.number, MC))
                            else divide(left.number, right.number)
                        )
                    }
                    // Implicit multiplication: 2π, 2(3), 3sin(30), (1+1)(2), 2√9
                    startsPrimary(t) -> {
                        val right = parseUnary()
                        left = Value(checked(left.number.multiply(right.number, MC)))
                    }
                    else -> return left
                }
            }
        }

        private fun startsPrimary(t: Token?): Boolean =
            t is Token.Num || t is Token.Const || t is Token.Func || t == Token.LParen

        private fun parseUnary(): Value {
            val t = peek()
            if (t is Token.Op && (t.symbol == '-' || t.symbol == '+')) {
                pos++
                val operand = parseUnary()
                return if (t.symbol == '-') Value(operand.number.negate(), operand.isPercent) else operand
            }
            return parsePower()
        }

        private fun parsePower(): Value {
            val base = parsePostfix()
            val t = peek()
            if (t is Token.Op && t.symbol == '^') {
                pos++
                val exponent = parseUnary()
                return Value(power(base.number, exponent.number))
            }
            return base
        }

        private fun parsePostfix(): Value {
            var value = parsePrimary()
            while (true) {
                val t = peek()
                if (t !is Token.Postfix) return value
                pos++
                value = when (t.symbol) {
                    '%' -> Value(value.number.divide(BigDecimal(100), MC), isPercent = true)
                    '!' -> Value(factorial(value.number))
                    '²' -> Value(checked(value.number.multiply(value.number, MC)))
                    else -> fail()
                }
            }
        }

        private fun parsePrimary(): Value {
            return when (val t = next()) {
                is Token.Num -> Value(checked(t.value))
                is Token.Const -> Value(t.value)
                Token.LParen -> {
                    val inner = parseExpression()
                    when (peek()) {
                        Token.RParen -> pos++
                        null -> Unit // auto-close at end of input
                        else -> fail()
                    }
                    Value(inner.number)
                }
                is Token.Func -> {
                    val arg = if (t.name == "√") parsePostfix() else parseFunctionArgument()
                    Value(applyFunction(t.name, arg.number))
                }
                else -> fail()
            }
        }

        /** `sin(30)`, `sin 30`, `sin30!` — a postfix-level operand. */
        private fun parseFunctionArgument(): Value {
            if (peek() == null) fail()
            return parsePostfix()
        }

        private fun applyFunction(name: String, x: BigDecimal): BigDecimal = when (name) {
            "√" -> {
                if (x.signum() < 0) fail(EvalError.Domain)
                fromDouble(Math.sqrt(x.toDouble()))
            }
            "sin", "cos", "tan" -> trig(name, x)
            "log" -> {
                if (x.signum() <= 0) fail(EvalError.Domain)
                fromDouble(Math.log10(x.toDouble()))
            }
            "ln" -> {
                if (x.signum() <= 0) fail(EvalError.Domain)
                fromDouble(Math.log(x.toDouble()))
            }
            else -> fail()
        }

        private fun trig(name: String, x: BigDecimal): BigDecimal {
            if (angleUnit == AngleUnit.Degrees) {
                // Exact results at multiples of 90° (avoids 1.2E-16 and tan(90°) = 1.6E16).
                val reduced = x.remainder(BigDecimal(360))
                    .let { if (it.signum() < 0) it.add(BigDecimal(360)) else it }
                if (reduced.remainder(BigDecimal(90)).signum() == 0) {
                    val quadrant = reduced.divide(BigDecimal(90)).toInt() % 4
                    val sin = intArrayOf(0, 1, 0, -1)[quadrant]
                    val cos = intArrayOf(1, 0, -1, 0)[quadrant]
                    return when (name) {
                        "sin" -> BigDecimal(sin)
                        "cos" -> BigDecimal(cos)
                        else -> if (cos == 0) fail(EvalError.Domain) else BigDecimal(sin * cos)
                    }
                }
                val radians = Math.toRadians(reduced.toDouble())
                return fromDouble(trigOf(name, radians))
            }
            return fromDouble(trigOf(name, x.toDouble()))
        }

        /** Trig in radians; results within 1e-14 of zero snap to 0 (so sin(π) = 0). */
        private fun trigOf(name: String, radians: Double): Double {
            val r = when (name) {
                "sin" -> Math.sin(radians)
                "cos" -> Math.cos(radians)
                else -> Math.tan(radians)
            }
            return if (Math.abs(r) < 1e-14) 0.0 else r
        }
    }

    // ---- arithmetic helpers ---------------------------------------------------------------

    private fun divide(a: BigDecimal, b: BigDecimal): BigDecimal {
        if (b.signum() == 0) fail(EvalError.DivideByZero)
        return checked(a.divide(b, MC))
    }

    private fun power(base: BigDecimal, exponent: BigDecimal): BigDecimal {
        val stripped = exponent.stripTrailingZeros()
        val isInteger = stripped.scale() <= 0
        if (isInteger && stripped.abs() <= BigDecimal(MAX_EXPONENT)) {
            val n = stripped.toInt()
            if (base.signum() == 0 && n < 0) fail(EvalError.DivideByZero)
            if (n == 0) return BigDecimal.ONE
            // Guard the magnitude before computing (e.g. 10^9999 × 10^9999).
            val baseExp = magnitude(base)
            if (base.signum() != 0 && Math.abs(baseExp.toLong() * n) > MAX_EXPONENT + 34) fail(EvalError.Overflow)
            return checked(base.pow(n, MC))
        }
        if (base.signum() < 0) fail(EvalError.Domain)
        if (base.signum() == 0) {
            return if (exponent.signum() > 0) BigDecimal.ZERO else fail(EvalError.DivideByZero)
        }
        return fromDouble(Math.pow(base.toDouble(), exponent.toDouble()))
    }

    private fun factorial(x: BigDecimal): BigDecimal {
        val stripped = x.stripTrailingZeros()
        if (stripped.signum() < 0 || stripped.scale() > 0) fail(EvalError.Domain)
        if (stripped > BigDecimal(MAX_FACTORIAL)) fail(EvalError.Overflow)
        val n = stripped.toInt()
        var result = BigDecimal.ONE
        for (k in 2..n) result = result.multiply(BigDecimal(k), MC)
        return checked(result)
    }

    /** Decimal exponent of [x] (0 for 0): 1234 → 3, 0.05 → -2. */
    private fun magnitude(x: BigDecimal): Int =
        if (x.signum() == 0) 0 else x.precision() - x.scale() - 1

    private fun checked(x: BigDecimal): BigDecimal {
        val m = magnitude(x)
        if (m > MAX_EXPONENT) fail(EvalError.Overflow)
        if (m < -MAX_EXPONENT) return BigDecimal.ZERO
        return x
    }

    private fun fromDouble(d: Double): BigDecimal {
        if (d.isNaN()) fail(EvalError.Domain)
        if (d.isInfinite()) fail(EvalError.Overflow)
        return BigDecimal(d).round(DOUBLE_ROUNDING).stripTrailingZeros()
    }
}
