package com.brohit.truecalc.domain.calc

import kotlin.math.ceil

data class SplitBillResult(
    val bill: Double,
    val tip: Double,
    /** Bill + tip. */
    val total: Double,
    val people: Int,
    /** What each person pays (rounded up to a whole rupee when requested). */
    val perPerson: Double,
    /** perPerson × people: what is collected (≥ total when rounding up). */
    val collected: Double,
    /** collected − total: the extra from rounding up (0 otherwise). */
    val roundingExtra: Double,
    val tipPerPerson: Double,
)

/** Splitting a bill with an optional tip between [people]. */
object SplitBillCalc {

    /** Tip chips (%); a custom value is also allowed. */
    val PRESET_TIPS: List<Double> = listOf(0.0, 5.0, 10.0, 15.0)

    /**
     * Null when [bill] is negative, [people] < 1 or [tipPercent] is negative.
     *
     * @param roundUp round each share up to the next whole rupee.
     */
    fun calculate(bill: Double, people: Int, tipPercent: Double, roundUp: Boolean = false): SplitBillResult? {
        if (bill < 0.0 || bill.isNaN() || people < 1 || tipPercent < 0.0) return null
        val tip = bill * tipPercent / 100.0
        val total = bill + tip
        val exactShare = total / people
        // Tiny epsilon so 100.0000000001 (floating error) doesn't round up to 101.
        val perPerson = if (roundUp) maxOf(0.0, ceil(exactShare - 1e-9)) else exactShare
        val collected = perPerson * people
        return SplitBillResult(
            bill = bill,
            tip = tip,
            total = total,
            people = people,
            perPerson = perPerson,
            collected = collected,
            roundingExtra = if (roundUp) collected - total else 0.0,
            tipPerPerson = tip / people,
        )
    }
}
