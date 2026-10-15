package com.brohit.truecalc.domain.calc

/** Add GST to a pre-tax price, or take it out of a GST-inclusive price. */
enum class GstMode(val key: String, val label: String) {
    Add("add", "Add GST"),
    Remove("remove", "Remove GST");

    companion object {
        fun fromKey(key: String?): GstMode = entries.firstOrNull { it.key == key } ?: Add
    }
}

data class GstResult(
    /** Taxable value, before GST. */
    val net: Double,
    val gst: Double,
    /** Net + GST. */
    val gross: Double,
    /** Central GST: half the GST on an intra-state supply. */
    val cgst: Double,
    /** State GST: the other half. */
    val sgst: Double,
    val rate: Double,
)

/**
 * GST maths.
 *
 * - Add (exclusive → inclusive): GST = amount × rate / 100, gross = amount + GST.
 * - Remove (inclusive → exclusive): net = amount × 100 / (100 + rate), GST = amount − net.
 *
 * Intra-state supplies split GST equally into CGST and SGST; inter-state supplies charge the whole
 * amount as IGST.
 */
object GstCalc {

    /**
     * GST 2.0 rates (%) from 22 Sep 2025, shown as chips: nil, 0.25 (rough diamonds etc.),
     * 3 (gold, silver), the two main slabs 5 and 18, and 40 for luxury and sin goods. The old 12%
     * and 28% slabs were removed from the general structure; any other rate is a custom value.
     */
    val PRESET_RATES: List<Double> = listOf(0.0, 0.25, 3.0, 5.0, 18.0, 40.0)

    const val DEFAULT_RATE = 18.0

    /** Null when [amount] is negative or [rate] is outside 0..100. */
    fun calculate(amount: Double, rate: Double, mode: GstMode): GstResult? {
        if (amount < 0.0 || amount.isNaN() || rate !in 0.0..100.0) return null
        val net: Double
        val gst: Double
        when (mode) {
            GstMode.Add -> {
                net = amount
                gst = amount * rate / 100.0
            }
            GstMode.Remove -> {
                net = amount * 100.0 / (100.0 + rate)
                gst = amount - net
            }
        }
        return GstResult(
            net = net,
            gst = gst,
            gross = net + gst,
            cgst = gst / 2.0,
            sgst = gst / 2.0,
            rate = rate,
        )
    }
}
