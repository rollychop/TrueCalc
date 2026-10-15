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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.brohit.truecalc.common.NumberInput
import com.brohit.truecalc.domain.model.TextFieldState
import com.brohit.truecalc.ui.theme.Spacing

/**
 * Numeric input bound to a [TextFieldState]; the app's only number field.
 *
 * - Decimal keyboard by default ([decimal] = false for whole numbers); input is filtered to
 *   digits and a single '.', and a ',' typed by locale keyboards becomes '.'.
 * - [currency] shows "₹ 12,34,567.50" with Indian grouping via
 *   [IndianGroupingVisualTransformation], allowing up to [CURRENCY_DECIMALS] decimals (paise);
 *   pass [decimal] = false for whole rupees. [prefix] is ignored then because the
 *   transformation draws the ₹ itself.
 * - [suffix] for units ("%", "yrs", "kg"). Inline error from [TextFieldState.getError], else
 *   [supportingText]. Trailing clear button while non-empty.
 * - [allowNegative] keeps a single leading '-' (e.g. temperatures) and shows a "±" button,
 *   since many numeric keyboards have no minus key; off by default.
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
    allowNegative: Boolean = false,
) {
    val prefixColor = MaterialTheme.colorScheme.onSurfaceVariant
    val visualTransformation: VisualTransformation = remember(currency, prefixColor) {
        if (currency) IndianGroupingVisualTransformation(prefixColor = prefixColor) else VisualTransformation.None
    }
    val allowDecimal = decimal
    val maxDecimals = if (currency) CURRENCY_DECIMALS else Int.MAX_VALUE
    val signToggle = allowNegative && !currency
    val isError = state.showErrors()
    val errorText = state.getError()?.takeIf { it.isNotBlank() }
    val helper = errorText ?: supportingText

    TextField(
        value = state.text,
        onValueChange = { raw ->
            val filtered = NumberInput.filter(raw, allowDecimal, signToggle, maxDecimals)
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
        leadingIcon = if (signToggle) {
            {
                IconButton(
                    onClick = { state.updateText(NumberInput.toggleSign(state.text)) },
                    enabled = enabled,
                    modifier = Modifier.semantics { contentDescription = "Toggle sign" },
                ) {
                    Text("±", style = MaterialTheme.typography.titleMedium)
                }
            }
        } else null,
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

/** Decimal places allowed in [NumberField]'s currency mode (paise). */
const val CURRENCY_DECIMALS = 2

@ToolPreviews
@Composable
private fun NumberFieldPreview() {
    ToolPreviewSurface {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.m)) {
            NumberField(
                state = TextFieldState().apply { updateText("1000000.5") },
                label = "Loan amount",
                currency = true,
            )
            NumberField(
                state = TextFieldState().apply { updateText("-40") },
                label = "Temperature",
                suffix = "°C",
                allowNegative = true,
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
