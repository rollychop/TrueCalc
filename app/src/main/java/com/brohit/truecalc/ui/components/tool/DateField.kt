package com.brohit.truecalc.ui.components.tool

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.brohit.truecalc.common.Constant
import com.brohit.truecalc.ui.theme.Spacing
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/** "15 Oct 2026" style used for dates across the tools. */
val ToolDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM yyyy", Constant.IndiaEnglishLocale)

/** "Thu, 15 Oct 2026": a date with its weekday. */
val ToolDateWithDayFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("EEE, d MMM yyyy", Constant.IndiaEnglishLocale)

/** Formats a date for display with [ToolDateFormatter]. */
fun LocalDate.toToolText(withWeekday: Boolean = false): String =
    format(if (withWeekday) ToolDateWithDayFormatter else ToolDateFormatter)

/** The M3 date picker works in UTC-midnight milliseconds. */
internal fun LocalDate.toPickerMillis(): Long = atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

internal fun pickerMillisToDate(millis: Long): LocalDate =
    Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()

/**
 * A field-like row showing [date] (or [placeholder] when null); tapping it opens an M3
 * [DatePickerDialog]. Styled like [NumberField] / [OptionPicker] so forms line up.
 *
 * @param yearRange years the picker offers.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateField(
    label: String,
    date: LocalDate?,
    onDateChange: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Select a date",
    supportingText: String? = null,
    yearRange: IntRange = 1900..2100,
) {
    var showDialog by rememberSaveable { mutableStateOf(false) }
    val valueText = date?.toToolText(withWeekday = true) ?: placeholder

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        Surface(
            onClick = { showDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = "$label: $valueText. Tap to change" },
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceContainerHighest,
        ) {
            Row(
                modifier = Modifier
                    .heightIn(min = TextFieldDefaults.MinHeight)
                    .padding(horizontal = Spacing.l, vertical = Spacing.s),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = valueText,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (date != null) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Icon(
                    Icons.Rounded.CalendarMonth,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        if (supportingText != null) {
            Text(
                text = supportingText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = Spacing.l),
            )
        }
    }

    if (showDialog) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = (date ?: LocalDate.now()).toPickerMillis(),
            yearRange = yearRange,
        )
        DatePickerDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerState.selectedDateMillis?.let { onDateChange(pickerMillisToDate(it)) }
                        showDialog = false
                    },
                    enabled = pickerState.selectedDateMillis != null,
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Cancel") }
            },
        ) {
            DatePicker(state = pickerState)
        }
    }
}

@ToolPreviews
@Composable
private fun DateFieldPreview() {
    ToolPreviewSurface {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.m)) {
            DateField(label = "Date of birth", date = LocalDate.of(1990, 5, 15), onDateChange = {})
            DateField(
                label = "As of",
                date = null,
                onDateChange = {},
                supportingText = "Defaults to today",
            )
        }
    }
}
