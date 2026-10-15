package com.brohit.truecalc.ui.screens.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.NorthWest
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brohit.truecalc.data.data_source.local.room.entity.CalculationHistory
import com.brohit.truecalc.ui.components.BackIconButton
import com.brohit.truecalc.ui.components.ToolIcon
import com.brohit.truecalc.ui.components.tool.ToolPreviews
import com.brohit.truecalc.ui.navigation.AppNavigator
import com.brohit.truecalc.ui.navigation.Route
import com.brohit.truecalc.ui.navigation.withRestorationJson
import com.brohit.truecalc.ui.screens.favorites.FavoritesViewModel
import com.brohit.truecalc.ui.theme.Spacing
import com.brohit.truecalc.ui.theme.TrueCalcTheme
import com.brohit.truecalc.ui.tools.ToolRegistry
import com.brohit.truecalc.ui.tools.ToolSpec
import kotlinx.serialization.json.Json

@Composable
fun SearchScreen(
    navigator: AppNavigator,
    viewModel: SearchViewModel = hiltViewModel(),
    favoritesViewModel: FavoritesViewModel = hiltViewModel()
) {
    val query by viewModel.query
    val filteredTools by viewModel.filteredTools.collectAsStateWithLifecycle()
    val filteredHistory by viewModel.filteredHistory.collectAsStateWithLifecycle()
    val recentQueries by viewModel.recentQueries.collectAsStateWithLifecycle()
    val suggestedTools by viewModel.suggestedTools.collectAsStateWithLifecycle()
    val isSearching by viewModel.isSearching.collectAsStateWithLifecycle()

    SearchScreenContent(
        query = query,
        filteredTools = filteredTools,
        filteredHistory = filteredHistory,
        recentQueries = recentQueries,
        suggestedTools = suggestedTools,
        isSearching = isSearching,
        onQueryChange = { viewModel.filter(it) },
        onBack = { navigator.navigateUp() },
        onSearch = { viewModel.addRecentQuery(query.text) },
        onRecentSearchClick = { q ->
            viewModel.filter(TextFieldValue(q, TextRange(q.length)))
        },
        onRecentSearchDelete = { viewModel.removeRecentQuery(it) },
        onClearRecent = { viewModel.clearRecentQueries() },
        onToolClick = { tool ->
            viewModel.addRecentQuery(query.text)
            favoritesViewModel.logUsage(tool)
            navigator.navigate(tool.route)
        },
        onHistoryClick = { history ->
            viewModel.addRecentQuery(query.text)
            val route = try {
                Json.decodeFromString<Route>(history.toolRoute)
            } catch (e: Exception) {
                null
            }
            if (route != null) {
                navigator.navigate(route.withRestorationJson(history.restorationJson))
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreenContent(
    query: TextFieldValue,
    filteredTools: List<ToolSpec>,
    filteredHistory: List<CalculationHistory>,
    recentQueries: List<String>,
    suggestedTools: List<ToolSpec>,
    isSearching: Boolean,
    onQueryChange: (TextFieldValue) -> Unit,
    onBack: () -> Unit,
    onSearch: () -> Unit,
    onRecentSearchClick: (String) -> Unit,
    onRecentSearchDelete: (String) -> Unit,
    onClearRecent: () -> Unit,
    onToolClick: (ToolSpec) -> Unit,
    onHistoryClick: (CalculationHistory) -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            SearchTopBar(
                query = query,
                onQueryChange = onQueryChange,
                onBack = onBack,
                onSearch = onSearch
            )
        }
    ) { innerPadding ->
        if (isSearching) {
            SearchLoadingView(modifier = Modifier.padding(innerPadding))
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(bottom = Spacing.l)
            ) {
                if (query.text.isEmpty()) {
                    if (recentQueries.isNotEmpty()) {
                        item(key = "recent_header") {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = Spacing.l, end = Spacing.xs, top = Spacing.s),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                SectionLabel("Recent searches", Modifier)
                                TextButton(onClick = onClearRecent) {
                                    Text("Clear all")
                                }
                            }
                        }
                        items(recentQueries, key = { "recent_$it" }) { q ->
                            RecentSearchItem(
                                query = q,
                                onClick = { onRecentSearchClick(q) },
                                onDelete = { onRecentSearchDelete(q) }
                            )
                        }
                    }
                    toolSection("Suggested tools", "suggested", suggestedTools, onToolClick)
                } else {
                    toolSection("Tools", "tools", filteredTools, onToolClick)

                    if (filteredHistory.isNotEmpty()) {
                        item(key = "history_header") { SectionLabel("Past calculations") }
                        items(filteredHistory, key = { "history_${it.id}" }) { history ->
                            HistoryRecordItem(
                                history = history,
                                onClick = { onHistoryClick(history) }
                            )
                        }
                    }

                    if (filteredTools.isEmpty() && filteredHistory.isEmpty()) {
                        item(key = "no_results") {
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(Spacing.xxl),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "No results for “${query.text.trim()}”",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        toolSection("Suggested tools", "suggested", suggestedTools, onToolClick)
                    }
                }
                item(key = "ime_spacer") { Spacer(Modifier.imePadding()) }
            }
        }
    }
}

private fun LazyListScope.toolSection(
    title: String,
    keyPrefix: String,
    tools: List<ToolSpec>,
    onToolClick: (ToolSpec) -> Unit,
) {
    if (tools.isEmpty()) return
    item(key = "${keyPrefix}_header") { SectionLabel(title) }
    items(tools, key = { "${keyPrefix}_${it.id}" }) { tool ->
        SearchItem(tool = tool, onClick = { onToolClick(tool) })
    }
}

@Composable
private fun SectionLabel(
    text: String,
    modifier: Modifier = Modifier.padding(
        start = Spacing.l,
        end = Spacing.l,
        top = Spacing.l,
        bottom = Spacing.s
    ),
) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchTopBar(
    query: TextFieldValue,
    onQueryChange: (TextFieldValue) -> Unit,
    onBack: () -> Unit,
    onSearch: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    TopAppBar(
        navigationIcon = {
            BackIconButton(onClick = onBack)
        },
        title = {
            TextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = {
                    Text(
                        "Search ${ToolRegistry.all.size} tools…",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = Spacing.s)
                    .focusRequester(focusRequester),
                trailingIcon = {
                    if (query.text.isNotEmpty()) {
                        IconButton(onClick = { onQueryChange(TextFieldValue("")) }) {
                            Icon(imageVector = Icons.Rounded.Close, contentDescription = "Clear search")
                        }
                    }
                },
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSearch() }),
                shape = CircleShape,
                leadingIcon = {
                    Icon(imageVector = Icons.Rounded.Search, contentDescription = null)
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        // Ensure cursor is at the end if there's pre-filled text
        if (query.text.isNotEmpty() && query.selection.start == 0) {
            onQueryChange(query.copy(selection = TextRange(query.text.length)))
        }
    }
}

@Composable
fun RecentSearchItem(
    query: String,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    ListItem(
        headlineContent = { Text(query) },
        leadingContent = {
            Icon(
                Icons.Rounded.History,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingContent = {
            IconButton(onClick = onDelete) {
                Icon(Icons.Rounded.Close, contentDescription = "Remove “$query”")
            }
        },
        modifier = Modifier.clickable(onClick = onClick),
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}

@Composable
fun HistoryRecordItem(
    history: CalculationHistory,
    onClick: () -> Unit
) {
    val results = remember(history.resultsJson) {
        try {
            Json.decodeFromString<Map<String, String>>(history.resultsJson)
        } catch (e: Exception) {
            emptyMap()
        }
    }
    val resultPreview = results.values.firstOrNull() ?: ""
    val tool = remember(history.toolRoute) { ToolRegistry.forRouteKey(history.toolRoute) }

    ListItem(
        headlineContent = { Text(history.toolName) },
        supportingContent = {
            if (resultPreview.isNotEmpty()) {
                Text("Result: $resultPreview", maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        },
        leadingContent = {
            if (tool != null) {
                ToolIcon(tool = tool)
            } else {
                Icon(
                    imageVector = Icons.Rounded.History,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        trailingContent = {
            Icon(
                Icons.Rounded.NorthWest,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        modifier = Modifier.clickable(onClickLabel = "Restore", onClick = onClick),
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}

@Composable
fun SearchItem(
    tool: ToolSpec,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(tool.name) },
        supportingContent = {
            Text(
                "${tool.category.title} · ${tool.description}",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        leadingContent = { ToolIcon(tool = tool) },
        modifier = Modifier.clickable(onClick = onClick),
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}

@ToolPreviews
@Composable
private fun SearchScreenResultsPreview() {
    TrueCalcTheme {
        SearchScreenContent(
            query = TextFieldValue("loan"),
            filteredTools = ToolRegistry.search("loan"),
            filteredHistory = emptyList(),
            recentQueries = emptyList(),
            suggestedTools = emptyList(),
            isSearching = false,
            onQueryChange = {},
            onBack = {},
            onSearch = {},
            onRecentSearchClick = {},
            onRecentSearchDelete = {},
            onClearRecent = {},
            onToolClick = {},
            onHistoryClick = {}
        )
    }
}

@ToolPreviews
@Composable
private fun SearchScreenSuggestionsPreview() {
    TrueCalcTheme {
        SearchScreenContent(
            query = TextFieldValue(""),
            filteredTools = emptyList(),
            filteredHistory = emptyList(),
            recentQueries = listOf("EMI", "BMI", "Interest"),
            suggestedTools = ToolRegistry.all.take(3),
            isSearching = false,
            onQueryChange = {},
            onBack = {},
            onSearch = {},
            onRecentSearchClick = {},
            onRecentSearchDelete = {},
            onClearRecent = {},
            onToolClick = {},
            onHistoryClick = {}
        )
    }
}
