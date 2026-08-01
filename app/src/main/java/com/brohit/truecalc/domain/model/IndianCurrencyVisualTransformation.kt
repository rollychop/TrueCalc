package com.brohit.truecalc.domain.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import com.brohit.truecalc.common.Constant.IndiaEnglishLocale
import java.text.DecimalFormat

class IndianCurrencyVisualTransformation(private val color: Color = Color.Unspecified) : VisualTransformation {
    private val formatter = DecimalFormat.getInstance(IndiaEnglishLocale) as DecimalFormat

    init {
        formatter.applyPattern("#,##,##0")
    }

    override fun filter(text: AnnotatedString): TransformedText {
        val originalText = text.text
        val formattedText = formatIndianCurrency(originalText)

        val annotatedString = buildAnnotatedString {
            if (formattedText.startsWith("₹ ")) {
                withStyle(SpanStyle(color = color)) {
                    append("₹ ")
                }
                append(formattedText.substring(2))
            } else {
                append(formattedText)
            }
        }

        return TransformedText(
            annotatedString,
            object : OffsetMapping {
                override fun originalToTransformed(offset: Int): Int {
                    return annotatedString.length
                }

                override fun transformedToOriginal(offset: Int): Int {
                    return originalText.length
                }
            }
        )
    }

    private fun formatIndianCurrency(input: String): String {
        val amount = input.toLongOrNull() ?: return input
        return "₹ ${formatter.format(amount)}"
    }

    companion object : VisualTransformation {
        override fun filter(text: AnnotatedString): TransformedText {
            return IndianCurrencyVisualTransformation().filter(text)
        }
    }
}
