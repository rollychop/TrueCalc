package com.brohit.truecalc.ui.components.tool

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.brohit.truecalc.ui.theme.Spacing

/**
 * Segmented single choice for a small set of units or modes (Years/Months, kg/lb, cm/ft).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> UnitToggle(
    options: List<T>,
    selected: T,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    label: (T) -> String = { it.toString() },
) {
    SingleChoiceSegmentedButtonRow(modifier = modifier.fillMaxWidth()) {
        options.forEachIndexed { index, option ->
            SegmentedButton(
                selected = option == selected,
                onClick = { onSelect(option) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                label = { Text(label(option), maxLines = 1, overflow = TextOverflow.Ellipsis) },
            )
        }
    }
}

@ToolPreviews
@Composable
private fun UnitTogglePreview() {
    ToolPreviewSurface {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.m)) {
            var years by remember { mutableStateOf(true) }
            UnitToggle(
                options = listOf(true, false),
                selected = years,
                onSelect = { years = it },
                label = { if (it) "Years" else "Months" },
            )
            var unit by remember { mutableStateOf("kg") }
            UnitToggle(options = listOf("kg", "lb"), selected = unit, onSelect = { unit = it })
        }
    }
}
