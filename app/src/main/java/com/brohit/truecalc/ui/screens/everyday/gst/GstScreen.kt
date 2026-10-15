package com.brohit.truecalc.ui.screens.everyday.gst

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brohit.truecalc.domain.calc.GstMode
import com.brohit.truecalc.ui.components.tool.BreakdownRow
import com.brohit.truecalc.ui.components.tool.BreakdownSection
import com.brohit.truecalc.ui.components.tool.ChipSelector
import com.brohit.truecalc.ui.components.tool.InputSection
import com.brohit.truecalc.ui.components.tool.NumberField
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
fun GstScreen(
    navigator: AppNavigator,
    viewModel: GstViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ToolScreen(
        tool = checkNotNull(ToolRegistry.forRoute(Route.Calculator.Gst())),
        navigator = navigator,
        onReset = viewModel::reset,
        shareText = state.shareText,
    ) {
        GstContent(inputState = viewModel.inputState, state = state)
    }
}

@Composable
fun GstContent(
    inputState: GstInputState,
    state: GstResultState,
) {
    InputSection {
        UnitToggle(
            options = GstMode.entries,
            selected = inputState.mode,
            onSelect = { inputState.mode = it },
            label = { it.label },
        )
        NumberField(
            state = inputState.amount,
            label = if (inputState.mode == GstMode.Add) "Amount before GST" else "Amount incl. GST",
            currency = true,
        )
        ChipSelector(
            label = "GST rate",
            presets = GstRatePresets,
            state = inputState.rate,
            presetLabel = { "$it%" },
            customFieldLabel = "Custom GST rate",
            suffix = "%",
        )
    }

    ResultHero(
        label = state.heroLabel,
        value = state.heroValue,
        caption = state.caption,
        emptyText = state.emptyText,
    )

    if (state.heroValue != null) {
        BreakdownSection(rows = state.rows)
    }

    ToolInfo(
        formula = "Add GST: GST = amount × rate ÷ 100; total = amount + GST\n" +
                "Remove GST: net = amount × 100 ÷ (100 + rate); GST = amount − net",
        notes = "Within a state, GST is split equally into CGST (central) and SGST (state, or " +
                "UTGST in union territories). For a sale to another state the whole GST is " +
                "charged as IGST at the full rate instead. The rate chips follow GST 2.0 " +
                "(from 22 Sep 2025): 5% and 18% are the main slabs and 40% applies to luxury " +
                "and sin goods. Check the rate that applies to your item or service, and use " +
                "Custom for any other rate.",
    )
}

@ToolPreviews
@Composable
private fun GstContentPreview() {
    val inputState = GstInputState().apply { amount.updateText("1000") }
    ToolPreviewColumn {
        GstContent(
            inputState = inputState,
            state = GstResultState(
                heroLabel = "Total incl. GST",
                heroValue = "₹1,180.00",
                caption = "GST ₹180.00 at 18%",
                rows = listOf(
                    BreakdownRow("Net amount (before GST)", "₹1,000.00"),
                    BreakdownRow("CGST (9%)", "₹90.00"),
                    BreakdownRow("SGST (9%)", "₹90.00"),
                    BreakdownRow("Total GST (18%)", "₹180.00"),
                    BreakdownRow("Gross amount (incl. GST)", "₹1,180.00", emphasized = true),
                ),
            ),
        )
    }
}
