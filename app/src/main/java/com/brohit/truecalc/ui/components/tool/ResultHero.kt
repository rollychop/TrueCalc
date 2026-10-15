package com.brohit.truecalc.ui.components.tool

import android.os.Build
import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.onLongClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import com.brohit.truecalc.ui.theme.Spacing
import com.brohit.truecalc.ui.theme.numberDisplay

/**
 * The big result number. Shows [emptyText] while [value] is null/blank. Long-press copies the
 * value to the clipboard.
 *
 * @param label what the number is ("Monthly EMI")
 * @param caption a quiet line under the number ("for 240 months", "Normal weight")
 * @param supporting optional content under the number and caption (e.g. a category chip);
 *   shown only when there is a value.
 */
@Suppress("DEPRECATION") // LocalClipboardManager: simple, synchronous and present in the BOM.
@Composable
fun ResultHero(
    label: String,
    value: String?,
    modifier: Modifier = Modifier,
    caption: String? = null,
    emptyText: String = "Enter the details above to see the result",
    supporting: (@Composable () -> Unit)? = null,
) {
    val clipboard = LocalClipboardManager.current
    val haptics = LocalHapticFeedback.current
    val context = LocalContext.current
    val hasValue = !value.isNullOrBlank()

    val copy: () -> Unit = {
        if (hasValue) {
            clipboard.setText(AnnotatedString(value.orEmpty()))
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            // Android 13+ shows its own clipboard confirmation.
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                Toast.makeText(context, "Copied $label", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(hasValue, value) {
                detectTapGestures(onLongPress = { copy() })
            }
            .semantics {
                if (hasValue) onLongClick(label = "Copy") { copy(); true }
            },
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
    ) {
        Column(
            modifier = Modifier
                .animateContentSize()
                .padding(horizontal = Spacing.xl, vertical = Spacing.xl),
            verticalArrangement = Arrangement.spacedBy(Spacing.xs),
        ) {
            Text(text = label, style = MaterialTheme.typography.labelLarge)
            if (hasValue) {
                Text(text = value.orEmpty(), style = MaterialTheme.typography.numberDisplay)
                if (caption != null) {
                    Text(text = caption, style = MaterialTheme.typography.bodyMedium)
                }
                if (supporting != null) {
                    Box(modifier = Modifier.padding(top = Spacing.s)) { supporting() }
                }
            } else {
                Text(text = emptyText, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@ToolPreviews
@Composable
private fun ResultHeroPreview() {
    ToolPreviewSurface {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.l)) {
            ResultHero(label = "Monthly EMI", value = "₹9,650.22", caption = "for 240 months")
            ResultHero(label = "Monthly EMI", value = null, emptyText = "Enter amount to see result")
        }
    }
}
