package com.brohit.truecalc.ui.screens.health.bmi

import com.brohit.truecalc.domain.calc.BmiCategory

data class BMIResultState(
    /** BMI formatted with two decimals; empty when there is no result. */
    val bmiValue: String = "",
    /** Category label as stored in history ("Normal weight"…); empty when there is no result. */
    val category: String = "",
    val bmiCategory: BmiCategory? = null,
    /** Healthy weight range for the entered height, e.g. "56.7 – 76.3 kg". */
    val healthyWeightRange: String? = null,
    /** Text for the Share action; null until there is a valid result. */
    val shareText: String? = null,
)
