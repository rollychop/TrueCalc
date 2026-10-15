package com.brohit.truecalc.ui.components.tool

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.brohit.truecalc.ui.theme.Spacing
import kotlinx.coroutines.launch

/**
 * Single choice from a list (replaces `ExpandableSelector` / `DropdownMenu`).
 * Up to [chipThreshold] options render as a row of chips; more open a bottom sheet list from a
 * field-like row.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun <T> OptionPicker(
    label: String,
    options: List<T>,
    selected: T,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    optionLabel: (T) -> String = { it.toString() },
    chipThreshold: Int = 4,
) {
    if (options.size <= chipThreshold) {
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
                options.forEach { option ->
                    val isSelected = option == selected
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSelect(option) },
                        label = { Text(optionLabel(option)) },
                        leadingIcon = if (isSelected) {
                            { Icon(Icons.Rounded.Check, contentDescription = null) }
                        } else null,
                    )
                }
            }
        }
    } else {
        var showSheet by rememberSaveable { mutableStateOf(false) }
        val selectedLabel = optionLabel(selected)

        Surface(
            onClick = { showSheet = true },
            modifier = modifier
                .fillMaxWidth()
                .semantics { contentDescription = "$label: $selectedLabel" },
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceContainerHighest,
        ) {
            Row(
                modifier = Modifier
                    .heightIn(min = TextFieldDefaults.MinHeight)
                    .padding(horizontal = Spacing.l, vertical = Spacing.s),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = selectedLabel,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                Icon(
                    Icons.Rounded.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        if (showSheet) {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            val scope = rememberCoroutineScope()
            ModalBottomSheet(
                onDismissRequest = { showSheet = false },
                sheetState = sheetState,
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = Spacing.xl, vertical = Spacing.s),
                )
                LazyColumn(modifier = Modifier.navigationBarsPadding()) {
                    itemsIndexed(options) { _, option ->
                        val isSelected = option == selected
                        ListItem(
                            headlineContent = { Text(optionLabel(option)) },
                            trailingContent = if (isSelected) {
                                { Icon(Icons.Rounded.Check, contentDescription = "Selected") }
                            } else null,
                            colors = ListItemDefaults.colors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                            ),
                            modifier = Modifier.selectable(
                                selected = isSelected,
                                role = Role.RadioButton,
                                onClick = {
                                    onSelect(option)
                                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                                        showSheet = false
                                    }
                                },
                            ),
                        )
                    }
                }
            }
        }
    }
}

@ToolPreviews
@Composable
private fun OptionPickerPreview() {
    ToolPreviewSurface {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.l)) {
            var gender by remember { mutableStateOf("Male") }
            OptionPicker(
                label = "Gender",
                options = listOf("Male", "Female"),
                selected = gender,
                onSelect = { gender = it },
            )
            var activity by remember { mutableStateOf("Sedentary") }
            OptionPicker(
                label = "Activity level",
                options = listOf("Sedentary", "Lightly active", "Moderately active", "Very active", "Extra active"),
                selected = activity,
                onSelect = { activity = it },
            )
        }
    }
}
