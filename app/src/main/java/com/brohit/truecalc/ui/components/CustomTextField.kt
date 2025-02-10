package com.brohit.truecalc.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.brohit.truecalc.domain.model.TextFieldState

@Composable
fun CustomTextField(
    state: TextFieldState,
    label: String,
    placeholder: String = "",
    modifier: Modifier = Modifier,
    maxLines: Int = 1,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    Column(modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF5D5D5D),
            modifier = Modifier.padding(bottom = 4.dp),
            fontWeight = FontWeight.SemiBold
        )
        val textStyle = MaterialTheme.typography.headlineSmall
            .copy(fontWeight = FontWeight.Bold)
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
                    else Color(0xfff4f5fe),
                    MaterialTheme.shapes.medium
                )
                .padding(12.dp),
            textStyle = textStyle,
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            keyboardOptions = keyboardOptions,
            visualTransformation = visualTransformation,
            decorationBox = @Composable { innerTextField ->
                Box(modifier = modifier) {
                    if (state.text.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = textStyle
                                .copy(color = LocalContentColor.current.copy(.75f)),
                            maxLines = maxLines
                        )
                    }
                    innerTextField()
                }
            },
            maxLines = maxLines
        )
    }

}
