package com.brohit.truecalc.ui.screens.health.bmi

import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn

class BMICalculatorViewModel : ViewModel() {

    val inputState = BMIInputState()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val stateFlow: StateFlow<BMIResultState> = run {
        val weight = snapshotFlow { inputState.weight.text }
        val height = snapshotFlow { inputState.height.text }

        combine(weight, height) { weightText, heightText ->
            Pair(weightText, heightText)
        }.mapLatest { (weightText, heightText) ->
            calculateBMI(weightText, heightText)
        }.flowOn(Dispatchers.Default)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), BMIResultState())

    val state: StateFlow<BMIResultState> = stateFlow

    private fun calculateBMI(weightText: String, heightText: String): BMIResultState {
        val weight = weightText.toDoubleOrNull() ?: return BMIResultState()
        val heightCm = heightText.toDoubleOrNull() ?: return BMIResultState()
        if (heightCm == 0.0) return BMIResultState()

        val heightM = heightCm / 100.0
        val bmi = weight / (heightM * heightM)

        val category = when {
            bmi < 18.5 -> "Underweight"
            bmi < 24.9 -> "Normal weight"
            bmi < 29.9 -> "Overweight"
            else -> "Obesity"
        }

        return BMIResultState(
            bmiValue = "%.2f".format(bmi),
            category = category
        )
    }
}
