package com.brohit.truecalc.ui.screens.health.calorie

import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.brohit.truecalc.domain.HistoryManager
import com.brohit.truecalc.domain.calc.ActivityLevel
import com.brohit.truecalc.domain.calc.CalorieCalc
import com.brohit.truecalc.domain.calc.Gender
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
class CaloriesCalculatorViewModel @Inject constructor(
    private val historyManager: HistoryManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val inputState = CaloriesInputState()
    
    init {
        val route: Route.Calculator.Calorie = savedStateHandle.toRoute()
        val restorationJson = route.restorationJson
        if (!restorationJson.isNullOrEmpty()) {
            try {
                val data = Json.decodeFromString<Map<String, String>>(restorationJson)
                inputState.age.updateText(data["age"] ?: "")
                inputState.height.updateText(data["height"] ?: "")
                inputState.weight.updateText(data["weight"] ?: "")
                data["gender"]?.let { inputState.gender = it }
                data["activity"]?.let { inputState.activityLevel = it }
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    /** Clears the inputs back to their defaults. */
    fun reset() = inputState.reset()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val stateFlow: StateFlow<CaloriesResultState> = run {
        val age = snapshotFlow { inputState.age.text }
        val weight = snapshotFlow { inputState.weight.text }
        val height = snapshotFlow { inputState.height.text }
        val gender = snapshotFlow { inputState.gender }
        val activityLevel = snapshotFlow { inputState.activityLevel }

        combine(age, weight, height, gender, activityLevel) { ageText, weightText, heightText, gender, activityLevel ->
            listOf(ageText, weightText, heightText, gender, activityLevel)
        }.mapLatest { params ->
            val result = calculateCalories(
                params[0],
                params[1],
                params[2],
                params[3],
                params[4]
            )
            if (result.caloriesNeeded.isNotEmpty() && result.caloriesNeeded != "0") {
                logCalculation(params[0], params[3], params[4], result)
            }
            result
        }.flowOn(Dispatchers.Default)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), CaloriesResultState())

    val state: StateFlow<CaloriesResultState> = stateFlow

    private fun logCalculation(age: String, gender: String, activity: String, result: CaloriesResultState) {
        historyManager.log(
            route = Route.Calculator.Calorie(),
            name = "Calorie Calculator",
            inputs = mapOf(
                "Age" to "$age years",
                "Gender" to gender,
                "Activity" to activity
            ),
            results = mapOf(
                "BMR" to "${result.bmrValue} kcal/day",
                "Calories Needed" to "${result.caloriesNeeded} kcal/day"
            ),
            restorationInputs = mapOf(
                "age" to age,
                "gender" to gender,
                "activity" to activity,
                "height" to inputState.height.text,
                "weight" to inputState.weight.text
            )
        )
    }

    private fun calculateCalories(
        ageText: String,
        weightText: String,
        heightText: String,
        gender: String,
        activityLevel: String
    ): CaloriesResultState {
        val activity = ActivityLevel.fromLabel(activityLevel)
        val result = CalorieCalc.calculate(
            age = ageText,
            weight = weightText,
            height = heightText,
            gender = Gender.fromLabel(gender),
            activity = activity
        ) ?: return CaloriesResultState()

        val calories = "%.0f".format(result.calories)
        return CaloriesResultState(
            bmrValue = "%.2f".format(result.bmr),
            caloriesNeeded = calories,
            activityFactor = activity.factor,
            shareText = "I need about $calories kcal a day to maintain my weight " +
                    "(${activity.label.lowercase()}) — via TrueCalc"
        )
    }
}
