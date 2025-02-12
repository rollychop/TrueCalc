package com.brohit.truecalc.core

import com.brohit.truecalc.common.Constant.INRFormatter
import com.brohit.truecalc.common.Constant.IndiaEnglishLocale

object TextUtils {
    fun Double.toFixed2INR(): String {
        if (isNaN()) return "-"
        if (isInfinite()) return "∞"
        return INRFormatter.format(this)
    }

    fun Double.toFixed2(): String {
        return String.format(IndiaEnglishLocale, "%.2f", this)
    }
}