package com.brohit.truecalc.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material3.Badge
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.brohit.truecalc.ui.components.tool.ToolPreviewSurface
import com.brohit.truecalc.ui.components.tool.ToolPreviews
import com.brohit.truecalc.ui.theme.Spacing
import com.brohit.truecalc.ui.tools.ToolRegistry
import com.brohit.truecalc.ui.tools.ToolSpec
import com.brohit.truecalc.ui.tools.categoryColors

private val TileIconSize = 40.dp
private val ChipIconSize = 28.dp
private val ChipMaxWidth = 200.dp

/**
 * Compact grid tile for a tool: category-tinted icon, name, a "New" badge when
 * [ToolSpec.isNew], and a star that pins or unpins the tool.
 */
@Composable
fun CompactToolCard(
    tool: ToolSpec,
    isPinned: Boolean,
    onClick: () -> Unit,
    onTogglePin: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(
            modifier = Modifier.padding(start = Spacing.m, bottom = Spacing.m, end = Spacing.xs, top = Spacing.xs),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ToolIcon(tool = tool, modifier = Modifier.padding(top = Spacing.s))
                Spacer(Modifier.weight(1f))
                if (tool.isNew) NewBadge()
                PinIconButton(isPinned = isPinned, toolName = tool.name, onClick = onTogglePin)
            }
            Spacer(Modifier.size(Spacing.s))
            Text(
                text = tool.name,
                modifier = Modifier.padding(end = Spacing.s),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                minLines = 2,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/** Small pill for horizontal rows (Pinned, Recent): icon and name. */
@Composable
fun ToolChip(
    tool: ToolSpec,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.widthIn(max = ChipMaxWidth),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Row(
            modifier = Modifier.padding(start = Spacing.s, end = Spacing.m, top = Spacing.s, bottom = Spacing.s),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ToolIcon(tool = tool, size = ChipIconSize, shape = MaterialTheme.shapes.small)
            Spacer(Modifier.width(Spacing.s))
            Text(
                text = tool.name,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/** A tool's icon on its category's container colour (the only place category colour is used). */
@Composable
fun ToolIcon(
    tool: ToolSpec,
    modifier: Modifier = Modifier,
    size: Dp = TileIconSize,
    shape: Shape = MaterialTheme.shapes.medium,
) {
    val (container, content) = categoryColors(tool.category)
    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(container),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = tool.icon,
            contentDescription = null,
            tint = content,
            modifier = Modifier.size(size * 0.6f),
        )
    }
}

/** Star toggle used wherever a tool can be pinned. */
@Composable
fun PinIconButton(
    isPinned: Boolean,
    toolName: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(onClick = onClick, modifier = modifier) {
        Icon(
            imageVector = if (isPinned) Icons.Rounded.Star else Icons.Rounded.StarBorder,
            contentDescription = if (isPinned) "Unpin $toolName" else "Pin $toolName",
            tint = if (isPinned) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun NewBadge() {
    Badge(
        containerColor = MaterialTheme.colorScheme.tertiary,
        contentColor = MaterialTheme.colorScheme.onTertiary,
    ) {
        Text("New")
    }
}

@ToolPreviews
@Composable
private fun CompactToolCardPreview() {
    ToolPreviewSurface {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.m)) {
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.m)) {
                CompactToolCard(
                    tool = ToolRegistry.all[0].copy(isNew = true),
                    isPinned = true,
                    onClick = {},
                    onTogglePin = {},
                    modifier = Modifier.weight(1f),
                )
                CompactToolCard(
                    tool = ToolRegistry.all[5],
                    isPinned = false,
                    onClick = {},
                    onTogglePin = {},
                    modifier = Modifier.weight(1f),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
                ToolChip(tool = ToolRegistry.all[1], onClick = {})
                ToolChip(tool = ToolRegistry.all[6], onClick = {})
            }
        }
    }
}
