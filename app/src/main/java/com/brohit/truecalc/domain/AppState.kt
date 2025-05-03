package com.brohit.truecalc.domain

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object AppState {
    var isPromotionalBannerShown by mutableStateOf(false)
        private set

    var isEmiSaveButtonEnabled by mutableStateOf(false)
        private set
}