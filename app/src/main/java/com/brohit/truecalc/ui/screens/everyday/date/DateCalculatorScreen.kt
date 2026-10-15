package com.brohit.truecalc.ui.screens.everyday.date

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.input.ImeAction
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brohit.truecalc.domain.calc.DateCalcMode
import com.brohit.truecalc.domain.calc.DateUnit
import com.brohit.truecalc.ui.components.tool.BreakdownRow
import com.brohit.truecalc.ui.components.tool.BreakdownSection
import com.brohit.truecalc.ui.components.tool.DateField
import com.brohit.truecalc.ui.components.tool.InputSection
import com.brohit.truecalc.ui.components.tool.MoreOptions
import com.brohit.truecalc.ui.components.tool.NumberField
import com.brohit.truecalc.ui.components.tool.ResultHero
import com.brohit.truecalc.ui.components.tool.SwitchRow
import com.brohit.truecalc.ui.components.tool.ToolInfo
import com.brohit.truecalc.ui.components.tool.ToolPreviewColumn
import com.brohit.truecalc.ui.components.tool.ToolPreviews
import com.brohit.truecalc.ui.components.tool.ToolScreen
import com.brohit.truecalc.ui.components.tool.UnitToggle
import com.brohit.truecalc.ui.navigation.AppNavigator
import com.brohit.truecalc.ui.navigation.Route
import com.brohit.truecalc.ui.tools.ToolRegistry
import java.time.LocalDate

@Composable
fun DateCalculatorScreen(
    navigator: AppNavigator,
    viewModel: DateCalculatorViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ToolScreen(
        tool = checkNotNull(ToolRegistry.forRoute(Route.Calculator.DateCalc())),
        navigator = navigator,
        onReset = viewModel::reset,
        shareText = state.shareText,
    ) {
        DateCalculatorContent(inputState = viewModel.inputState, state = state)
    }
}

@Composable
fun DateCalculatorContent(
    inputState: DateCalculatorInputState,
    state: DateCalculatorResultState,
) {
    InputSection {
        UnitToggle(
            options = DateCalcMode.entries,
            selected = inputState.mode,
            onSelect = { inputState.mode = it },
            label = { it.label },
        )
        DateField(
            label = "Start date",
            date = inputState.start,
            onDateChange = { inputState.start = it },
        )
        when (inputState.mode) {
            DateCalcMode.Between -> {
                DateField(
                    label = "End date",
                    date = inputState.end,
                    onDateChange = { inputState.end = it },
                )
            }
            DateCalcMode.AddSubtract -> {
                UnitToggle(
                    options = listOf(false, true),
                    selected = inputState.subtract,
                    onSelect = { inputState.subtract = it },
                    label = { if (it) "Subtract" else "Add" },
                )
                NumberField(
                    state = inputState.amount,
                    label = "Amount",
                    decimal = false,
                    suffix = inputState.unit.label.lowercase(),
                    imeAction = ImeAction.Done,
                )
                UnitToggle(
                    options = DateUnit.entries,
                    selected = inputState.unit,
                    onSelect = { inputState.unit = it },
                    label = { it.label },
                )
            }
        }
    }

    if (inputState.mode == DateCalcMode.Between) {
        MoreOptions(summary = if (inputState.includeEndDate) "End date included" else "End date not counted") {
            SwitchRow(
                label = "Include end date",
                checked = inputState.includeEndDate,
                onCheckedChange = { inputState.includeEndDate = it },
                supportingText = "Count both the first and the last day",
            )
        }
    }

    ResultHero(
        label = state.heroLabel,
        value = state.heroValue,
        caption = state.caption,
        emptyText = state.emptyText,
    )

    if (state.heroValue != null) {
        BreakdownSection(rows = state.rows, title = "Details")
    }

    ToolInfo(
        formula = when (inputState.mode) {
            DateCalcMode.Between -> "Days = end date − start date (+1 if the end date is included)\n" +
                    "Working days = Monday to Friday in that span"
            DateCalcMode.AddSubtract -> "Result = start date ± amount (days, weeks, months or years)"
        },
        notes = when (inputState.mode) {
            DateCalcMode.Between -> "By default the end date isn't counted, so 1 to 2 January is " +
                    "1 day. Working days skip Saturdays and Sundays only; public holidays aren't " +
                    "excluded."
            DateCalcMode.AddSubtract -> "Adding months or years keeps the day of the month where " +
                    "possible; if that day doesn't exist the result is the last day of the month " +
                    "(31 January + 1 month = 28 or 29 February)."
        },
    )
}

@ToolPreviews
@Composable
private fun DateCalculatorContentPreview() {
    val inputState = DateCalculatorInputState().apply {
        start = LocalDate.of(2026, 1, 1)
        end = LocalDate.of(2026, 12, 31)
    }
    ToolPreviewColumn {
        DateCalculatorContent(
            inputState = inputState,
            state = DateCalculatorResultState(
                heroValue = "364 days",
                caption = "Thu, 1 Jan 2026 to Thu, 31 Dec 2026",
                rows = listOf(
                    BreakdownRow("Weeks and days", "52 weeks, 0 days"),
                    BreakdownRow("Working days (Mon–Fri)", "260 days", emphasized = true),
                ),
            ),
        )
    }
}
