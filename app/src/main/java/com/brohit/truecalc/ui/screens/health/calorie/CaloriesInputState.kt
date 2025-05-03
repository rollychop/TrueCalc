package com.brohit.truecalc.ui.screens.health.calorie

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.brohit.truecalc.domain.model.TextFieldState

class CaloriesInputState {
    val age = TextFieldState()
    val weight = TextFieldState()
    val height = TextFieldState()
    var gender by mutableStateOf("Male")
    var activityLevel by mutableStateOf("Sedentary")
}
