package com.brohit.truecalc.ui.screens.everyday.age

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brohit.truecalc.ui.components.tool.BreakdownRow
import com.brohit.truecalc.ui.components.tool.BreakdownSection
import com.brohit.truecalc.ui.components.tool.DateField
import com.brohit.truecalc.ui.components.tool.InputSection
import com.brohit.truecalc.ui.components.tool.ResultHero
import com.brohit.truecalc.ui.components.tool.ToolInfo
import com.brohit.truecalc.ui.components.tool.ToolPreviewColumn
import com.brohit.truecalc.ui.components.tool.ToolPreviews
import com.brohit.truecalc.ui.components.tool.ToolScreen
import com.brohit.truecalc.ui.navigation.AppNavigator
import com.brohit.truecalc.ui.navigation.Route
import com.brohit.truecalc.ui.tools.ToolRegistry
import java.time.LocalDate

@Composable
fun AgeCalculatorScreen(
    navigator: AppNavigator,
    viewModel: AgeCalculatorViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ToolScreen(
        tool = checkNotNull(ToolRegistry.forRoute(Route.Calculator.Age())),
        navigator = navigator,
        onReset = viewModel::reset,
        shareText = state.shareText,
    ) {
        AgeCalculatorContent(inputState = viewModel.inputState, state = state)
    }
}

@Composable
fun AgeCalculatorContent(
    inputState: AgeInputState,
    state: AgeResultState,
) {
    InputSection {
        DateField(
            label = "Date of birth",
            date = inputState.dateOfBirth,
            onDateChange = { inputState.dateOfBirth = it },
        )
        DateField(
            label = "Age as of",
            date = inputState.asOf,
            onDateChange = { inputState.asOf = it },
            supportingText = if (inputState.asOf == LocalDate.now()) "Today" else null,
        )
    }

    ResultHero(
        label = "Age",
        value = state.headline,
        caption = state.caption,
        emptyText = state.emptyText,
    )

    if (state.headline != null) {
        BreakdownSection(rows = state.details, title = "Details")
        BreakdownSection(rows = state.nextBirthday, title = "Next birthday")
    }

    ToolInfo(
        formula = "Age = whole years since the last birthday, then whole months and days " +
                "since that birthday",
        notes = "Months are calendar months, so the day count depends on month lengths. For " +
                "someone born on 29 February, the birthday in non-leap years is taken as " +
                "1 March. Totals count every day between the two dates.",
    )
}

@ToolPreviews
@Composable
private fun AgeCalculatorContentPreview() {
    val inputState = AgeInputState().apply {
        dateOfBirth = LocalDate.of(1990, 5, 15)
        asOf = LocalDate.of(2026, 10, 15)
    }
    ToolPreviewColumn {
        AgeCalculatorContent(
            inputState = inputState,
            state = AgeResultState(
                headline = "36 years",
                caption = "5 months and 0 days",
                details = listOf(
                    BreakdownRow("Born on", "Tuesday"),
                    BreakdownRow("Total days", "13,302 days"),
                ),
                nextBirthday = listOf(
                    BreakdownRow("Date", "Sat, 15 May 2027"),
                    BreakdownRow("Days to go", "212 days"),
                ),
            ),
        )
    }
}
