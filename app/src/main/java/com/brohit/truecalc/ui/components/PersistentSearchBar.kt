package com.brohit.truecalc.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.brohit.truecalc.ui.components.tool.ToolPreviewSurface
import com.brohit.truecalc.ui.components.tool.ToolPreviews
import com.brohit.truecalc.ui.theme.Spacing

private val SearchBarHeight = 56.dp

/** Search-bar-shaped button that opens the search screen. */
@Composable
fun PersistentSearchBar(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search tools…",
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = SearchBarHeight),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Spacing.l, vertical = Spacing.m),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.width(Spacing.m))
            Text(
                text = placeholder,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@ToolPreviews
@Composable
private fun PersistentSearchBarPreview() {
    ToolPreviewSurface {
        PersistentSearchBar(onClick = {}, placeholder = "Search 8 tools…")
    }
}
