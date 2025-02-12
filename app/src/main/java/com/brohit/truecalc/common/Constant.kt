package com.brohit.truecalc.common

import java.text.NumberFormat
import java.util.Locale

object Constant {
    const val BASE_URL: String = ""
    const val DATA_STORE_NAME = "true_calc_store"
    val IndiaEnglishLocale = Locale("en", "IN")
    val INRFormatter: NumberFormat = NumberFormat.getCurrencyInstance(IndiaEnglishLocale)

}