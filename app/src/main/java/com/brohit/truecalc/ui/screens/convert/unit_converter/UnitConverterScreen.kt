package com.brohit.truecalc.ui.screens.convert.unit_converter

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brohit.truecalc.domain.calc.UnitCategory
import com.brohit.truecalc.domain.calc.UnitConverter
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
import com.brohit.truecalc.ui.navigation.AppNavigator
import com.brohit.truecalc.ui.navigation.Route
import com.brohit.truecalc.ui.tools.ToolRegistry

@Composable
fun UnitConverterScreen(
    navigator: AppNavigator,
    viewModel: UnitConverterViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ToolScreen(
        tool = checkNotNull(ToolRegistry.forRoute(Route.Calculator.UnitConverter())),
        navigator = navigator,
        onReset = viewModel::reset,
        shareText = state.shareText,
    ) {
        UnitConverterContent(inputState = viewModel.inputState, state = state)
    }
}

@Composable
fun UnitConverterContent(
    inputState: UnitConverterInputState,
    state: UnitConverterResultState,
) {
    val category = inputState.category
    InputSection {
        OptionPicker(
            label = "Category",
            options = UnitCategory.entries,
            selected = category,
            onSelect = inputState::selectCategory,
            optionLabel = { it.label },
            chipThreshold = UnitCategory.entries.size,
        )
        NumberField(
            state = inputState.value,
            label = "Value",
            suffix = inputState.from.symbol,
            allowNegative = category == UnitCategory.Temperature,
            imeAction = ImeAction.Done,
        )
        OptionPicker(
            label = "From",
            options = category.units,
            selected = inputState.from,
            onSelect = { inputState.from = it },
            optionLabel = { it.pickerLabel() },
            chipThreshold = 0,
        )
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            FilledTonalIconButton(onClick = inputState::swap) {
                Icon(Icons.Rounded.SwapVert, contentDescription = "Swap units")
            }
        }
        OptionPicker(
            label = "To",
            options = category.units,
            selected = inputState.to,
            onSelect = { inputState.to = it },
            optionLabel = { it.pickerLabel() },
            chipThreshold = 0,
        )
    }

    ResultHero(
        label = state.label,
        value = state.value,
        caption = state.caption,
        emptyText = "Enter a value to convert",
    )

    if (state.value != null) {
        BreakdownSection(rows = state.allUnits, title = "All units")
    }

    ToolInfo(
        formula = when (category) {
            UnitCategory.Temperature -> "°F = °C × 9 ÷ 5 + 32\nK = °C + 273.15"
            else -> "Value in target = value × (from factor ÷ to factor), through the base unit " +
                    "(${baseUnitName(category)})"
        },
        notes = categoryNotes(category),
    )
}

private fun baseUnitName(category: UnitCategory): String = when (category) {
    UnitCategory.Length -> "metre"
    UnitCategory.Weight -> "kilogram"
    UnitCategory.Temperature -> "degree Celsius"
    UnitCategory.Area -> "square metre"
    UnitCategory.Volume -> "litre"
    UnitCategory.Speed -> "metre per second"
    UnitCategory.Data -> "byte"
    UnitCategory.Time -> "second"
}

private fun categoryNotes(category: UnitCategory): String = when (category) {
    UnitCategory.Length -> "Uses exact international definitions: 1 inch = 2.54 cm, 1 mile = 1,609.344 m."
    UnitCategory.Weight -> "1 lb = 0.45359237 kg exactly. 1 tola = 11.6638125 g; 1 quintal = 100 kg."
    UnitCategory.Temperature -> "Temperatures convert with an offset, not just a factor, so 0 °C is " +
            "32 °F. Negative values are allowed."
    UnitCategory.Area -> "Bigha has no single standard; it varies by state (from about 12,000 sq ft " +
            "in parts of Madhya Pradesh, 14,400 sq ft in West Bengal and Assam, to 27,000 sq ft or " +
            "more in Uttar Pradesh, Bihar and Rajasthan). This converter uses " +
            "1 bigha = ${UnitConverter.format(UnitConverter.BIGHA_SQ_FT)} sq ft; check your local " +
            "land records. 1 gaj = 1 sq yd = 9 sq ft, 1 guntha = 1,089 sq ft, 1 cent = 435.6 sq ft, " +
            "1 acre = 43,560 sq ft."
    UnitCategory.Volume -> "Cups, spoons, pints and gallons are US measures unless marked UK; " +
            "1 UK gallon = 4.54609 L."
    UnitCategory.Speed -> "1 knot = 1 nautical mile per hour = 1.852 km/h."
    UnitCategory.Data -> "KB, MB, GB and TB are decimal (1 KB = 1,000 bytes), as used by storage " +
            "makers. KiB, MiB, GiB and TiB are binary (1 KiB = 1,024 bytes), which is what many " +
            "operating systems show as \"GB\". 1 byte = 8 bits."
    UnitCategory.Time -> "Month and year are averages over the Gregorian calendar " +
            "(1 year = 365.2425 days, 1 month = 1/12 of that)."
}

@ToolPreviews
@Composable
private fun UnitConverterContentPreview() {
    val inputState = UnitConverterInputState()
    ToolPreviewColumn {
        UnitConverterContent(
            inputState = inputState,
            state = UnitConverterResultState(
                label = "1 km =",
                value = "0.62137119 mi",
                caption = "Mile",
                allUnits = listOf(
                    BreakdownRow("Metre (m)", "1,000 m"),
                    BreakdownRow("Mile (mi)", "0.62137119 mi", emphasized = true),
                ),
            ),
        )
    }
}
