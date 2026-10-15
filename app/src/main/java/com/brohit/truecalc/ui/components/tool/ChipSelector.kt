package com.brohit.truecalc.ui.components.tool

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import com.brohit.truecalc.domain.model.TextFieldState
import com.brohit.truecalc.ui.theme.Spacing

/**
 * Preset values as chips plus a "Custom" chip that reveals a [NumberField]; the value lives in
 * [state] as text, so presets and custom entry share one source of truth (and history restore
 * just sets the text).
 *
 * A preset chip is selected when [state]'s number equals it; a value matching no preset selects
 * Custom.
 *
 * @param presets preset values as plain numbers ("5", "0.25").
 * @param presetLabel chip text for a preset ("18%").
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChipSelector(
    label: String,
    presets: List<String>,
    state: TextFieldState,
    modifier: Modifier = Modifier,
    presetLabel: (String) -> String = { it },
    customLabel: String = "Custom",
    customFieldLabel: String = label,
    suffix: String? = null,
) {
    val matchingPreset = matchingPreset(presets, state.text)
    var customChosen by rememberSaveable { mutableStateOf(false) }
    // Custom is derived from the value: a non-preset value is custom; a preset value is custom
    // only while the user is typing it in the custom field. So a Reset (or restore) to a preset
    // value always selects that preset's chip.
    val isCustom = (matchingPreset == null && (customChosen || state.text.isNotEmpty())) ||
            (customChosen && state.isFocused)

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(Spacing.s),
            verticalArrangement = Arrangement.spacedBy(Spacing.s),
        ) {
            presets.forEach { preset ->
                val selected = !isCustom && preset == matchingPreset
                FilterChip(
                    selected = selected,
                    onClick = {
                        customChosen = false
                        state.updateText(preset)
                    },
                    label = { Text(presetLabel(preset)) },
                    leadingIcon = if (selected) {
                        { Icon(Icons.Rounded.Check, contentDescription = null) }
                    } else null,
                )
            }
            FilterChip(
                selected = isCustom,
                onClick = {
                    if (!isCustom) {
                        customChosen = true
                        // Start the custom field empty so it isn't mistaken for a preset.
                        state.updateText("")
                    }
                },
                label = { Text(customLabel) },
                leadingIcon = if (isCustom) {
                    { Icon(Icons.Rounded.Check, contentDescription = null) }
                } else null,
            )
        }
        if (isCustom) {
            NumberField(
                state = state,
                label = customFieldLabel,
                suffix = suffix,
                imeAction = ImeAction.Done,
            )
        }
    }
}

/** The preset numerically equal to [text] ("18.0" matches "18"), or null. */
internal fun matchingPreset(presets: List<String>, text: String): String? {
    val current = text.toDoubleOrNull() ?: return null
    return presets.firstOrNull { it.toDoubleOrNull() == current }
}

@ToolPreviews
@Composable
private fun ChipSelectorPreview() {
    ToolPreviewSurface {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.l)) {
            ChipSelector(
                label = "GST rate",
                presets = listOf("0.25", "3", "5", "12", "18", "28"),
                state = TextFieldState().apply { updateText("18") },
                presetLabel = { "$it%" },
                suffix = "%",
            )
            ChipSelector(
                label = "Tip",
                presets = listOf("0", "5", "10", "15"),
                state = TextFieldState().apply { updateText("7.5") },
                presetLabel = { "$it%" },
                suffix = "%",
            )
        }
    }
}
