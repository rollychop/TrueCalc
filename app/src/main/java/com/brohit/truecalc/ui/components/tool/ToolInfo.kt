package com.brohit.truecalc.ui.components.tool

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

/**
 * Collapsible "How it's calculated" with the formula and notes, for trust.
 *
 * @param formula the formula text; use line breaks for multiple lines.
 * @param notes optional extra explanation under the formula.
 */
@Composable
fun ToolInfo(
    formula: String,
    modifier: Modifier = Modifier,
    title: String = "How it's calculated",
    notes: String? = null,
    initiallyExpanded: Boolean = false,
) {
    var expanded by rememberSaveable { mutableStateOf(initiallyExpanded) }
    ExpandableBlock(
        title = title,
        summary = null,
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier,
    ) {
        Text(
            text = formula,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        if (notes != null) {
            Text(
                text = notes,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@ToolPreviews
@Composable
private fun ToolInfoPreview() {
    ToolPreviewSurface {
        ToolInfo(
            formula = "EMI = P × r × (1 + r)ⁿ / ((1 + r)ⁿ − 1)",
            notes = "P is the loan amount, r the monthly rate (annual rate ÷ 12 ÷ 100) and n the number of months.",
            initiallyExpanded = true,
        )
    }
}
