package com.brohit.truecalc.ui.screens.emi

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brohit.truecalc.data.data_source.local.room.entity.FixedCharge
import com.brohit.truecalc.ui.components.EMIPieChart
import com.brohit.truecalc.ui.components.tool.BreakdownRow
import com.brohit.truecalc.ui.components.tool.BreakdownSection
import com.brohit.truecalc.ui.components.tool.InputSection
import com.brohit.truecalc.ui.components.tool.MoreOptions
import com.brohit.truecalc.ui.components.tool.NumberField
import com.brohit.truecalc.ui.components.tool.ResultHero
import com.brohit.truecalc.ui.components.tool.ToolInfo
import com.brohit.truecalc.ui.components.tool.ToolPreviewColumn
import com.brohit.truecalc.ui.components.tool.ToolPreviews
import com.brohit.truecalc.ui.components.tool.ToolScreen
import com.brohit.truecalc.ui.components.tool.UnitToggle
import com.brohit.truecalc.ui.navigation.AppNavigator
import com.brohit.truecalc.ui.navigation.Route
import com.brohit.truecalc.ui.theme.Spacing
import com.brohit.truecalc.ui.tools.ToolRegistry

@Composable
fun EmiCalculatorScreen(
    navigator: AppNavigator,
    viewModel: EmiCalculatorViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val fixedCharges by viewModel.allFixedCharges.collectAsStateWithLifecycle()

    ToolScreen(
        tool = checkNotNull(ToolRegistry.forRoute(Route.Calculator.Emi())),
        navigator = navigator,
        onReset = viewModel::reset,
        shareText = state.shareText,
    ) {
        EmiCalculatorContent(
            inputState = viewModel.inputState,
            state = state,
            fixedCharges = fixedCharges,
            onManageCharges = { navigator.navigate(Route.EmiSettings) },
        )
    }
}

/** Inputs, result and details of the EMI tool (everything inside the [ToolScreen] frame). */
@Composable
fun EmiCalculatorContent(
    inputState: EmiCalculatorInputState,
    state: EmiCalculatorState,
    fixedCharges: List<FixedCharge>,
    onManageCharges: () -> Unit,
) {
    val termUnit = if (inputState.isTermInYears) "years" else "months"

    InputSection {
        NumberField(
            state = inputState.principal,
            label = "Loan amount",
            currency = true,
            decimal = false,
        )
        NumberField(
            state = inputState.interestRate,
            label = "Interest rate (p.a.)",
            suffix = "%",
        )
        NumberField(
            state = inputState.term,
            label = "Loan tenure",
            suffix = termUnit,
            decimal = false,
            imeAction = ImeAction.Done,
        )
        UnitToggle(
            options = listOf(true, false),
            selected = inputState.isTermInYears,
            onSelect = { inputState.isTermInYears = it },
            label = { if (it) "Years" else "Months" },
        )
    }

    MoreOptions(
        title = "Fixed charges",
        summary = if (fixedCharges.isEmpty()) "None added" else "${fixedCharges.size} added to the loan",
    ) {
        if (fixedCharges.isEmpty()) {
            Text(
                text = "Processing fees, insurance or stamp duty are added to the loan amount.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
                fixedCharges.forEach { charge ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = charge.name,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            text = if (charge.isPercentage) "${charge.amount}%" else "₹${charge.amount}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
        }
        TextButton(onClick = onManageCharges, modifier = Modifier.align(Alignment.End)) {
            Icon(Icons.Rounded.Tune, contentDescription = null)
            Text("Manage charges", modifier = Modifier.padding(start = Spacing.s))
        }
    }

    ResultHero(
        label = "Monthly EMI",
        value = if (state.hasResult) state.monthlyPayment else null,
        caption = if (state.hasResult) "for ${inputState.term.text} $termUnit" else null,
        emptyText = "Enter loan amount, rate and tenure to see your EMI",
    )

    if (state.hasResult) {
        val rows = buildList {
            add(BreakdownRow("Total interest", state.totalInterest))
            if (fixedCharges.isNotEmpty()) {
                add(BreakdownRow("Total charges", state.totalCharges))
                add(BreakdownRow("Effective interest rate", state.effectiveInterestRate))
            }
            add(BreakdownRow("Total payment", state.totalPaid, emphasized = true))
        }
        BreakdownSection(
            rows = rows,
            chart = { EMIPieChart(state = state, title = null) },
        )
    }

    ToolInfo(
        formula = "EMI = P × r × (1 + r)ⁿ ÷ ((1 + r)ⁿ − 1)",
        notes = "P is the loan amount plus fixed charges, r the monthly rate (annual rate ÷ 12 ÷ 100) " +
                "and n the tenure in months. The effective interest rate is the rate at which " +
                "this EMI would repay only the amount you receive.",
    )
}

@ToolPreviews
@Composable
private fun EmiCalculatorContentPreview() {
    val inputState = EmiCalculatorInputState().apply {
        principal.updateText("1000000")
        interestRate.updateText("10")
        term.updateText("20")
    }
    ToolPreviewColumn {
        EmiCalculatorContent(
            inputState = inputState,
            state = EmiCalculatorState(mp = 9650.22, tp = 2316052.74, ti = 1316052.74, eir = 10.0),
            fixedCharges = listOf(FixedCharge(0, "Stamp duty", 200.0, false)),
            onManageCharges = {},
        )
    }
}
