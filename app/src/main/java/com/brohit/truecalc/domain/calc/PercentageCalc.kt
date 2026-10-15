package com.brohit.truecalc.domain.calc

/** The four percentage questions the Percentage tool answers. [key] is stored in history. */
enum class PercentageMode(val key: String, val label: String) {
    PercentOf("percent_of", "X% of Y"),
    WhatPercent("what_percent", "X is what % of Y"),
    Change("change", "% change"),
    ApplyChange("apply_change", "Add / remove %");

    companion object {
        fun fromKey(key: String?): PercentageMode = entries.firstOrNull { it.key == key } ?: PercentOf
    }
}

/** Whether a percentage is added or taken off ([PercentageCalc.applyChange]). */
enum class ChangeDirection(val key: String, val label: String) {
    Increase("increase", "Increase"),
    Decrease("decrease", "Decrease");

    companion object {
        fun fromKey(key: String?): ChangeDirection = entries.firstOrNull { it.key == key } ?: Increase
    }
}

/** Percentage maths. Functions return null when the answer is undefined (division by zero). */
object PercentageCalc {

    /** X% of Y = X / 100 × Y. */
    fun percentOf(percent: Double, value: Double): Double = percent / 100.0 * value

    /** X is what percent of Y = X / Y × 100; null when Y is 0. */
    fun whatPercent(part: Double, whole: Double): Double? =
        if (whole == 0.0) null else part / whole * 100.0

    /**
     * Percentage change from [from] to [to] = (to − from) / |from| × 100, signed: positive for an
     * increase, negative for a decrease. Null when [from] is 0.
     */
    fun percentChange(from: Double, to: Double): Double? =
        if (from == 0.0) null else (to - from) / Math.abs(from) * 100.0

    /** [value] after a [percent]% increase or decrease: value × (1 ± percent / 100). */
    fun applyChange(value: Double, percent: Double, direction: ChangeDirection): Double =
        when (direction) {
            ChangeDirection.Increase -> value * (1 + percent / 100.0)
            ChangeDirection.Decrease -> value * (1 - percent / 100.0)
        }
}
