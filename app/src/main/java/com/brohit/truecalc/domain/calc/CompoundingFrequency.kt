package com.brohit.truecalc.domain.calc

/**
 * How often interest is compounded (or paid out) per year.
 *
 * Pure enum: user-facing labels live in the UI layer (see `getLabelFor` in
 * `ui/screens/compund_interest`). Entry names are persisted in history `restorationJson`
 * (`"frequency"` key), so never rename them.
 */
enum class CompoundingFrequency(val timesPerYear: Int) {
    Annually(1),
    SemiAnnually(2),
    Quarterly(4),
    Monthly(12),
    Daily(365);
}
