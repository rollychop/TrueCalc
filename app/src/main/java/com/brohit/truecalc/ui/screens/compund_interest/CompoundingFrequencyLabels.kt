package com.brohit.truecalc.ui.screens.compund_interest

import com.brohit.truecalc.domain.calc.CompoundingFrequency

/**
 * User-facing label for a [CompoundingFrequency]. Fixed deposits talk about payout ("Yearly",
 * "Half-Yearly"), compound interest about compounding ("Annually", "Semi-Annually").
 *
 * @param context one of [CalculationType]'s constants.
 */
fun CompoundingFrequency.getLabelFor(context: String): String {
    return when (context) {
        CalculationType.FIXED -> when (this) {
            CompoundingFrequency.Annually -> "Yearly"
            CompoundingFrequency.SemiAnnually -> "Half-Yearly"
            CompoundingFrequency.Quarterly -> "Quarterly"
            CompoundingFrequency.Monthly -> "Monthly"
            CompoundingFrequency.Daily -> "Daily (Rare)"
        }

        else -> when (this) {
            CompoundingFrequency.Annually -> "Annually"
            CompoundingFrequency.SemiAnnually -> "Semi-Annually"
            CompoundingFrequency.Quarterly -> "Quarterly"
            CompoundingFrequency.Monthly -> "Monthly"
            CompoundingFrequency.Daily -> "Daily"
        }
    }
}
