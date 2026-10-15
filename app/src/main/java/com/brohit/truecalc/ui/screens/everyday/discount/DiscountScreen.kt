package com.brohit.truecalc.ui.screens.everyday.discount

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.input.ImeAction
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import com.brohit.truecalc.ui.navigation.AppNavigator
import com.brohit.truecalc.ui.navigation.Route
import com.brohit.truecalc.ui.tools.ToolRegistry

@Composable
fun DiscountScreen(
    navigator: AppNavigator,
    viewModel: DiscountViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ToolScreen(
        tool = checkNotNull(ToolRegistry.forRoute(Route.Calculator.Discount())),
        navigator = navigator,
        onReset = viewModel::reset,
        shareText = state.shareText,
    ) {
        DiscountContent(inputState = viewModel.inputState, state = state)
    }
}

@Composable
fun DiscountContent(
    inputState: DiscountInputState,
    state: DiscountResultState,
) {
    InputSection {
        NumberField(state = inputState.price, label = "Original price", currency = true)
        NumberField(
            state = inputState.discount,
            label = "Discount",
            suffix = "% off",
            imeAction = ImeAction.Done,
        )
    }

    val extras = listOfNotNull(
        inputState.secondDiscount.text.takeIf { it.isNotBlank() }?.let { "+$it% extra" },
        inputState.flatOff.text.takeIf { it.isNotBlank() }?.let { "₹$it off" },
    )
    MoreOptions(
        title = "Extra discounts",
        summary = extras.joinToString(" · ").ifEmpty { "Stacked % or flat ₹ off" },
    ) {
        NumberField(
            state = inputState.secondDiscount,
            label = "Extra discount",
            suffix = "%",
            supportingText = "Applied on the already discounted price",
        )
        NumberField(
            state = inputState.flatOff,
            label = "Flat amount off",
            currency = true,
            supportingText = "Taken off after the percentages",
            imeAction = ImeAction.Done,
        )
    }

    ResultHero(
        label = "Final price",
        value = state.finalPrice,
        caption = state.finalPrice?.let { "You save ${state.saved} (${state.effectivePercent} off)" },
        emptyText = state.emptyText,
    )

    if (state.finalPrice != null) {
        BreakdownSection(rows = state.rows)
    }

    ToolInfo(
        formula = "After discount = price × (1 − d₁ ÷ 100) × (1 − d₂ ÷ 100) − flat\n" +
                "Effective % = saved ÷ price × 100",
        notes = "Stacked discounts multiply rather than add: 20% then an extra 10% is 28% off, " +
                "not 30%. The final price never goes below ₹0.",
    )
}

@ToolPreviews
@Composable
private fun DiscountContentPreview() {
    val inputState = DiscountInputState().apply {
        price.updateText("1000")
        discount.updateText("20")
        secondDiscount.updateText("10")
    }
    ToolPreviewColumn {
        DiscountContent(
            inputState = inputState,
            state = DiscountResultState(
                finalPrice = "₹720.00",
                saved = "₹280.00",
                effectivePercent = "28%",
                rows = listOf(
                    BreakdownRow("Original price", "₹1,000.00"),
                    BreakdownRow("Discount (20%)", "− ₹200.00"),
                    BreakdownRow("Extra discount (10%)", "− ₹80.00"),
                    BreakdownRow("Final price", "₹720.00", emphasized = true),
                ),
            ),
        )
    }
}
