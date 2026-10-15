package com.brohit.truecalc.ui.screens.recurring_deposit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.input.ImeAction
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brohit.truecalc.domain.calc.CompoundingFrequency
import com.brohit.truecalc.domain.calc.RecurringInterval
import com.brohit.truecalc.domain.model.InvestmentResultState
import com.brohit.truecalc.ui.components.InvestmentBreakdownChart
import com.brohit.truecalc.ui.components.tool.BreakdownRow
import com.brohit.truecalc.ui.components.tool.BreakdownSection
import com.brohit.truecalc.ui.components.tool.InputSection
import com.brohit.truecalc.ui.components.tool.MoreOptions
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
import com.brohit.truecalc.ui.screens.compund_interest.CalculationType
import com.brohit.truecalc.ui.screens.compund_interest.getLabelFor
import com.brohit.truecalc.ui.tools.ToolRegistry

@Composable
fun RecurringDepositScreen(
    navigator: AppNavigator,
    viewModel: RecurringDepositViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ToolScreen(
        tool = checkNotNull(ToolRegistry.forRoute(Route.Calculator.RecurringDeposit())),
        navigator = navigator,
        onReset = viewModel::reset,
        shareText = state.shareText,
    ) {
        RecurringDepositContent(inputState = viewModel.inputState, state = state)
    }
}

@Composable
fun RecurringDepositContent(
    inputState: RecurringDepositInputState,
    state: InvestmentResultState,
) {
    val timeUnit = if (inputState.isTimeInYears) "years" else "months"

    InputSection {
        NumberField(
            state = inputState.principal,
            label = "Installment amount",
            currency = true,
        )
        OptionPicker(
            label = "Installment frequency",
            options = RecurringInterval.entries,
            selected = inputState.recurringInterval,
            onSelect = { inputState.recurringInterval = it },
            optionLabel = { it.label },
        )
        NumberField(
            state = inputState.interestRate,
            label = "Interest rate (p.a.)",
            suffix = "%",
        )
        NumberField(
            state = inputState.time,
            label = "Time period",
            suffix = timeUnit,
            imeAction = ImeAction.Done,
        )
        UnitToggle(
            options = listOf(true, false),
            selected = inputState.isTimeInYears,
            onSelect = { inputState.isTimeInYears = it },
            label = { if (it) "Years" else "Months" },
        )
    }

    MoreOptions(
        summary = "${inputState.compoundingFrequency.getLabelFor(CalculationType.FIXED)} interest payout",
    ) {
        OptionPicker(
            label = "Interest payout",
            options = CompoundingFrequency.entries,
            selected = inputState.compoundingFrequency,
            onSelect = { inputState.compoundingFrequency = it },
            optionLabel = { it.getLabelFor(CalculationType.FIXED) },
        )
    }

    ResultHero(
        label = "Maturity amount",
        value = if (state.hasResult) state.finalAmount else null,
        caption = if (state.hasResult) "Interest earned ${state.interestEarned}" else null,
        emptyText = "Enter installment, rate and time to see the maturity amount",
    )

    if (state.hasResult) {
        BreakdownSection(
            rows = listOf(
                BreakdownRow("Total invested", state.totalInvestment),
                BreakdownRow("Interest earned", state.interestEarned),
                BreakdownRow("Maturity amount", state.finalAmount, emphasized = true),
            ),
            chart = {
                InvestmentBreakdownChart(
                    totalAmount = state.amount,
                    interest = state.interest,
                    principal = state.principal,
                    title = null,
                )
            },
        )
    }

    ToolInfo(
        formula = "M = I × ((1 + R/n)^(n × T) − 1) ÷ (1 − (1 + R/n)^(−n/p))",
        notes = "I is the installment, R the annual rate ÷ 100, n the interest payouts per year, " +
                "p the installments per year and T the time in years. Each installment earns " +
                "compound interest until maturity.",
    )
}

@ToolPreviews
@Composable
private fun RecurringDepositContentPreview() {
    val inputState = RecurringDepositInputState().apply {
        principal.updateText("1000")
        interestRate.updateText("7.5")
        time.updateText("1")
    }
    ToolPreviewColumn {
        RecurringDepositContent(
            inputState = inputState,
            state = InvestmentResultState(amount = 12498.0, interest = 498.0, principal = 12000.0),
        )
    }
}

@ToolPreviews
@Composable
private fun RecurringDepositContentEmptyPreview() {
    ToolPreviewColumn {
        RecurringDepositContent(
            inputState = RecurringDepositInputState(),
            state = InvestmentResultState(),
        )
    }
}
