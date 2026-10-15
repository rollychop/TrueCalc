package com.brohit.truecalc.domain.calc

/**
 * How often a recurring deposit installment is paid.
 *
 * Entry names are persisted in history `restorationJson` (`"interval"` key), so never rename them.
 */
enum class RecurringInterval(val paymentsPerYear: Int, val label: String) {
    DAILY(365, "Daily"),
    WEEKLY(52, "Weekly"),
    MONTHLY(12, "Monthly"),
    QUARTERLY(4, "Quarterly"),
    HALF_YEARLY(2, "Half-Yearly"),
    YEARLY(1, "Yearly")
}
