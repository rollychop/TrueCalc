package com.brohit.truecalc.ui.screens.health.bmi

import com.brohit.truecalc.domain.model.TextFieldState

class BMIInputState {
    val weight = TextFieldState()
    val height = TextFieldState()

    /** Clears the inputs back to their defaults. */
    fun reset() {
        weight.reset()
        height.reset()
    }
}
