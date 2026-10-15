package com.brohit.truecalc.domain.calc

data class DiscountResult(
    val originalPrice: Double,
    /** Price after all discounts, never below 0. */
    val finalPrice: Double,
    /** originalPrice − finalPrice. */
    val saved: Double,
    /** saved as a percentage of the original price. */
    val effectivePercent: Double,
    /** Amount taken off by the first discount. */
    val firstDiscountAmount: Double,
    /** Amount taken off by the stacked second discount (0 when none). */
    val secondDiscountAmount: Double,
    /** Flat amount actually taken off (capped so the price doesn't go negative). */
    val flatAmount: Double,
)

/**
 * Shop discounts: a percentage, an optional second percentage applied on the already-discounted
 * price (stacked, "extra 10% off"), then an optional flat amount off.
 *
 * Example: ₹1,000 with 20% then 10% then ₹50 off → 1000 × 0.8 = 800 → × 0.9 = 720 → − 50 = 670;
 * saved ₹330, an effective 33% (not 30%).
 */
object DiscountCalc {

    /**
     * Null when [price] isn't positive or a percentage is outside 0..100 or the flat amount is
     * negative.
     */
    fun calculate(
        price: Double,
        discountPercent: Double,
        secondDiscountPercent: Double = 0.0,
        flatOff: Double = 0.0,
    ): DiscountResult? {
        if (price <= 0.0 || price.isNaN()) return null
        if (discountPercent !in 0.0..100.0 || secondDiscountPercent !in 0.0..100.0) return null
        if (flatOff < 0.0) return null

        val afterFirst = price * (1 - discountPercent / 100.0)
        val afterSecond = afterFirst * (1 - secondDiscountPercent / 100.0)
        val flat = minOf(flatOff, afterSecond)
        val finalPrice = afterSecond - flat
        val saved = price - finalPrice
        return DiscountResult(
            originalPrice = price,
            finalPrice = finalPrice,
            saved = saved,
            effectivePercent = saved / price * 100.0,
            firstDiscountAmount = price - afterFirst,
            secondDiscountAmount = afterFirst - afterSecond,
            flatAmount = flat,
        )
    }

    /** Text-field entry point; blank optional fields count as 0. */
    fun calculate(price: String, discount: String, second: String = "", flat: String = ""): DiscountResult? {
        val p = CalcFormat.parse(price) ?: return null
        val d = CalcFormat.parse(discount) ?: return null
        val s = if (second.isBlank()) 0.0 else CalcFormat.parse(second) ?: return null
        val f = if (flat.isBlank()) 0.0 else CalcFormat.parse(flat) ?: return null
        return calculate(p, d, s, f)
    }
}
