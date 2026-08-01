package com.brohit.truecalc.ui.screens.health.bmi

import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.brohit.truecalc.domain.HistoryManager
import com.brohit.truecalc.ui.navigation.Route
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.json.Json
import javax.inject.Inject

@HiltViewModel
class BMICalculatorViewModel @Inject constructor(
    private val historyManager: HistoryManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val inputState = BMIInputState()
    
    init {
        val route: Route.BmiCalculator = savedStateHandle.toRoute()
        val restorationJson = route.restorationJson
        if (!restorationJson.isNullOrEmpty()) {
            try {
                val data = Json.decodeFromString<Map<String, String>>(restorationJson)
                inputState.weight.updateText(data["weight"] ?: "")
                inputState.height.updateText(data["height"] ?: "")
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val stateFlow: StateFlow<BMIResultState> = run {
        val weight = snapshotFlow { inputState.weight.text }
        val height = snapshotFlow { inputState.height.text }

        combine(weight, height) { weightText, heightText ->
            Pair(weightText, heightText)
        }
            .mapLatest { (weightText, heightText) ->
                val result = calculateBMI(weightText, heightText)
                if (result.bmiValue.isNotEmpty()) {
                    logCalculation(weightText, heightText, result)
                }
                result
            }.flowOn(Dispatchers.Default)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), BMIResultState())

    val state: StateFlow<BMIResultState> = stateFlow

    private fun logCalculation(weight: String, height: String, result: BMIResultState) {
        historyManager.log(
            route = Route.BmiCalculator(),
            name = "BMI Calculator",
            inputs = mapOf(
                "Weight" to "$weight kg",
                "Height" to "$height cm"
            ),
            results = mapOf(
                "BMI" to result.bmiValue,
                "Category" to result.category
            ),
            restorationInputs = mapOf(
                "weight" to weight,
                "height" to height
            )
        )
    }

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
