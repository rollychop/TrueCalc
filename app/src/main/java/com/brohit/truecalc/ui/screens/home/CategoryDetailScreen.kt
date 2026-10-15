package com.brohit.truecalc.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brohit.truecalc.ui.components.BackIconButton
import com.brohit.truecalc.ui.components.PinIconButton
import com.brohit.truecalc.ui.components.ToolIcon
import com.brohit.truecalc.ui.components.tool.ToolPreviews
import com.brohit.truecalc.ui.navigation.AppNavigator
import com.brohit.truecalc.ui.screens.favorites.FavoritesViewModel
import com.brohit.truecalc.ui.theme.Spacing
import com.brohit.truecalc.ui.theme.TrueCalcTheme
import com.brohit.truecalc.ui.tools.ToolCategory
import com.brohit.truecalc.ui.tools.ToolRegistry
import com.brohit.truecalc.ui.tools.ToolSpec

/**
 * All tools in one category.
 *
 * @param categoryKey the stable [ToolCategory] enum name (e.g. "Finance").
 */
@Composable
fun CategoryDetailScreen(
    categoryKey: String,
    navigator: AppNavigator,
    favoritesViewModel: FavoritesViewModel = hiltViewModel()
) {
    val category = remember(categoryKey) { ToolRegistry.categoryForKey(categoryKey) }
    val tools = remember(category) { category?.let(ToolRegistry::inCategory).orEmpty() }
    val pinned by favoritesViewModel.pinnedTools.collectAsStateWithLifecycle()
    val pinnedIds = remember(pinned) { pinned.mapTo(HashSet()) { it.id } }

    CategoryDetailContent(
        title = category?.title ?: "Tools",
        tools = tools,
        pinnedIds = pinnedIds,
        onBack = { navigator.navigateUp() },
        onOpenTool = { tool ->
            favoritesViewModel.logUsage(tool)
            navigator.navigate(tool.route)
        },
        onTogglePin = favoritesViewModel::togglePin,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDetailContent(
    title: String,
    tools: List<ToolSpec>,
    pinnedIds: Set<String>,
    onBack: () -> Unit,
    onOpenTool: (ToolSpec) -> Unit,
    onTogglePin: (ToolSpec) -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = { BackIconButton(onClick = onBack) },
                scrollBehavior = scrollBehavior,
            )
        }
    ) { innerPadding ->
        if (tools.isEmpty()) {
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .padding(Spacing.xl),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "No tools here yet",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .consumeWindowInsets(innerPadding),
                contentPadding = PaddingValues(
                    start = Spacing.l,
                    end = Spacing.l,
                    top = innerPadding.calculateTopPadding() + Spacing.s,
                    bottom = innerPadding.calculateBottomPadding() + Spacing.l,
                ),
                verticalArrangement = Arrangement.spacedBy(Spacing.s),
            ) {
                items(tools, key = { it.id }) { tool ->
                    ToolListRow(
                        tool = tool,
                        isPinned = tool.id in pinnedIds,
                        onClick = { onOpenTool(tool) },
                        onTogglePin = { onTogglePin(tool) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ToolListRow(
    tool: ToolSpec,
    isPinned: Boolean,
    onClick: () -> Unit,
    onTogglePin: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        ListItem(
            headlineContent = { Text(tool.name) },
            supportingContent = { Text(tool.description) },
            leadingContent = { ToolIcon(tool = tool) },
            trailingContent = {
                PinIconButton(isPinned = isPinned, toolName = tool.name, onClick = onTogglePin)
            },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        )
    }
}

@ToolPreviews
@Composable
private fun CategoryDetailContentPreview() {
    TrueCalcTheme {
        CategoryDetailContent(
            title = ToolCategory.Finance.title,
            tools = ToolRegistry.inCategory(ToolCategory.Finance),
            pinnedIds = setOf("emi"),
            onBack = {},
            onOpenTool = {},
            onTogglePin = {},
        )
    }
}
