package com.brohit.truecalc.domain.calc

enum class BmiCategory(val label: String) {
    Underweight("Underweight"),
    Normal("Normal weight"),
    Overweight("Overweight"),
    Obesity("Obesity");
}

data class BmiResult(val bmi: Double, val category: BmiCategory)

/** BMI maths, extracted verbatim from `BMICalculatorViewModel`. */
object BmiCalc {

    /** BMI = kg / m². Returns null when height is 0 (the ViewModel showed an empty result). */
    fun calculate(weightKg: Double, heightCm: Double): BmiResult? {
        if (heightCm == 0.0) return null
        val heightM = heightCm / 100.0
        val bmi = weightKg / (heightM * heightM)
        return BmiResult(bmi, categoryOf(bmi))
    }

    /** Text-field entry point; null if a field doesn't parse or height is 0. */
    fun calculate(weight: String, height: String): BmiResult? {
        val w = weight.toDoubleOrNull() ?: return null
        val h = height.toDoubleOrNull() ?: return null
        return calculate(w, h)
    }

    /** Lower and upper bound of the normal BMI band used by [categoryOf]. */
    const val HEALTHY_MIN = 18.5
    const val HEALTHY_MAX = 24.9

    /**
     * Body weight (kg) range that keeps BMI within [HEALTHY_MIN]..[HEALTHY_MAX] for a height;
     * null for a non-positive height.
     */
    fun healthyWeightRange(heightCm: Double): ClosedFloatingPointRange<Double>? {
        if (heightCm <= 0.0) return null
        val heightM = heightCm / 100.0
        val squared = heightM * heightM
        return (HEALTHY_MIN * squared)..(HEALTHY_MAX * squared)
    }

    /** Thresholds as in the original app: <18.5, <24.9, <29.9, else obesity. */
    fun categoryOf(bmi: Double): BmiCategory = when {
        bmi < HEALTHY_MIN -> BmiCategory.Underweight
        bmi < HEALTHY_MAX -> BmiCategory.Normal
        bmi < 29.9 -> BmiCategory.Overweight
        else -> BmiCategory.Obesity
    }
}
