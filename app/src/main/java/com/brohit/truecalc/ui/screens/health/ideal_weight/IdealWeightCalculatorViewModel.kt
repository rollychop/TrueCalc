package com.brohit.truecalc.ui.screens.health.ideal_weight

import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn

class IdealWeightCalculatorViewModel : ViewModel() {

    val inputState = IdealWeightInputState()

    private val stateFlow: StateFlow<IdealWeightResultState> = run {
        val height = snapshotFlow { inputState.height.text }
        val gender = snapshotFlow { inputState.gender }

        combine(height, gender) { heightText, gender ->
            calculateIdealWeight(heightText, gender)
        }.flowOn(Dispatchers.Default)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), IdealWeightResultState())

    val state: StateFlow<IdealWeightResultState> = stateFlow

    private fun calculateIdealWeight(
        heightText: String,
        gender: String
    ): IdealWeightResultState {
        val heightCm = heightText.toDoubleOrNull() ?: return IdealWeightResultState()

        // Convert cm to inches
        val heightInches = heightCm / 2.54
        val baseHeightInches = 60.0  // 5 feet = 60 inches

        val extraInches = (heightInches - baseHeightInches).coerceAtLeast(0.0)

        val idealWeight = if (gender == "Male") {
            50.0 + (2.3 * extraInches)
        } else {
            45.5 + (2.3 * extraInches)
        }

        return IdealWeightResultState(
            idealWeight = "%.2f".format(idealWeight)
        )
    }
}
