package com.brohit.truecalc.ui.screens.health.calorie

import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import javax.inject.Inject

@HiltViewModel
class CaloriesCalculatorViewModel @Inject constructor(
    private val historyManager: HistoryManager
) : ViewModel() {

    val inputState = CaloriesInputState()

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
            route = Route.CalorieCalculator,
            name = "Calorie Calculator",
            inputs = mapOf(
                "Age" to "$age years",
                "Gender" to gender,
                "Activity" to activity
            ),
            results = mapOf(
                "BMR" to "${result.bmrValue} kcal/day",
                "Calories Needed" to "${result.caloriesNeeded} kcal/day"
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
        val age = ageText.toIntOrNull() ?: return CaloriesResultState()
        val weight = weightText.toDoubleOrNull() ?: return CaloriesResultState()
        val height = heightText.toDoubleOrNull() ?: return CaloriesResultState()

        val bmr = if (gender == "Male") {
            (10 * weight) + (6.25 * height) - (5 * age) + 5
        } else {
            (10 * weight) + (6.25 * height) - (5 * age) - 161
        }

        val activityFactor = when (activityLevel) {
            "Sedentary" -> 1.2
            "Lightly active" -> 1.375
            "Moderately active" -> 1.55
            "Very active" -> 1.725
            "Extra active" -> 1.9
            else -> 1.2
        }

        val calories = bmr * activityFactor

        return CaloriesResultState(
            bmrValue = "%.2f".format(bmr),
            caloriesNeeded = "%.0f".format(calories)
        )
    }
}
