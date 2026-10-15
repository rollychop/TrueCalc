package com.brohit.truecalc.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.brohit.truecalc.ui.components.tool.ToolPreviewSurface
import com.brohit.truecalc.ui.components.tool.ToolPreviews
import com.brohit.truecalc.ui.theme.Spacing
import com.brohit.truecalc.ui.tools.ToolCategory
import com.brohit.truecalc.ui.tools.categoryAccentColors

private val MinTouchHeight = 48.dp

/**
 * Home section header for a [ToolCategory]: tinted category icon, title, tool count and a
 * "See all" affordance. The whole row opens the category.
 */
@Composable
fun CategorySectionHeader(
    category: ToolCategory,
    toolCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val (accent, _) = categoryAccentColors(category)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = MinTouchHeight)
            .clip(MaterialTheme.shapes.small)
            .clickable(onClickLabel = "Open ${category.title}", role = Role.Button, onClick = onClick)
            .padding(horizontal = Spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(imageVector = category.icon, contentDescription = null, tint = accent)
        Spacer(Modifier.width(Spacing.s))
        Text(
            text = category.title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.width(Spacing.s))
        Text(
            text = toolCount.toString(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.weight(1f))
        Text(
            text = "See all",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
        )
    }
}

@ToolPreviews
@Composable
private fun CategorySectionHeaderPreview() {
    ToolPreviewSurface {
        CategorySectionHeader(category = ToolCategory.Finance, toolCount = 5, onClick = {})
    }
}
