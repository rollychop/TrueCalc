package com.brohit.truecalc.domain.model

import com.brohit.truecalc.ui.screens.compund_interest.CalculationType

enum class CompoundingFrequency(val timesPerYear: Int) {
    Annually(1),
    SemiAnnually(2),
    Quarterly(4),
    Monthly(12),
    Daily(365);

    fun getLabelFor(context: String): String {
        return when (context) {
            CalculationType.FIXED -> when (this) {
                Annually -> "Yearly"
                SemiAnnually -> "Half-Yearly"
                Quarterly -> "Quarterly"
                Monthly -> "Monthly"
                Daily -> "Daily (Rare)"
            }

            else -> when (this) {
                Annually -> "Annually"
                SemiAnnually -> "Semi-Annually"
                Quarterly -> "Quarterly"
                Monthly -> "Monthly"
                Daily -> "Daily"
            }
        }
    }
}