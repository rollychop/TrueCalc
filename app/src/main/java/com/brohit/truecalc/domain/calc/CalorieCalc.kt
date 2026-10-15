package com.brohit.truecalc.domain.calc

/** Activity multipliers for TDEE. [label]s are persisted in history (`"activity"` key). */
enum class ActivityLevel(val label: String, val factor: Double) {
    Sedentary("Sedentary", 1.2),
    LightlyActive("Lightly active", 1.375),
    ModeratelyActive("Moderately active", 1.55),
    VeryActive("Very active", 1.725),
    ExtraActive("Extra active", 1.9);

    companion object {
        /** Unknown labels fall back to [Sedentary] (factor 1.2), as the ViewModel did. */
        fun fromLabel(label: String): ActivityLevel =
            entries.firstOrNull { it.label == label } ?: Sedentary
    }
}

data class CalorieResult(
    /** Basal metabolic rate, kcal/day. */
    val bmr: Double,
    /** BMR × activity factor, kcal/day. */
    val calories: Double,
)

/** Mifflin-St Jeor BMR and daily calories, extracted verbatim from `CaloriesCalculatorViewModel`. */
object CalorieCalc {

    fun bmr(ageYears: Int, weightKg: Double, heightCm: Double, gender: Gender): Double =
        if (gender == Gender.Male) {
            (10 * weightKg) + (6.25 * heightCm) - (5 * ageYears) + 5
        } else {
            (10 * weightKg) + (6.25 * heightCm) - (5 * ageYears) - 161
        }

    fun calculate(
        ageYears: Int,
        weightKg: Double,
        heightCm: Double,
        gender: Gender,
        activity: ActivityLevel,
    ): CalorieResult {
        val bmr = bmr(ageYears, weightKg, heightCm, gender)
        return CalorieResult(bmr = bmr, calories = bmr * activity.factor)
    }

    /** Text-field entry point (age parsed as Int); null if a field doesn't parse. */
    fun calculate(
        age: String,
        weight: String,
        height: String,
        gender: Gender,
        activity: ActivityLevel,
    ): CalorieResult? {
        val a = age.toIntOrNull() ?: return null
        val w = weight.toDoubleOrNull() ?: return null
        val h = height.toDoubleOrNull() ?: return null
        return calculate(a, w, h, gender, activity)
    }
}
