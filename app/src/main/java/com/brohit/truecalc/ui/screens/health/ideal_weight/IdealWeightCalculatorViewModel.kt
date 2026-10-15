package com.brohit.truecalc.ui.screens.health.ideal_weight

import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.brohit.truecalc.domain.HistoryManager
import com.brohit.truecalc.domain.calc.Gender
import com.brohit.truecalc.domain.calc.IdealWeightCalc
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
        val route: Route.Calculator.IdealWeight = savedStateHandle.toRoute()
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

    /** Clears the inputs back to their defaults. */
    fun reset() = inputState.reset()

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
            route = Route.Calculator.IdealWeight(),
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
        val idealWeight = IdealWeightCalc.calculate(heightText, Gender.fromLabel(gender))
            ?: return IdealWeightResultState()
        val value = "%.2f".format(idealWeight)
        return IdealWeightResultState(
            idealWeight = value,
            shareText = "My ideal body weight is $value kg at $heightText cm — via TrueCalc"
        )
    }
}
