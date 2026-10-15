package com.brohit.truecalc.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brohit.truecalc.ui.components.CategorySectionHeader
import com.brohit.truecalc.ui.components.CompactToolCard
import com.brohit.truecalc.ui.components.PersistentSearchBar
import com.brohit.truecalc.ui.components.ToolChip
import com.brohit.truecalc.ui.components.tool.ToolPreviews
import com.brohit.truecalc.ui.navigation.AppNavigator
import com.brohit.truecalc.ui.navigation.Route
import com.brohit.truecalc.ui.screens.favorites.FavoritesViewModel
import com.brohit.truecalc.ui.theme.Spacing
import com.brohit.truecalc.ui.theme.TrueCalcTheme
import com.brohit.truecalc.ui.tools.ToolCategory
import com.brohit.truecalc.ui.tools.ToolRegistry
import com.brohit.truecalc.ui.tools.ToolSpec
import com.brohit.truecalc.ui.utils.MoreIcon
import com.brohit.truecalc.ui.utils.toIntent
import java.time.LocalTime

private const val APP_NAME = "True Calc"

@Composable
fun HomeScreen(
    navigator: AppNavigator,
    favoritesViewModel: FavoritesViewModel = hiltViewModel()
) {
    val pinned by favoritesViewModel.pinnedTools.collectAsStateWithLifecycle()
    val recent by favoritesViewModel.recentTools.collectAsStateWithLifecycle()

    HomeContent(
        pinned = pinned,
        recent = recent,
        onSearch = { navigator.navigate(Route.SearchScreen) },
        onOpenTool = { tool ->
            favoritesViewModel.logUsage(tool)
            navigator.navigate(tool.route)
        },
        onTogglePin = favoritesViewModel::togglePin,
        onOpenCategory = { category -> navigator.navigate(Route.CategoryDetail(category.name)) },
        actions = { MoreMenuIconButton() },
    )
}

/**
 * Stateless Home: title and greeting, search, Pinned and Recent rows (each hidden when empty),
 * then every category as a header plus a two-column grid of tool tiles.
 */
@Composable
fun HomeContent(
    pinned: List<ToolSpec>,
    recent: List<ToolSpec>,
    onSearch: () -> Unit,
    onOpenTool: (ToolSpec) -> Unit,
    onTogglePin: (ToolSpec) -> Unit,
    onOpenCategory: (ToolCategory) -> Unit,
    modifier: Modifier = Modifier,
    greeting: String = rememberGreeting(),
    actions: @Composable () -> Unit = {},
) {
    val pinnedIds = remember(pinned) { pinned.mapTo(HashSet()) { it.id } }
    val categories = remember { ToolRegistry.byCategory() }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = Spacing.l,
            end = Spacing.l,
            top = Spacing.xl,
            bottom = Spacing.xl,
        ),
        verticalArrangement = Arrangement.spacedBy(Spacing.m),
        horizontalArrangement = Arrangement.spacedBy(Spacing.m),
    ) {
        fullWidth("title") {
            HomeTitle(greeting = greeting, actions = actions)
        }
        fullWidth("search") {
            PersistentSearchBar(
                onClick = onSearch,
                placeholder = "Search ${ToolRegistry.all.size} tools…",
                modifier = Modifier.padding(bottom = Spacing.s),
            )
        }

        if (pinned.isNotEmpty()) {
            fullWidth("pinned") {
                ToolRow(title = "Pinned", tools = pinned, onOpenTool = onOpenTool)
            }
        } else {
            fullWidth("pin_hint") { PinHint() }
        }

        if (recent.isNotEmpty()) {
            fullWidth("recent") {
                ToolRow(title = "Recent", tools = recent, onOpenTool = onOpenTool)
            }
        }

        fullWidth("all_tools") {
            SectionTitle(text = "All tools", modifier = Modifier.padding(top = Spacing.s))
        }
        categories.forEach { (category, tools) ->
            fullWidth("category_${category.name}") {
                CategorySectionHeader(
                    category = category,
                    toolCount = tools.size,
                    onClick = { onOpenCategory(category) },
                )
            }
            items(tools, key = { "tool_${it.id}" }) { tool ->
                CompactToolCard(
                    tool = tool,
                    isPinned = tool.id in pinnedIds,
                    onClick = { onOpenTool(tool) },
                    onTogglePin = { onTogglePin(tool) },
                )
            }
        }
    }
}

private fun LazyGridScope.fullWidth(key: String, content: @Composable () -> Unit) {
    item(key = key, span = { GridItemSpan(maxLineSpan) }) { content() }
}

@Composable
private fun HomeTitle(greeting: String, actions: @Composable () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = Spacing.s),
        verticalAlignment = Alignment.Top,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = greeting,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = APP_NAME,
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        actions()
    }
}

@Composable
private fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

/** Titled horizontal row of tool chips. */
@Composable
private fun ToolRow(
    title: String,
    tools: List<ToolSpec>,
    onOpenTool: (ToolSpec) -> Unit,
) {
    Column {
        SectionTitle(text = title, modifier = Modifier.padding(bottom = Spacing.s))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
            items(tools, key = { it.id }) { tool ->
                ToolChip(tool = tool, onClick = { onOpenTool(tool) })
            }
        }
    }
}

/** Shown instead of the Pinned row while nothing is pinned. */
@Composable
private fun PinHint() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Rounded.StarBorder,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.width(Spacing.s))
        Text(
            text = "Tap ☆ on any tool to pin it here",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun rememberGreeting(): String = remember { greetingFor(LocalTime.now().hour) }

/** Short time-of-day greeting for [hour] (0–23). */
internal fun greetingFor(hour: Int): String = when (hour) {
    in 5..11 -> "Good morning"
    in 12..16 -> "Good afternoon"
    in 17..21 -> "Good evening"
    else -> "Hello"
}

/** Overflow menu with feedback, share and update links. */
@Composable
private fun MoreMenuIconButton() {
    var isShown by remember { mutableStateOf(false) }
    val context = LocalContext.current
    Box(contentAlignment = Alignment.Center) {
        IconButton(onClick = { isShown = true }) {
            Icon(Icons.Rounded.MoreVert, contentDescription = "More options")
        }
        DropdownMenu(
            expanded = isShown,
            onDismissRequest = { isShown = false }
        ) {
            MoreIcon.entries.forEach { type ->
                DropdownMenuItem(
                    text = { Text(type.label) },
                    onClick = {
                        context.startActivity(type.toIntent(context))
                        isShown = false
                    },
                )
            }
        }
    }
}

@ToolPreviews
@Composable
private fun HomeContentPreview() {
    TrueCalcTheme {
        Surface(color = MaterialTheme.colorScheme.surface) {
            HomeContent(
                pinned = ToolRegistry.all.take(2),
                recent = ToolRegistry.all.drop(4).take(3),
                onSearch = {},
                onOpenTool = {},
                onTogglePin = {},
                onOpenCategory = {},
                greeting = "Good morning",
            )
        }
    }
}

@ToolPreviews
@Composable
private fun HomeContentEmptyPreview() {
    TrueCalcTheme {
        Surface(color = MaterialTheme.colorScheme.surface) {
            HomeContent(
                pinned = emptyList(),
                recent = emptyList(),
                onSearch = {},
                onOpenTool = {},
                onTogglePin = {},
                onOpenCategory = {},
                greeting = "Hello",
            )
        }
    }
}
