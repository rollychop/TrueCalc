package com.brohit.truecalc.ui.components.tool

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import com.brohit.truecalc.domain.model.IndianCurrencyVisualTransformation
import com.brohit.truecalc.domain.model.TextFieldState
import com.brohit.truecalc.ui.theme.Spacing

/**
 * Numeric input bound to a [TextFieldState]; the app's only number field.
 *
 * - Decimal keyboard by default ([decimal] = false for whole numbers); input is filtered to
 *   digits and a single '.', and a ',' typed by locale keyboards becomes '.'.
 * - [currency] shows "₹ 12,34,567" grouping via [IndianCurrencyVisualTransformation] (whole
 *   rupees only, like the existing fields); [prefix] is ignored then because the transformation
 *   draws the ₹ itself.
 * - [suffix] for units ("%", "yrs", "kg"). Inline error from [TextFieldState.getError], else
 *   [supportingText]. Trailing clear button while non-empty.
 */
@Composable
fun NumberField(
    state: TextFieldState,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    prefix: String? = null,
    suffix: String? = null,
    currency: Boolean = false,
    decimal: Boolean = true,
    supportingText: String? = null,
    imeAction: ImeAction = ImeAction.Next,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    enabled: Boolean = true,
) {
    val prefixColor = MaterialTheme.colorScheme.onSurfaceVariant
    val visualTransformation: VisualTransformation = remember(currency, prefixColor) {
        if (currency) IndianCurrencyVisualTransformation(prefixColor) else VisualTransformation.None
    }
    val allowDecimal = decimal && !currency
    val isError = state.showErrors()
    val errorText = state.getError()?.takeIf { it.isNotBlank() }
    val helper = errorText ?: supportingText

    TextField(
        value = state.text,
        onValueChange = { raw ->
            val filtered = filterNumberInput(raw, allowDecimal)
            if (filtered != null) state.updateText(filtered)
        },
        modifier = modifier
            .fillMaxWidth()
            .onFocusChanged { focusState ->
                state.onFocusChange(focusState.isFocused)
                if (!focusState.isFocused) state.enableShowErrors()
            },
        enabled = enabled,
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyLarge.copy(fontFeatureSettings = "tnum"),
        label = { Text(label) },
        placeholder = placeholder?.let { { Text(it) } },
        prefix = if (!currency && prefix != null) {
            { Text(prefix) }
        } else null,
        suffix = suffix?.let { { Text(it) } },
        trailingIcon = if (state.text.isNotEmpty() && enabled) {
            {
                IconButton(onClick = { state.updateText("") }) {
                    Icon(Icons.Rounded.Close, contentDescription = "Clear $label")
                }
            }
        } else null,
        supportingText = helper?.let { { Text(it) } },
        isError = isError,
        visualTransformation = visualTransformation,
        keyboardOptions = KeyboardOptions(
            keyboardType = if (allowDecimal) KeyboardType.Decimal else KeyboardType.Number,
            imeAction = imeAction,
        ),
        keyboardActions = keyboardActions,
        shape = MaterialTheme.shapes.medium,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            errorContainerColor = MaterialTheme.colorScheme.errorContainer,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            errorIndicatorColor = Color.Transparent,
        ),
    )
}

/**
 * Keeps digits and (if [allowDecimal]) one '.', mapping ',' to '.'.
 * Returns null when the edit should be rejected (a second decimal point).
 */
internal fun filterNumberInput(raw: String, allowDecimal: Boolean): String? {
    val normalized = raw.replace(',', '.')
    val kept = normalized.filter { it.isDigit() || (allowDecimal && it == '.') }
    if (kept.count { it == '.' } > 1) return null
    return kept
}

@ToolPreviews
@Composable
private fun NumberFieldPreview() {
    ToolPreviewSurface {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.m)) {
            NumberField(
                state = TextFieldState().apply { updateText("1000000") },
                label = "Loan amount",
                currency = true,
            )
            NumberField(
                state = TextFieldState().apply { updateText("8.5") },
                label = "Interest rate",
                suffix = "% p.a.",
            )
            NumberField(
                state = TextFieldState(),
                label = "Weight",
                placeholder = "e.g. 70",
                suffix = "kg",
                supportingText = "Between 20 and 300 kg",
                imeAction = ImeAction.Done,
            )
        }
    }
}
