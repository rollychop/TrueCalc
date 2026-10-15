package com.brohit.truecalc.ui.screens.health.ideal_weight

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.input.ImeAction
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brohit.truecalc.domain.calc.Gender
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
fun IdealWeightCalculatorScreen(
    navigator: AppNavigator,
    viewModel: IdealWeightCalculatorViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ToolScreen(
        tool = checkNotNull(ToolRegistry.forRoute(Route.Calculator.IdealWeight())),
        navigator = navigator,
        onReset = viewModel::reset,
        shareText = state.shareText,
    ) {
        IdealWeightCalculatorContent(inputState = viewModel.inputState, state = state)
    }
}

@Composable
fun IdealWeightCalculatorContent(
    inputState: IdealWeightInputState,
    state: IdealWeightResultState,
) {
    InputSection {
        NumberField(
            state = inputState.height,
            label = "Height",
            suffix = "cm",
            imeAction = ImeAction.Done,
        )
        OptionPicker(
            label = "Gender",
            options = Gender.entries.map { it.label },
            selected = inputState.gender,
            onSelect = { inputState.gender = it },
        )
    }

    val hasResult = state.idealWeight.isNotEmpty()
    ResultHero(
        label = "Ideal body weight",
        value = if (hasResult) "${state.idealWeight} kg" else null,
        caption = if (hasResult) "Devine formula" else null,
        emptyText = "Enter your height to see your ideal weight",
    )

    ToolInfo(
        formula = "Men: 50 kg + 2.3 kg × (inches over 5 ft)\nWomen: 45.5 kg + 2.3 kg × (inches over 5 ft)",
        notes = "Height is converted from cm to inches (÷ 2.54). Heights under 5 ft use the base " +
                "weight. It's an estimate that doesn't account for build or muscle mass.",
    )
}

@ToolPreviews
@Composable
private fun IdealWeightCalculatorContentPreview() {
    val inputState = IdealWeightInputState().apply { height.updateText("180") }
    ToolPreviewColumn {
        IdealWeightCalculatorContent(
            inputState = inputState,
            state = IdealWeightResultState(idealWeight = "74.99"),
        )
    }
}
