package com.brohit.truecalc.domain.model

import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue


open class TextFieldState(
    private val validator: (String) -> Boolean = { true },
    private val errorFor: (String) -> String = { "" },
    val maxChar: Int = Int.MAX_VALUE
) {
    var text: String by mutableStateOf("")
        private set

    fun updateText(newText: String) {
        text = newText.take(maxChar)
    }


    // was the TextField ever focused
    var isFocusedDirty: Boolean by mutableStateOf(false)
    var isFocused: Boolean by mutableStateOf(false)
    private var displayErrors: Boolean by mutableStateOf(false)

    open val isValid: Boolean
        get() = validator(text)




    fun onFocusChange(focused: Boolean) {
        isFocused = focused
        if (focused) isFocusedDirty = true
    }

    fun enableShowErrors(dirty: Boolean = false) {
        if (isFocusedDirty || dirty) {
            displayErrors = true
        }
    }


    fun reset() {
        text = ""
        isFocused = false
        isFocusedDirty = false
        displayErrors = false
    }

    fun showErrors() = !isValid && displayErrors

    open fun getError(): String? {
        return if (showErrors()) {
            errorFor(text)
        } else {
            null
        }
    }
}


fun textFieldStateSaver(state: TextFieldState) = listSaver<TextFieldState, Any>(
    save = { listOf(it.text, it.isFocusedDirty) },
    restore = {
        state.apply {
            updateText(it[0] as String)
            isFocusedDirty = it[1] as Boolean
        }
    }
)

val TextFileStateSaver = textFieldStateSaver(TextFieldState())

@Composable
fun rememberTextFieldState(
    validator: (String) -> Boolean = { true },
    errorFor: (String) -> String = { "" },
    maxChar: Int = Int.MAX_VALUE
): TextFieldState {
    return rememberSaveable(saver = TextFileStateSaver) {
        TextFieldState(validator, errorFor, maxChar)
    }
}