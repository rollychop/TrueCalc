package com.brohit.truecalc.ui.components.tool

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.brohit.truecalc.domain.model.TextFieldState
import com.brohit.truecalc.ui.theme.Spacing

/** Minimum touch target height for header rows. */
private val HeaderMinHeight = 48.dp

/**
 * Collapsible block for advanced inputs (fees, compounding, step-up). Collapsed by default;
 * expansion survives configuration changes. Set [summary] to hint what's inside when collapsed
 * (e.g. "Quarterly · no fees").
 */
@Composable
fun MoreOptions(
    modifier: Modifier = Modifier,
    title: String = "More options",
    summary: String? = null,
    initiallyExpanded: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    var expanded by rememberSaveable { mutableStateOf(initiallyExpanded) }
    ExpandableBlock(
        title = title,
        summary = summary,
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier,
        content = content,
    )
}

/** Shared header + animated body used by [MoreOptions] and [ToolInfo]. */
@Composable
internal fun ExpandableBlock(
    title: String,
    summary: String?,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val rotation by animateFloatAsState(if (expanded) 180f else 0f, label = "expandIcon")
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(modifier = Modifier.animateContentSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = HeaderMinHeight)
                    .toggleable(value = expanded, role = Role.Button, onValueChange = onExpandedChange)
                    .padding(horizontal = Spacing.l, vertical = Spacing.m),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    if (summary != null && !expanded) {
                        Text(
                            text = summary,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Rounded.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.rotate(rotation),
                )
            }
            if (expanded) {
                Column(
                    modifier = Modifier.padding(start = Spacing.l, end = Spacing.l, bottom = Spacing.l),
                    verticalArrangement = Arrangement.spacedBy(Spacing.l),
                    content = content,
                )
            }
        }
    }
}

@ToolPreviews
@Composable
private fun MoreOptionsPreview() {
    ToolPreviewSurface {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.l)) {
            MoreOptions(summary = "Quarterly · no fees") {
                NumberField(state = TextFieldState(), label = "Processing fee", suffix = "%")
            }
            MoreOptions(initiallyExpanded = true) {
                NumberField(state = TextFieldState(), label = "Processing fee", suffix = "%")
            }
        }
    }
}
