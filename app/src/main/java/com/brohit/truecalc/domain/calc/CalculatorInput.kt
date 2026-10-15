package com.brohit.truecalc.domain.calc

/** How a calculator key is styled and grouped. */
enum class CalcKeyKind { Digit, Operator, Function, Control, Equals }

/** Every key on the calculator keypad. [label] is what the key shows. */
enum class CalcKey(val label: String, val kind: CalcKeyKind) {
    Zero("0", CalcKeyKind.Digit),
    One("1", CalcKeyKind.Digit),
    Two("2", CalcKeyKind.Digit),
    Three("3", CalcKeyKind.Digit),
    Four("4", CalcKeyKind.Digit),
    Five("5", CalcKeyKind.Digit),
    Six("6", CalcKeyKind.Digit),
    Seven("7", CalcKeyKind.Digit),
    Eight("8", CalcKeyKind.Digit),
    Nine("9", CalcKeyKind.Digit),
    Dot(".", CalcKeyKind.Digit),

    Add("+", CalcKeyKind.Operator),
    Subtract("−", CalcKeyKind.Operator),
    Multiply("×", CalcKeyKind.Operator),
    Divide("÷", CalcKeyKind.Operator),
    Percent("%", CalcKeyKind.Operator),
    /** Smart parenthesis: opens or closes depending on context. */
    Parens("( )", CalcKeyKind.Operator),

    Equals("=", CalcKeyKind.Equals),
    Clear("AC", CalcKeyKind.Control),
    Backspace("⌫", CalcKeyKind.Control),

    // Scientific
    AngleToggle("DEG", CalcKeyKind.Function),
    Sin("sin", CalcKeyKind.Function),
    Cos("cos", CalcKeyKind.Function),
    Tan("tan", CalcKeyKind.Function),
    Log("log", CalcKeyKind.Function),
    Ln("ln", CalcKeyKind.Function),
    Sqrt("√", CalcKeyKind.Function),
    Square("x²", CalcKeyKind.Function),
    Power("xʸ", CalcKeyKind.Function),
    Factorial("!", CalcKeyKind.Function),
    Pi("π", CalcKeyKind.Function),
    E("e", CalcKeyKind.Function),
    Ans("Ans", CalcKeyKind.Function),
    OpenParen("(", CalcKeyKind.Function),
    CloseParen(")", CalcKeyKind.Function);

    val digit: Char? get() = if (kind == CalcKeyKind.Digit && this != Dot) label[0] else null
}

/** Keypad layouts, row by row. */
object CalculatorLayouts {
    val basic: List<List<CalcKey>> = listOf(
        listOf(CalcKey.Clear, CalcKey.Parens, CalcKey.Percent, CalcKey.Divide),
        listOf(CalcKey.Seven, CalcKey.Eight, CalcKey.Nine, CalcKey.Multiply),
        listOf(CalcKey.Four, CalcKey.Five, CalcKey.Six, CalcKey.Subtract),
        listOf(CalcKey.One, CalcKey.Two, CalcKey.Three, CalcKey.Add),
        listOf(CalcKey.Zero, CalcKey.Dot, CalcKey.Backspace, CalcKey.Equals),
    )

    val scientific: List<List<CalcKey>> = listOf(
        listOf(CalcKey.AngleToggle, CalcKey.Sin, CalcKey.Cos, CalcKey.Tan, CalcKey.Pi),
        listOf(CalcKey.Ln, CalcKey.Log, CalcKey.Sqrt, CalcKey.Square, CalcKey.Power),
        listOf(CalcKey.Factorial, CalcKey.Clear, CalcKey.Parens, CalcKey.Percent, CalcKey.Divide),
        listOf(CalcKey.E, CalcKey.Seven, CalcKey.Eight, CalcKey.Nine, CalcKey.Multiply),
        listOf(CalcKey.Ans, CalcKey.Four, CalcKey.Five, CalcKey.Six, CalcKey.Subtract),
        listOf(CalcKey.OpenParen, CalcKey.One, CalcKey.Two, CalcKey.Three, CalcKey.Add),
        listOf(CalcKey.CloseParen, CalcKey.Zero, CalcKey.Dot, CalcKey.Backspace, CalcKey.Equals),
    )
}

/**
 * Pure editing rules for the calculator's expression line (what each key press does to the text).
 * [CalcKey.Equals] and [CalcKey.AngleToggle] don't edit the text; the ViewModel handles them.
 *
 * The expression uses display symbols (− × ÷ π √ ² ^) that [ExpressionEvaluator] understands.
 */
object CalculatorEditor {

    const val MAX_LENGTH = 200

    private const val BINARY_OPS = "+−×÷^"
    private val FUNCTION_PREFIXES = listOf("sin(", "cos(", "tan(", "log(", "ln(")

    /**
     * The expression after pressing [key].
     *
     * @param justEvaluated the expression currently holds the result of "=": a digit, constant or
     *   function starts a new expression, while an operator continues from the result.
     * @param lastAnswer the last result, inserted by [CalcKey.Ans].
     */
    fun apply(
        expression: String,
        key: CalcKey,
        justEvaluated: Boolean = false,
        lastAnswer: String? = null,
    ): String {
        if (key == CalcKey.Clear) return ""
        if (key == CalcKey.Backspace) return backspace(expression)
        if (key == CalcKey.Equals || key == CalcKey.AngleToggle) return expression

        val startsOperand = key.kind == CalcKeyKind.Digit || key in OPERAND_STARTERS
        val expr = if (justEvaluated && startsOperand) "" else expression
        val result = when {
            key.digit != null -> digit(expr, key.digit!!)
            key == CalcKey.Dot -> dot(expr)
            key in BINARY_KEYS -> binary(expr, BINARY_KEYS.getValue(key))
            key == CalcKey.Percent -> postfix(expr, '%')
            key == CalcKey.Factorial -> postfix(expr, '!')
            key == CalcKey.Square -> postfix(expr, '²')
            key == CalcKey.Sin -> operand(expr, "sin(")
            key == CalcKey.Cos -> operand(expr, "cos(")
            key == CalcKey.Tan -> operand(expr, "tan(")
            key == CalcKey.Log -> operand(expr, "log(")
            key == CalcKey.Ln -> operand(expr, "ln(")
            key == CalcKey.Sqrt -> operand(expr, "√")
            key == CalcKey.Pi -> operand(expr, "π")
            key == CalcKey.E -> operand(expr, "e")
            key == CalcKey.OpenParen -> operand(expr, "(")
            key == CalcKey.CloseParen -> closeParen(expr)
            key == CalcKey.Parens -> {
                if (openParens(expr) > 0 && endsWithOperand(expr)) closeParen(expr) else operand(expr, "(")
            }
            key == CalcKey.Ans -> {
                val answer = lastAnswer?.let(::toExpressionText) ?: return expression
                operand(expr, answer)
            }
            else -> expr
        }
        return if (result.length > MAX_LENGTH) expression else result
    }

    /** A formatted result ("-12.5", "1.2E-8") as expression text ("−12.5", "1.2E−8"). */
    fun toExpressionText(result: String): String = result.replace(",", "").replace('-', '−')

    private val OPERAND_STARTERS = setOf(
        CalcKey.Sin, CalcKey.Cos, CalcKey.Tan, CalcKey.Log, CalcKey.Ln, CalcKey.Sqrt,
        CalcKey.Pi, CalcKey.E, CalcKey.Ans, CalcKey.OpenParen,
    )

    private val BINARY_KEYS = mapOf(
        CalcKey.Add to '+',
        CalcKey.Subtract to '−',
        CalcKey.Multiply to '×',
        CalcKey.Divide to '÷',
        CalcKey.Power to '^',
    )

    /**
     * The expression for display, with Indian grouping in the integer part of each number
     * ("1234567+0.12345" → "12,34,567+0.12345"). Fractions and E exponents are left alone.
     */
    fun displayText(expr: String): String {
        val out = StringBuilder(expr.length + 8)
        var i = 0
        while (i < expr.length) {
            val c = expr[i]
            if (!c.isDigit()) {
                out.append(c)
                i++
                continue
            }
            var j = i
            while (j < expr.length && expr[j].isDigit()) j++
            val run = expr.substring(i, j)
            val prev = expr.getOrNull(i - 1)
            val prev2 = expr.getOrNull(i - 2)
            val isFractionOrExponent = prev == '.' || prev == 'E' ||
                    ((prev == '−' || prev == '-' || prev == '+') && prev2 == 'E')
            out.append(if (isFractionOrExponent) run else CalcFormat.groupIndian(run))
            i = j
        }
        return out.toString()
    }

    /** Number of '(' not yet closed. */
    fun openParens(expr: String): Int = expr.count { it == '(' } - expr.count { it == ')' }

    private fun endsWithOperand(expr: String): Boolean {
        val c = expr.lastOrNull() ?: return false
        return c.isDigit() || c == '.' || c == ')' || c == 'π' || c == 'e' || c == '%' || c == '!' || c == '²'
    }

    /** Trailing run of digits and dots (the number being typed). */
    private fun currentNumber(expr: String): String = expr.takeLastWhile { it.isDigit() || it == '.' }

    private fun digit(expr: String, d: Char): String {
        val last = expr.lastOrNull()
        if (last == ')' || last == 'π' || last == 'e' || last == '%' || last == '!' || last == '²') {
            return "$expr×$d"
        }
        val number = currentNumber(expr)
        // Exponent part of "1.5E12": just append.
        if (number == "0") return expr.dropLast(1) + d
        return expr + d
    }

    private fun dot(expr: String): String {
        val number = currentNumber(expr)
        if (number.contains('.')) return expr
        if (number.isEmpty()) {
            return if (endsWithOperand(expr)) "$expr×0." else "${expr}0."
        }
        return "$expr."
    }

    private fun binary(expr: String, op: Char): String {
        if (expr.isEmpty()) return if (op == '−') "−" else expr
        val last = expr.last()
        if (last == '(') return if (op == '−') "$expr−" else expr
        if (last in BINARY_OPS) {
            // "5×" + "−" → "5×−" (a negative operand); otherwise replace the trailing operator(s).
            if (op == '−' && last in "×÷^") return "$expr−"
            val trimmed = expr.trimEnd { it in BINARY_OPS }
            if (trimmed.isEmpty()) return if (op == '−') "−" else ""
            if (trimmed.last() == '(') return if (op == '−') "$trimmed−" else trimmed
            return trimmed + op
        }
        return expr + op
    }

    private fun postfix(expr: String, symbol: Char): String =
        if (endsWithOperand(expr) && expr.last() != '.') expr + symbol else expr

    /** Appends something that starts an operand, with an implicit × after a finished operand. */
    private fun operand(expr: String, text: String): String =
        if (endsWithOperand(expr)) "$expr×$text" else expr + text

    private fun closeParen(expr: String): String =
        if (openParens(expr) > 0 && endsWithOperand(expr)) "$expr)" else expr

    private fun backspace(expr: String): String {
        if (expr.isEmpty()) return expr
        val fn = FUNCTION_PREFIXES.firstOrNull { expr.endsWith(it) }
        return if (fn != null) expr.dropLast(fn.length) else expr.dropLast(1)
    }
}
