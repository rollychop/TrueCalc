package com.brohit.truecalc.domain.model

import com.brohit.truecalc.core.TextUtils.toFixed2INR

data class InvestmentResultState(
    val amount: Double = 0.0,
    val interest: Double = 0.0,
    val principal: Double = 0.0
) {
    val finalAmount: String = if (amount == 0.0) "₹0.00" else amount.toFixed2INR()
    val interestEarned: String = if (interest == 0.0) "₹0.00" else interest.toFixed2INR()
    val totalInvestment: String = if (principal == 0.0) "₹0.00" else principal.toFixed2INR()
}