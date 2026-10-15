package com.brohit.truecalc.ui.components.tool

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.brohit.truecalc.domain.model.TextFieldState
import com.brohit.truecalc.ui.theme.Spacing

/**
 * A tool section: `surfaceContainerLow` card with `shapes.large`, inner padding and spacing
 * [Spacing.l], and an optional title. Used for inputs (and by [BreakdownSection]).
 */
@Composable
fun InputSection(
    modifier: Modifier = Modifier,
    title: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(
            modifier = Modifier.padding(Spacing.l),
            verticalArrangement = Arrangement.spacedBy(Spacing.l),
        ) {
            if (title != null) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            content()
        }
    }
}

@ToolPreviews
@Composable
private fun InputSectionPreview() {
    ToolPreviewSurface {
        InputSection(title = "Loan details") {
            NumberField(
                state = TextFieldState().apply { updateText("2500000") },
                label = "Loan amount",
                currency = true,
            )
            NumberField(state = TextFieldState(), label = "Interest rate", suffix = "%")
        }
    }
}
