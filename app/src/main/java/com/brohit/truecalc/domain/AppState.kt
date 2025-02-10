package com.brohit.truecalc.domain

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.time.LocalDate

object AppState {

    var isPromotionalBannerShown by mutableStateOf(false)
        private set

    val appValidTill: LocalDate = LocalDate.of(2025, 2, 28)
}