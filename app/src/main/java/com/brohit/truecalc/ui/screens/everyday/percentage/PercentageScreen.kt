package com.brohit.truecalc.ui.screens.everyday.percentage

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.input.ImeAction
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brohit.truecalc.domain.calc.ChangeDirection
import com.brohit.truecalc.domain.calc.PercentageMode
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
import com.brohit.truecalc.ui.components.tool.UnitToggle
import com.brohit.truecalc.ui.navigation.AppNavigator
import com.brohit.truecalc.ui.navigation.Route
import com.brohit.truecalc.ui.tools.ToolRegistry

@Composable
fun PercentageScreen(
    navigator: AppNavigator,
    viewModel: PercentageViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ToolScreen(
        tool = checkNotNull(ToolRegistry.forRoute(Route.Calculator.Percentage())),
        navigator = navigator,
        onReset = viewModel::reset,
        shareText = state.shareText,
    ) {
        PercentageContent(inputState = viewModel.inputState, state = state)
    }
}

@Composable
fun PercentageContent(
    inputState: PercentageInputState,
    state: PercentageResultState,
) {
    val labels = inputState.mode.fieldLabels()
    InputSection {
        OptionPicker(
            label = "Calculate",
            options = PercentageMode.entries,
            selected = inputState.mode,
            onSelect = { inputState.mode = it },
            optionLabel = { it.label },
        )
        NumberField(state = inputState.first, label = labels.first, suffix = labels.firstSuffix)
        NumberField(
            state = inputState.second,
            label = labels.second,
            suffix = labels.secondSuffix,
            imeAction = ImeAction.Done,
        )
        if (inputState.mode == PercentageMode.ApplyChange) {
            UnitToggle(
                options = ChangeDirection.entries,
                selected = inputState.direction,
                onSelect = { inputState.direction = it },
                label = { it.label },
            )
        }
    }

    ResultHero(
        label = state.label,
        value = state.value,
        caption = state.caption,
        emptyText = state.emptyText,
    )

    if (state.value != null && state.rows.isNotEmpty()) {
        BreakdownSection(rows = state.rows, title = "Details")
    }

    ToolInfo(
        formula = when (inputState.mode) {
            PercentageMode.PercentOf -> "X% of Y = X ÷ 100 × Y"
            PercentageMode.WhatPercent -> "X as % of Y = X ÷ Y × 100"
            PercentageMode.Change -> "% change = (new − old) ÷ old × 100"
            PercentageMode.ApplyChange -> "Increase: Y × (1 + X ÷ 100)\nDecrease: Y × (1 − X ÷ 100)"
        },
        notes = "A positive change is an increase and a negative one a decrease. Results are " +
                "rounded to 4 decimal places for display.",
    )
}

@ToolPreviews
@Composable
private fun PercentageContentPreview() {
    val inputState = PercentageInputState().apply {
        mode = PercentageMode.Change
        first.updateText("80")
        second.updateText("100")
    }
    ToolPreviewColumn {
        PercentageContent(
            inputState = inputState,
            state = PercentageResultState(
                label = "Change from 80 to 100",
                value = "+25%",
                caption = "Increase",
                rows = listOf(BreakdownRow("Difference", "+20"), BreakdownRow("New value as % of old", "125%")),
            ),
        )
    }
}
