package com.brohit.truecalc.ui.components.tool

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.brohit.truecalc.ui.theme.Spacing
import com.brohit.truecalc.ui.theme.numberMedium

/** One labelled value in a [BreakdownSection]. [emphasized] rows (totals) use stronger text. */
@Immutable
data class BreakdownRow(
    val label: String,
    val value: String,
    val emphasized: Boolean = false,
)

/**
 * Result details under the [ResultHero]: an optional chart followed by label/value rows,
 * in a `surfaceContainerLow` section.
 */
@Composable
fun BreakdownSection(
    rows: List<BreakdownRow>,
    modifier: Modifier = Modifier,
    title: String? = "Breakdown",
    chart: (@Composable () -> Unit)? = null,
) {
    InputSection(modifier = modifier, title = title) {
        if (chart != null) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                chart()
            }
        }
        Column {
            rows.forEachIndexed { index, row ->
                BreakdownRowItem(row)
                if (index != rows.lastIndex) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
            }
        }
    }
}

/** Builds plain rows from `label to value` pairs. */
fun breakdownRows(vararg rows: Pair<String, String>): List<BreakdownRow> =
    rows.map { (label, value) -> BreakdownRow(label, value) }

@Composable
private fun BreakdownRowItem(row: BreakdownRow) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.m),
        horizontalArrangement = Arrangement.spacedBy(Spacing.l),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = row.label,
            style = if (row.emphasized) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium,
            color = if (row.emphasized) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = row.value,
            style = MaterialTheme.typography.numberMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.End,
        )
    }
}

@ToolPreviews
@Composable
private fun BreakdownSectionPreview() {
    ToolPreviewSurface {
        BreakdownSection(
            rows = listOf(
                BreakdownRow("Principal", "₹10,00,000.00"),
                BreakdownRow("Total interest", "₹13,16,052.74"),
                BreakdownRow("Total payment", "₹23,16,052.74", emphasized = true),
            ),
        )
    }
}
