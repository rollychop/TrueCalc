package com.brohit.truecalc.ui.screens.health.calorie

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.brohit.truecalc.domain.calc.ActivityLevel
import com.brohit.truecalc.domain.calc.Gender
import com.brohit.truecalc.domain.model.TextFieldState

class CaloriesInputState {
    val age = TextFieldState()
    val weight = TextFieldState()
    val height = TextFieldState()

    /** Stored as the label ("Male"/"Female"), which is what history restores. */
    var gender by mutableStateOf(Gender.Male.label)

    /** Stored as the [ActivityLevel.label], which is what history restores. */
    var activityLevel by mutableStateOf(ActivityLevel.Sedentary.label)

    /** Clears the inputs back to their defaults. */
    fun reset() {
        age.reset()
        weight.reset()
        height.reset()
        gender = Gender.Male.label
        activityLevel = ActivityLevel.Sedentary.label
    }
}
