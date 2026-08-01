package com.brohit.truecalc.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.brohit.truecalc.domain.model.IndianCurrencyVisualTransformation
import com.brohit.truecalc.domain.model.TextFieldState

@Composable
fun CustomTextField(
    state: TextFieldState,
    label: String,
    modifier: Modifier = Modifier,
    trailingContent: @Composable (() -> Unit)? = null,
    placeholder: String = "",
    maxLines: Int = 1,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    val prefixColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
    val transformedVisualTransformation = remember(visualTransformation, prefixColor) {
        if (visualTransformation == IndianCurrencyVisualTransformation) {
            IndianCurrencyVisualTransformation(prefixColor)
        } else {
            visualTransformation
        }
    }
    Column(modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
        )
        val textStyle = MaterialTheme.typography.bodyLarge
            .copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        BasicTextField(
            value = state.text,
            onValueChange = { state.updateText(it.take(state.maxChar)) },
            modifier = Modifier
                .onFocusChanged { focusState ->
                    state.onFocusChange(focusState.isFocused)
                    if (!focusState.isFocused) {
                        state.enableShowErrors()
                    }
                }
                .background(
                    if (state.showErrors()) MaterialTheme.colorScheme.errorContainer
                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    MaterialTheme.shapes.medium
                )
                .padding(horizontal = 16.dp, vertical = 12.dp),
            textStyle = textStyle,
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            keyboardOptions = keyboardOptions,
            visualTransformation = transformedVisualTransformation,
            decorationBox = @Composable { innerTextField ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        if (state.text.isEmpty()) {
                            Text(
                                text = placeholder,
                                style = textStyle
                                    .copy(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(.7f)),
                                maxLines = maxLines,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                        innerTextField()
                    }
                    trailingContent?.let {
                        Box(
                            modifier = Modifier.height(32.dp),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            trailingContent.invoke()
                        }
                    }
                }
            },
            maxLines = maxLines
        )
    }

}
