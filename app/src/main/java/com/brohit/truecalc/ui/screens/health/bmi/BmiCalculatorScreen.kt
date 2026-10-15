package com.brohit.truecalc.ui.screens.health.bmi

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brohit.truecalc.domain.calc.BmiCalc
import com.brohit.truecalc.domain.calc.BmiCategory
import com.brohit.truecalc.ui.components.tool.BreakdownRow
import com.brohit.truecalc.ui.components.tool.BreakdownSection
import com.brohit.truecalc.ui.components.tool.InputSection
import com.brohit.truecalc.ui.components.tool.NumberField
import com.brohit.truecalc.ui.components.tool.ResultHero
import com.brohit.truecalc.ui.components.tool.ToolInfo
import com.brohit.truecalc.ui.components.tool.ToolPreviewColumn
import com.brohit.truecalc.ui.components.tool.ToolPreviews
import com.brohit.truecalc.ui.components.tool.ToolScreen
import com.brohit.truecalc.ui.navigation.AppNavigator
import com.brohit.truecalc.ui.navigation.Route
import com.brohit.truecalc.ui.theme.LocalExtendedColors
import com.brohit.truecalc.ui.theme.Spacing
import com.brohit.truecalc.ui.tools.ToolRegistry

@Composable
fun BMICalculatorScreen(
    navigator: AppNavigator,
    viewModel: BMICalculatorViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ToolScreen(
        tool = checkNotNull(ToolRegistry.forRoute(Route.Calculator.Bmi())),
        navigator = navigator,
        onReset = viewModel::reset,
        shareText = state.shareText,
    ) {
        BmiCalculatorContent(inputState = viewModel.inputState, state = state)
    }
}

@Composable
fun BmiCalculatorContent(
    inputState: BMIInputState,
    state: BMIResultState,
) {
    InputSection {
        NumberField(state = inputState.weight, label = "Weight", suffix = "kg")
        NumberField(
            state = inputState.height,
            label = "Height",
            suffix = "cm",
            imeAction = ImeAction.Done,
        )
    }

    val hasResult = state.bmiValue.isNotEmpty()
    ResultHero(
        label = "Your BMI",
        value = state.bmiValue.ifEmpty { null },
        caption = if (hasResult) {
            "Healthy range ${BmiCalc.HEALTHY_MIN} – ${BmiCalc.HEALTHY_MAX}"
        } else null,
        emptyText = "Enter weight and height to see your BMI",
        supporting = state.bmiCategory?.let { category -> { BmiCategoryChip(category) } },
    )

    if (hasResult && state.healthyWeightRange != null) {
        BreakdownSection(
            title = "For your height",
            rows = listOf(BreakdownRow("Healthy weight", state.healthyWeightRange)),
        )
    }

    ToolInfo(
        formula = "BMI = weight (kg) ÷ height (m)²",
        notes = "Under 18.5 is underweight, 18.5 to 24.9 normal, 24.9 to 29.9 overweight and " +
                "29.9 or more obese. BMI doesn't account for muscle mass, age or build.",
    )
}

/** Category label on a tinted pill; colours come from the scheme (no hex). */
@Composable
private fun BmiCategoryChip(category: BmiCategory) {
    val scheme = MaterialTheme.colorScheme
    val extended = LocalExtendedColors.current
    val (container: Color, content: Color) = when (category) {
        BmiCategory.Underweight -> scheme.secondaryContainer to scheme.onSecondaryContainer
        BmiCategory.Normal -> scheme.tertiaryContainer to scheme.onTertiaryContainer
        BmiCategory.Overweight -> extended.accentContainer to extended.onAccentContainer
        BmiCategory.Obesity -> scheme.errorContainer to scheme.onErrorContainer
    }
    Surface(
        shape = MaterialTheme.shapes.small,
        color = container,
        contentColor = content,
    ) {
        Text(
            text = category.label,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = Spacing.m, vertical = Spacing.xs),
        )
    }
}

@ToolPreviews
@Composable
private fun BmiCalculatorContentPreview() {
    val inputState = BMIInputState().apply {
        weight.updateText("70")
        height.updateText("175")
    }
    ToolPreviewColumn {
        BmiCalculatorContent(
            inputState = inputState,
            state = BMIResultState(
                bmiValue = "22.86",
                category = BmiCategory.Normal.label,
                bmiCategory = BmiCategory.Normal,
                healthyWeightRange = "56.7 – 76.3 kg",
            ),
        )
    }
}
