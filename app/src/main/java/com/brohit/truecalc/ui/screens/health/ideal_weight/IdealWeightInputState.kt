package com.brohit.truecalc.ui.screens.health.ideal_weight

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.brohit.truecalc.domain.model.TextFieldState

class IdealWeightInputState {
    val height = TextFieldState()
    var gender by mutableStateOf("Male")
}
