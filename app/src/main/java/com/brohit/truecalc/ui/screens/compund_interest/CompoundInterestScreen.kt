package com.brohit.truecalc.ui.screens.compund_interest

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.input.ImeAction
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brohit.truecalc.domain.calc.CompoundingFrequency
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
import com.brohit.truecalc.ui.tools.ToolRegistry

/** Compound interest, simple interest and fixed deposit share this screen. */
@Composable
fun CompoundInterestScreen(
    navigator: AppNavigator,
    viewModel: CompoundInterestViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val inputState = viewModel.inputState
    val tool = ToolRegistry.forRoute(Route.Calculator.CompoundInterest(inputState.calculationType))
        ?: checkNotNull(ToolRegistry.byId("compound_interest"))

    ToolScreen(
        tool = tool,
        navigator = navigator,
        onReset = viewModel::reset,
        shareText = state.shareText,
    ) {
        CompoundInterestContent(inputState = inputState, state = state)
    }
}

/** Short labels for the calculation switcher. */
private fun shortLabel(type: String): String = when (type) {
    CalculationType.COMPOUND -> "Compound"
    CalculationType.SIMPLE -> "Simple"
    CalculationType.FIXED -> "FD"
    else -> type
}

@Composable
fun CompoundInterestContent(
    inputState: CompoundInterestInputState,
    state: InvestmentResultState,
) {
    val type = inputState.calculationType
    val timeUnit = if (inputState.isTimeInYears) "years" else "months"

    InputSection {
        UnitToggle(
            options = CalculationType.entries,
            selected = type,
            onSelect = inputState::onCalculationTypeChange,
            label = ::shortLabel,
        )
        NumberField(
            state = inputState.principal,
            label = if (type == CalculationType.FIXED) "Deposit amount" else "Principal amount",
            prefix = "₹",
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

    if (type != CalculationType.SIMPLE) {
        val frequencyTitle =
            if (type == CalculationType.FIXED) "Interest payout frequency" else "Compounding frequency"
        MoreOptions(
            summary = inputState.compoundingFrequency.getLabelFor(type) +
                    if (type == CalculationType.FIXED) " payout" else " compounding",
        ) {
            OptionPicker(
                label = frequencyTitle,
                options = CompoundingFrequency.entries,
                selected = inputState.compoundingFrequency,
                onSelect = { inputState.compoundingFrequency = it },
                optionLabel = { it.getLabelFor(type) },
            )
        }
    }

    ResultHero(
        label = if (type == CalculationType.FIXED) "Maturity amount" else "Final amount",
        value = if (state.hasResult) state.finalAmount else null,
        caption = if (state.hasResult) "Interest earned ${state.interestEarned}" else null,
        emptyText = "Enter amount, rate and time to see the result",
    )

    if (state.hasResult) {
        BreakdownSection(
            rows = listOf(
                BreakdownRow("Total invested", state.totalInvestment),
                BreakdownRow("Interest earned", state.interestEarned),
                BreakdownRow(
                    if (type == CalculationType.FIXED) "Maturity amount" else "Final amount",
                    state.finalAmount,
                    emphasized = true,
                ),
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

    when (type) {
        CalculationType.SIMPLE -> ToolInfo(
            formula = "Interest = P × R × T\nAmount = P + Interest",
            notes = "P is the principal, R the annual rate ÷ 100 and T the time in years " +
                    "(months ÷ 12). Interest is earned only on the principal.",
        )

        else -> ToolInfo(
            formula = "A = P × (1 + R/n)^(n × T)",
            notes = "P is the principal, R the annual rate ÷ 100, n how many times a year interest " +
                    "is compounded and T the time in years (months ÷ 12). Interest = A − P.",
        )
    }
}

@ToolPreviews
@Composable
private fun CompoundInterestContentPreview() {
    val inputState = CompoundInterestInputState(CalculationType.FIXED).apply {
        principal.updateText("100000")
        interestRate.updateText("7")
        time.updateText("5")
    }
    ToolPreviewColumn {
        CompoundInterestContent(
            inputState = inputState,
            state = InvestmentResultState(amount = 141478.30, interest = 41478.30, principal = 100000.0),
        )
    }
}
