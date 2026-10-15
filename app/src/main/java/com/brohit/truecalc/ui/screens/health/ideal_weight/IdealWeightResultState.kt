package com.brohit.truecalc.ui.screens.health.ideal_weight

data class IdealWeightResultState(
    /** Ideal weight in kg with two decimals; empty when there is no result. */
    val idealWeight: String = "",
    /** Text for the Share action; null until there is a valid result. */
    val shareText: String? = null,
)
