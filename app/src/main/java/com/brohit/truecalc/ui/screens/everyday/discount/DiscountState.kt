package com.brohit.truecalc.ui.screens.everyday.discount

import com.brohit.truecalc.domain.model.TextFieldState
import com.brohit.truecalc.ui.components.tool.BreakdownRow

class DiscountInputState {
    val price = TextFieldState()
    val discount = TextFieldState()

    /** Optional extra % applied on the already-discounted price. */
    val secondDiscount = TextFieldState()

    /** Optional flat ₹ amount off, after the percentages. */
    val flatOff = TextFieldState()

    fun reset() {
        price.reset()
        discount.reset()
        secondDiscount.reset()
        flatOff.reset()
    }
}

data class DiscountResultState(
    /** Final price, formatted; null when there is no result. */
    val finalPrice: String? = null,
    val saved: String = "",
    val effectivePercent: String = "",
    val rows: List<BreakdownRow> = emptyList(),
    val emptyText: String = "Enter the price and discount to see the final price",
    val shareText: String? = null,
)
