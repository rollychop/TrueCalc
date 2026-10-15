package com.brohit.truecalc.ui.components.tool

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import com.brohit.truecalc.common.NumberInput

/**
 * Shows a plain number ("1234567.5") as "₹ 12,34,567.5" with Indian grouping, decimals kept as
 * typed, and a cursor mapping that skips the prefix and separators (see [NumberInput.grouped]).
 * [prefixColor] tints the prefix.
 */
class IndianGroupingVisualTransformation(
    private val prefix: String = "₹ ",
    private val prefixColor: Color = Color.Unspecified,
) : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val grouped = NumberInput.grouped(text.text, prefix)
        val annotated = buildAnnotatedString {
            if (grouped.text.startsWith(prefix) && prefix.isNotEmpty()) {
                withStyle(SpanStyle(color = prefixColor)) { append(prefix) }
                append(grouped.text.substring(prefix.length))
            } else {
                append(grouped.text)
            }
        }
        return TransformedText(
            annotated,
            object : OffsetMapping {
                override fun originalToTransformed(offset: Int): Int = grouped.originalToTransformed(offset)
                override fun transformedToOriginal(offset: Int): Int = grouped.transformedToOriginal(offset)
            },
        )
    }

    override fun equals(other: Any?): Boolean =
        other is IndianGroupingVisualTransformation && other.prefix == prefix && other.prefixColor == prefixColor

    override fun hashCode(): Int = 31 * prefix.hashCode() + prefixColor.hashCode()
}
