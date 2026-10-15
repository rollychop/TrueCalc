package com.brohit.truecalc.common

/**
 * Pure text logic behind the app's number fields (input filtering, sign toggle, and Indian digit
 * grouping with cursor mapping), kept free of Compose so it is unit-tested on the JVM.
 */
object NumberInput {

    /**
     * Keeps digits and (if [allowDecimal]) one '.', mapping ',' to '.'; with [allowNegative] a '-'
     * (or '−') typed first is kept as a leading minus. At most [maxDecimals] digits after the point.
     * Returns null when the edit should be rejected (a second point, or too many decimals).
     */
    fun filter(
        raw: String,
        allowDecimal: Boolean,
        allowNegative: Boolean = false,
        maxDecimals: Int = Int.MAX_VALUE,
    ): String? {
        val normalized = raw.replace(',', '.').replace('−', '-')
        val negative = allowNegative && normalized.startsWith("-")
        val kept = normalized.filter { it.isDigit() || (allowDecimal && it == '.') }
        if (kept.count { it == '.' } > 1) return null
        val dot = kept.indexOf('.')
        if (dot >= 0 && kept.length - dot - 1 > maxDecimals) return null
        return if (negative) "-$kept" else kept
    }

    /** Adds or removes the leading '-' ("" → "-", "-" → "", "5" → "-5", "-5" → "5"). */
    fun toggleSign(text: String): String = if (text.startsWith("-")) text.drop(1) else "-$text"

    /**
     * [raw] ("1234567.5") shown with Indian grouping and [prefix]: "₹ 12,34,567.5". Only the
     * integer part is grouped; empty text stays empty so the field's placeholder shows.
     */
    fun grouped(raw: String, prefix: String = ""): GroupedText {
        if (raw.isEmpty()) return GroupedText("", IntArray(1), IntArray(1))
        val negative = raw.startsWith("-")
        val body = if (negative) raw.substring(1) else raw
        val dot = body.indexOf('.')
        val intLen = if (dot >= 0) dot else body.length
        val intStart = if (negative) 1 else 0

        val out = StringBuilder(prefix)
        // originalToTransformed[i] = where raw char i lands; last entry = end of text.
        val o2t = IntArray(raw.length + 1)
        if (negative) {
            o2t[0] = out.length
            out.append('-')
        }
        for (k in 0 until intLen) {
            // Separator before this digit when the digits left after it form a group boundary:
            // last three, then every two (… ,xx,xx,xxx).
            val remaining = intLen - k
            if (k > 0 && (remaining == 3 || (remaining > 3 && (remaining - 3) % 2 == 0))) out.append(',')
            o2t[intStart + k] = out.length
            out.append(body[k])
        }
        for (i in intStart + intLen until raw.length) {
            o2t[i] = out.length
            out.append(raw[i])
        }
        o2t[raw.length] = out.length

        val text = out.toString()
        // transformedToOriginal[j] = raw chars placed before position j (prefix and commas map to
        // the next raw char).
        val t2o = IntArray(text.length + 1)
        var rawIndex = 0
        for (j in 0..text.length) {
            while (rawIndex < raw.length && o2t[rawIndex] < j) rawIndex++
            t2o[j] = rawIndex
        }
        return GroupedText(text, o2t, t2o)
    }

    class GroupedText(
        val text: String,
        private val originalToTransformed: IntArray,
        private val transformedToOriginal: IntArray,
    ) {
        fun originalToTransformed(offset: Int): Int =
            originalToTransformed[offset.coerceIn(0, originalToTransformed.lastIndex)]

        fun transformedToOriginal(offset: Int): Int =
            transformedToOriginal[offset.coerceIn(0, transformedToOriginal.lastIndex)]
    }
}
