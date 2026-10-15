package com.brohit.truecalc.ui.screens.health.ideal_weight

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.brohit.truecalc.domain.calc.Gender
import com.brohit.truecalc.domain.model.TextFieldState

class IdealWeightInputState {
    val height = TextFieldState()

    /** Stored as the label ("Male"/"Female"), which is what history restores. */
    var gender by mutableStateOf(Gender.Male.label)

    /** Clears the inputs back to their defaults. */
    fun reset() {
        height.reset()
        gender = Gender.Male.label
    }
}
