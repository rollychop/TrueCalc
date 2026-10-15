package com.brohit.truecalc.domain.calc

/**
 * Biological sex used by the health formulas. Persisted in history as the [label]
 * ("Male"/"Female"); anything other than "Male" was treated as female, see [fromLabel].
 */
enum class Gender(val label: String) {
    Male("Male"),
    Female("Female");

    companion object {
        fun fromLabel(label: String): Gender = if (label == Male.label) Male else Female
    }
}
