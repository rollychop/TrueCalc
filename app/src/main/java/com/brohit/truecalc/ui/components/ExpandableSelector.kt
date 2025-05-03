package com.brohit.truecalc.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.brohit.truecalc.ui.theme.TrueCalcTheme

@Composable
fun <T> ExpandableSelector(
    title: String,
    options: List<T>,
    selectedOption: T,
    onOptionSelected: (T) -> Unit,
    optionTitleSelector: (T) -> String = { it.toString() },
    selectedColor: Color = MaterialTheme.colorScheme.primary,
    trailingTextColor: Color = MaterialTheme.colorScheme.primary,
    containerColor: Color = Color(0xfff4f5fe)
) {
    var isVisible by remember { mutableStateOf(false) }

    val shape = MaterialTheme.shapes.medium
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                containerColor,
                shape = shape
            )
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .clickable { isVisible = !isVisible }
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF5D5D5D),
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                modifier = Modifier
            )
            Text(
                text = optionTitleSelector.invoke(selectedOption),
                color = trailingTextColor,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp),
                textAlign = TextAlign.End
            )
            Icon(
                imageVector = if (isVisible) Icons.Default.ExpandLess
                else Icons.Default.ExpandMore,
                contentDescription = "expand",
                tint = LocalContentColor.current.copy(.5f)
            )
        }

        AnimatedVisibility(isVisible) {
            Column {
                options.forEach { option ->
                    Text(
                        text = optionTitleSelector.invoke(option),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onOptionSelected(option)
                                isVisible = false
                            }
                            .padding(8.dp),
                        color = if (option == selectedOption)
                            selectedColor
                        else
                            LocalContentColor.current
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ExpandableSelectorPreview() {

    val options = listOf(
        "Sedentary", "Light",
        "Moderate",
        "Active",
        "Very Active asdf asdf asdf asdf asdf sdf"
    )
    var selected by remember { mutableStateOf(options.last()) }


    TrueCalcTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            ExpandableSelector(
                title = "Activity Level",
                options = options,
                selectedOption = selected,
                onOptionSelected = { selected = it }
            )
        }
    }
}
