package com.brohit.truecalc.ui.screens.health.ideal_weight

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
class IdealWeightCalculatorViewModel @Inject constructor(
    private val historyManager: HistoryManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val inputState = IdealWeightInputState()
    
    init {
        val route: Route.IdealWeightCalculator = savedStateHandle.toRoute()
        val restorationJson = route.restorationJson
        if (!restorationJson.isNullOrEmpty()) {
            try {
                val data = Json.decodeFromString<Map<String, String>>(restorationJson)
                inputState.height.updateText(data["height"] ?: "")
                data["gender"]?.let { inputState.gender = it }
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val stateFlow: StateFlow<IdealWeightResultState> = run {
        val height = snapshotFlow { inputState.height.text }
        val gender = snapshotFlow { inputState.gender }

        combine(height, gender) { heightText, gender ->
            heightText to gender
        }.mapLatest { (heightText, gender) ->
            val result = calculateIdealWeight(heightText, gender)
            if (result.idealWeight.isNotEmpty()) {
                logCalculation(heightText, gender, result)
            }
            result
        }.flowOn(Dispatchers.Default)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), IdealWeightResultState())

    val state: StateFlow<IdealWeightResultState> = stateFlow

    private fun logCalculation(height: String, gender: String, result: IdealWeightResultState) {
        historyManager.log(
            route = Route.IdealWeightCalculator(),
            name = "Ideal Weight Calculator",
            inputs = mapOf(
                "Height" to "$height cm",
                "Gender" to gender
            ),
            results = mapOf(
                "Ideal Weight" to "${result.idealWeight} kg"
            ),
            restorationInputs = mapOf(
                "height" to height,
                "gender" to gender
            )
        )
    }

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
