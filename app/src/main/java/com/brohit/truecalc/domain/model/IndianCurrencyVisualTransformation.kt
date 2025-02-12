package com.brohit.truecalc.domain.model

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import com.brohit.truecalc.common.Constant.IndiaEnglishLocale
import java.text.DecimalFormat

object IndianCurrencyVisualTransformation : VisualTransformation {
    private val formatter = DecimalFormat.getInstance(IndiaEnglishLocale) as DecimalFormat

    init {
        formatter.applyPattern("#,##,##0")
    }

    override fun filter(text: AnnotatedString): TransformedText {
        val formattedText = formatIndianCurrency(text.text)
        return TransformedText(
            AnnotatedString(formattedText),
            object : OffsetMapping {
                override fun originalToTransformed(offset: Int): Int = formattedText.length
                override fun transformedToOriginal(offset: Int): Int = text.text.length
            }
        )
    }

    private fun formatIndianCurrency(input: String): String {
        val amount = input.toLongOrNull() ?: return input
        return "₹ ${formatter.format(amount)}"
    }
}