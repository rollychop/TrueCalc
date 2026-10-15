package com.brohit.truecalc.ui.screens.health.calorie

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brohit.truecalc.domain.calc.ActivityLevel
import com.brohit.truecalc.domain.calc.Gender
import com.brohit.truecalc.ui.components.tool.BreakdownRow
import com.brohit.truecalc.ui.components.tool.BreakdownSection
import com.brohit.truecalc.ui.components.tool.InputSection
import com.brohit.truecalc.ui.components.tool.NumberField
import com.brohit.truecalc.ui.components.tool.OptionPicker
import com.brohit.truecalc.ui.components.tool.ResultHero
import com.brohit.truecalc.ui.components.tool.ToolInfo
import com.brohit.truecalc.ui.components.tool.ToolPreviewColumn
import com.brohit.truecalc.ui.components.tool.ToolPreviews
import com.brohit.truecalc.ui.components.tool.ToolScreen
import com.brohit.truecalc.ui.navigation.AppNavigator
import com.brohit.truecalc.ui.navigation.Route
import com.brohit.truecalc.ui.tools.ToolRegistry

@Composable
fun CaloriesCalculatorScreen(
    navigator: AppNavigator,
    viewModel: CaloriesCalculatorViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ToolScreen(
        tool = checkNotNull(ToolRegistry.forRoute(Route.Calculator.Calorie())),
        navigator = navigator,
        onReset = viewModel::reset,
        shareText = state.shareText,
    ) {
        CaloriesCalculatorContent(inputState = viewModel.inputState, state = state)
    }
}

@Composable
fun CaloriesCalculatorContent(
    inputState: CaloriesInputState,
    state: CaloriesResultState,
) {
    InputSection {
        NumberField(state = inputState.age, label = "Age", suffix = "years", decimal = false)
        OptionPicker(
            label = "Gender",
            options = Gender.entries.map { it.label },
            selected = inputState.gender,
            onSelect = { inputState.gender = it },
        )
        NumberField(state = inputState.height, label = "Height", suffix = "cm")
        NumberField(state = inputState.weight, label = "Weight", suffix = "kg")
        OptionPicker(
            label = "Activity level",
            options = ActivityLevel.entries.map { it.label },
            selected = inputState.activityLevel,
            onSelect = { inputState.activityLevel = it },
        )
    }

    val hasResult = state.caloriesNeeded.isNotEmpty()
    ResultHero(
        label = "Maintenance calories",
        value = if (hasResult) "${state.caloriesNeeded} kcal" else null,
        caption = if (hasResult) "per day to keep your current weight" else null,
        emptyText = "Enter age, height and weight to see your daily calories",
    )

    if (hasResult) {
        BreakdownSection(
            rows = listOf(
                BreakdownRow("Basal metabolic rate (BMR)", "${state.bmrValue} kcal"),
                BreakdownRow(
                    "Activity factor (${inputState.activityLevel.lowercase()})",
                    state.activityFactor?.let { "× $it" } ?: "–",
                ),
                BreakdownRow("Maintenance calories", "${state.caloriesNeeded} kcal", emphasized = true),
            ),
        )
    }

    ToolInfo(
        formula = "BMR = 10 × weight + 6.25 × height − 5 × age + 5 (men) or − 161 (women)\n" +
                "Calories = BMR × activity factor",
        notes = "Mifflin-St Jeor equation with weight in kg and height in cm. Activity factors: " +
                "sedentary 1.2, lightly active 1.375, moderately active 1.55, very active 1.725, " +
                "extra active 1.9.",
    )
}

@ToolPreviews
@Composable
private fun CaloriesCalculatorContentPreview() {
    val inputState = CaloriesInputState().apply {
        age.updateText("30")
        height.updateText("175")
        weight.updateText("70")
    }
    ToolPreviewColumn {
        CaloriesCalculatorContent(
            inputState = inputState,
            state = CaloriesResultState(bmrValue = "1648.75", caloriesNeeded = "1979", activityFactor = 1.2),
        )
    }
}
