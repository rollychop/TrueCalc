package com.brohit.truecalc.domain.calc

import kotlin.math.pow

data class InterestResult(
    /** Final / maturity amount. */
    val amount: Double = 0.0,
    /** Interest earned. */
    val interest: Double = 0.0,
    /** Amount invested. */
    val principal: Double = 0.0,
) {
    companion object {
        val Empty = InterestResult()
    }
}

/**
 * Compound interest, simple interest and fixed deposit maths, extracted verbatim from
 * `CompoundInterestViewModel`. A fixed deposit is compound interest at the payout frequency.
 */
object InterestCalc {

    /** Converts a duration to years (months / 12). */
    fun toYears(time: Double, isTimeInYears: Boolean): Double =
        if (isTimeInYears) time else time / 12

    /** A = P (1 + r/n)^(n t) */
    fun compound(
        principal: Double,
        annualRatePercent: Double,
        years: Double,
        frequency: CompoundingFrequency,
    ): InterestResult {
        val rate = annualRatePercent / 100
        val n = frequency.timesPerYear
        val amount = principal * (1 + rate / n).pow(n * years)
        return InterestResult(amount = amount, interest = amount - principal, principal = principal)
    }

    /** I = P r t */
    fun simple(principal: Double, annualRatePercent: Double, years: Double): InterestResult {
        val rate = annualRatePercent / 100
        val interest = principal * rate * years
        return InterestResult(amount = interest + principal, interest = interest, principal = principal)
    }

    /** Fixed deposit: compound interest at the payout frequency. */
    fun fixedDeposit(
        principal: Double,
        annualRatePercent: Double,
        years: Double,
        frequency: CompoundingFrequency,
    ): InterestResult = compound(principal, annualRatePercent, years, frequency)

    /** Text-field entry point for [compound]; [InterestResult.Empty] if any field doesn't parse. */
    fun compound(
        principal: String,
        rate: String,
        time: String,
        isTimeInYears: Boolean,
        frequency: CompoundingFrequency,
    ): InterestResult = parse(principal, rate, time, isTimeInYears) { p, r, y ->
        compound(p, r, y, frequency)
    }

    /** Text-field entry point for [simple]; [InterestResult.Empty] if any field doesn't parse. */
    fun simple(
        principal: String,
        rate: String,
        time: String,
        isTimeInYears: Boolean,
    ): InterestResult = parse(principal, rate, time, isTimeInYears) { p, r, y -> simple(p, r, y) }

    /** Text-field entry point for [fixedDeposit]; [InterestResult.Empty] if any field doesn't parse. */
    fun fixedDeposit(
        principal: String,
        rate: String,
        time: String,
        isTimeInYears: Boolean,
        frequency: CompoundingFrequency,
    ): InterestResult = compound(principal, rate, time, isTimeInYears, frequency)

    private inline fun parse(
        principal: String,
        rate: String,
        time: String,
        isTimeInYears: Boolean,
        block: (Double, Double, Double) -> InterestResult,
    ): InterestResult {
        val p = principal.toDoubleOrNull() ?: return InterestResult.Empty
        val r = rate.toDoubleOrNull() ?: return InterestResult.Empty
        val t = time.toDoubleOrNull() ?: return InterestResult.Empty
        return block(p, r, toYears(t, isTimeInYears))
    }
}
