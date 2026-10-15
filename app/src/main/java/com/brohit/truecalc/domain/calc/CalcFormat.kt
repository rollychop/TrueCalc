package com.brohit.truecalc.domain.calc

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

/**
 * Locale-independent number formatting for tool results, so output (and tests) are identical on
 * Android and the JVM. Money keeps using `core/TextUtils.toFixed2INR`.
 */
object CalcFormat {

    /**
     * [value] rounded half-up to at most [maxDecimals] places, trailing zeros removed, with
     * Indian digit grouping when [grouping] (12,34,567.5). NaN gives "-", infinities "∞".
     */
    fun number(value: Double, maxDecimals: Int = 2, grouping: Boolean = true): String {
        if (value.isNaN()) return "-"
        if (value.isInfinite()) return if (value > 0) "∞" else "-∞"
        val rounded = BigDecimal.valueOf(value).setScale(maxDecimals, RoundingMode.HALF_UP)
        return plain(rounded, grouping)
    }

    /**
     * [value] to at most [digits] significant digits, trailing zeros removed. Magnitudes of
     * 10^[maxPlainExponent] or more, or below 10^[minPlainExponent], use E notation ("1.5E12").
     */
    fun significant(
        value: Double,
        digits: Int = 10,
        grouping: Boolean = true,
        maxPlainExponent: Int = 12,
        minPlainExponent: Int = -6,
    ): String {
        if (value.isNaN()) return "-"
        if (value.isInfinite()) return if (value > 0) "∞" else "-∞"
        return significant(BigDecimal.valueOf(value), digits, grouping, maxPlainExponent, minPlainExponent)
    }

    /** BigDecimal flavour of [significant]. */
    fun significant(
        value: BigDecimal,
        digits: Int = 10,
        grouping: Boolean = true,
        maxPlainExponent: Int = 12,
        minPlainExponent: Int = -6,
    ): String {
        if (value.signum() == 0) return "0"
        val rounded = value.round(MathContext(digits, RoundingMode.HALF_UP)).stripTrailingZeros()
        val exponent = rounded.precision() - rounded.scale() - 1
        if (exponent >= maxPlainExponent || exponent < minPlainExponent) {
            val unscaled = rounded.unscaledValue().abs().toString()
            val mantissa = if (unscaled.length == 1) unscaled
            else unscaled.substring(0, 1) + "." + unscaled.substring(1)
            val sign = if (rounded.signum() < 0) "-" else ""
            return "${sign}${mantissa}E$exponent"
        }
        return plain(rounded, grouping)
    }

    /** Plain (non-E) string of [value] without trailing zeros, optionally Indian-grouped. */
    fun plain(value: BigDecimal, grouping: Boolean = true): String {
        val stripped = value.stripTrailingZeros()
        val text = (if (stripped.scale() < 0) stripped.setScale(0) else stripped).toPlainString()
        if (text == "-0") return "0"
        return if (grouping) groupIndian(text) else text
    }

    /**
     * Inserts Indian grouping separators into a plain decimal string: the last three integer
     * digits, then groups of two ("1234567.89" → "12,34,567.89").
     */
    fun groupIndian(plain: String): String {
        val negative = plain.startsWith("-")
        val body = if (negative) plain.substring(1) else plain
        val dot = body.indexOf('.')
        val intPart = if (dot >= 0) body.substring(0, dot) else body
        val fraction = if (dot >= 0) body.substring(dot) else ""
        if (intPart.length <= 3) return plain
        val last3 = intPart.takeLast(3)
        var rest = intPart.dropLast(3)
        val groups = ArrayList<String>()
        while (rest.length > 2) {
            groups.add(0, rest.takeLast(2))
            rest = rest.dropLast(2)
        }
        if (rest.isNotEmpty()) groups.add(0, rest)
        val grouped = (groups + last3).joinToString(",")
        return (if (negative) "-" else "") + grouped + fraction
    }

    /** Parses a user-typed number, ignoring grouping commas and surrounding spaces. */
    fun parse(text: String): Double? = text.replace(",", "").trim().toDoubleOrNull()
}
