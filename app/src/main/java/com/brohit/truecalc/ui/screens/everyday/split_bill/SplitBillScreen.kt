package com.brohit.truecalc.ui.screens.everyday.split_bill

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brohit.truecalc.ui.components.tool.BreakdownRow
import com.brohit.truecalc.ui.components.tool.BreakdownSection
import com.brohit.truecalc.ui.components.tool.ChipSelector
import com.brohit.truecalc.ui.components.tool.InputSection
import com.brohit.truecalc.ui.components.tool.MoreOptions
import com.brohit.truecalc.ui.components.tool.NumberField
import com.brohit.truecalc.ui.components.tool.ResultHero
import com.brohit.truecalc.ui.components.tool.SwitchRow
import com.brohit.truecalc.ui.components.tool.ToolInfo
import com.brohit.truecalc.ui.components.tool.ToolPreviewColumn
import com.brohit.truecalc.ui.components.tool.ToolPreviews
import com.brohit.truecalc.ui.components.tool.ToolScreen
import com.brohit.truecalc.ui.navigation.AppNavigator
import com.brohit.truecalc.ui.navigation.Route
import com.brohit.truecalc.ui.tools.ToolRegistry

@Composable
fun SplitBillScreen(
    navigator: AppNavigator,
    viewModel: SplitBillViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ToolScreen(
        tool = checkNotNull(ToolRegistry.forRoute(Route.Calculator.SplitBill())),
        navigator = navigator,
        onReset = viewModel::reset,
        shareText = state.shareText,
    ) {
        SplitBillContent(inputState = viewModel.inputState, state = state)
    }
}

@Composable
fun SplitBillContent(
    inputState: SplitBillInputState,
    state: SplitBillResultState,
) {
    InputSection {
        NumberField(state = inputState.bill, label = "Bill amount", currency = true)
        NumberField(state = inputState.people, label = "People", decimal = false, suffix = "people")
        ChipSelector(
            label = "Tip",
            presets = TipPresets,
            state = inputState.tip,
            presetLabel = { "$it%" },
            customFieldLabel = "Custom tip",
            suffix = "%",
        )
    }

    MoreOptions(summary = if (inputState.roundUp) "Rounding up each share" else "Exact shares") {
        SwitchRow(
            label = "Round up per person",
            checked = inputState.roundUp,
            onCheckedChange = { inputState.roundUp = it },
            supportingText = "Each share goes up to the next whole rupee",
        )
    }

    ResultHero(
        label = "Each person pays",
        value = state.perPerson,
        caption = state.caption,
        emptyText = state.emptyText,
    )

    if (state.perPerson != null) {
        BreakdownSection(rows = state.rows)
    }

    ToolInfo(
        formula = "Tip = bill × tip % ÷ 100\nEach person = (bill + tip) ÷ people",
        notes = "With round up on, each share goes up to the next whole rupee and the extra " +
                "collected is shown. In India a service charge on the bill is optional; tip " +
                "on the amount before any service charge.",
    )
}

@ToolPreviews
@Composable
private fun SplitBillContentPreview() {
    val inputState = SplitBillInputState().apply {
        bill.updateText("2000")
        people.updateText("4")
        tip.updateText("10")
    }
    ToolPreviewColumn {
        SplitBillContent(
            inputState = inputState,
            state = SplitBillResultState(
                perPerson = "₹550.00",
                caption = "Total ₹2,200.00 incl. ₹200.00 tip, split 4 ways",
                rows = listOf(
                    BreakdownRow("Bill", "₹2,000.00"),
                    BreakdownRow("Tip (10%)", "₹200.00"),
                    BreakdownRow("Each person pays", "₹550.00", emphasized = true),
                ),
            ),
        )
    }
}
