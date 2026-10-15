package com.brohit.truecalc.domain.calc

/** Devine ideal body weight, extracted verbatim from `IdealWeightCalculatorViewModel`. */
object IdealWeightCalc {

    private const val CM_PER_INCH = 2.54
    private const val BASE_HEIGHT_INCHES = 60.0 // 5 ft

    /** 50 kg (men) / 45.5 kg (women) + 2.3 kg per inch over 5 ft; heights under 5 ft get the base. */
    fun calculate(heightCm: Double, gender: Gender): Double {
        val heightInches = heightCm / CM_PER_INCH
        val extraInches = (heightInches - BASE_HEIGHT_INCHES).coerceAtLeast(0.0)
        return if (gender == Gender.Male) {
            50.0 + (2.3 * extraInches)
        } else {
            45.5 + (2.3 * extraInches)
        }
    }

    /** Text-field entry point; null if height doesn't parse. */
    fun calculate(height: String, gender: Gender): Double? =
        height.toDoubleOrNull()?.let { calculate(it, gender) }
}
