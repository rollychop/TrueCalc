package com.brohit.truecalc.ui.screens.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.NorthWest
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brohit.truecalc.data.data_source.local.room.entity.CalculationHistory
import com.brohit.truecalc.domain.model.SearchRoute
import com.brohit.truecalc.ui.components.BackIconButton
import com.brohit.truecalc.ui.navigation.AppNavigator
import com.brohit.truecalc.ui.navigation.FakeAppNavigator
import com.brohit.truecalc.ui.navigation.Route
import com.brohit.truecalc.ui.screens.favorites.FavoritesViewModel
import kotlinx.serialization.json.Json

@Composable
fun SearchScreen(
    navigator: AppNavigator,
    viewModel: SearchViewModel = hiltViewModel(),
    favoritesViewModel: FavoritesViewModel = hiltViewModel()
) {
    val query by viewModel.query
    val filteredRoutes by viewModel.filteredRoutes.collectAsStateWithLifecycle()
    val filteredHistory by viewModel.filteredHistory.collectAsStateWithLifecycle()
    val recentQueries by viewModel.recentQueries.collectAsStateWithLifecycle()
    val suggestedTools by viewModel.suggestedTools.collectAsStateWithLifecycle()
    val isSearching by viewModel.isSearching.collectAsStateWithLifecycle()

    SearchScreenContent(
        query = query,
        filteredRoutes = filteredRoutes,
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
        onToolClick = { route ->
            viewModel.addRecentQuery(query.text)
            favoritesViewModel.logUsage(route.route)
            navigator.navigate(route.route)
        },
        onHistoryClick = { history ->
            viewModel.addRecentQuery(query.text)
            val route = try {
                Json.decodeFromString<Route>(history.toolRoute)
            } catch (e: Exception) {
                null
            }
            if (route != null) {
                val restorationRoute = when (route) {
                    is Route.Calculator.Emi -> route.copy(restorationJson = history.restorationJson)
                    is Route.Calculator.CompoundInterest -> route.copy(restorationJson = history.restorationJson)
                    is Route.Calculator.RecurringDeposit -> route.copy(restorationJson = history.restorationJson)
                    is Route.Calculator.Bmi -> route.copy(restorationJson = history.restorationJson)
                    is Route.Calculator.Calorie -> route.copy(restorationJson = history.restorationJson)
                    is Route.Calculator.IdealWeight -> route.copy(restorationJson = history.restorationJson)
                    else -> route
                }
                navigator.navigate(restorationRoute)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreenContent(
    query: TextFieldValue,
    filteredRoutes: List<SearchRoute>,
    filteredHistory: List<CalculationHistory>,
    recentQueries: List<String>,
    suggestedTools: List<SearchRoute>,
    isSearching: Boolean,
    onQueryChange: (TextFieldValue) -> Unit,
    onBack: () -> Unit,
    onSearch: () -> Unit,
    onRecentSearchClick: (String) -> Unit,
    onRecentSearchDelete: (String) -> Unit,
    onClearRecent: () -> Unit,
    onToolClick: (SearchRoute) -> Unit,
    onHistoryClick: (CalculationHistory) -> Unit
) {
    Scaffold(
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
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                if (query.text.isEmpty()) {
                    if (recentQueries.isNotEmpty()) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Recent Searches",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                TextButton(onClick = onClearRecent) {
                                    Text("Clear All")
                                }
                            }
                        }
                        items(recentQueries) { q ->
                            RecentSearchItem(
                                query = q,
                                onClick = { onRecentSearchClick(q) },
                                onDelete = { onRecentSearchDelete(q) }
                            )
                        }
                    }

                    if (suggestedTools.isNotEmpty()) {
                        item {
                            Text(
                                "Suggested Tools",
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        items(suggestedTools) { route ->
                            SearchItem(route = route, onClick = { onToolClick(route) })
                        }
                    }
                } else {
                    if (filteredRoutes.isNotEmpty()) {
                        item {
                            Text(
                                "Calculators",
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        items(filteredRoutes) { route ->
                            SearchItem(route = route, onClick = { onToolClick(route) })
                        }
                    }

                    if (filteredHistory.isNotEmpty()) {
                        item {
                            Text(
                                "Past Calculations",
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        items(filteredHistory) { history ->
                            HistoryRecordItem(
                                history = history,
                                onClick = { onHistoryClick(history) }
                            )
                        }
                    }

                    if (filteredRoutes.isEmpty() && filteredHistory.isEmpty()) {
                        item {
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp), contentAlignment = Alignment.Center
                            ) {
                                Text("No results found", color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
                            }
                        }
                        if (suggestedTools.isNotEmpty()) {
                            item {
                                Text(
                                    "Suggested Tools",
                                    modifier = Modifier.padding(16.dp),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            items(suggestedTools) { route ->
                                SearchItem(route = route, onClick = { onToolClick(route) })
                            }
                        }
                    }
                }
                item { Spacer(Modifier.imePadding()) }
            }
        }
    }
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
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = { Text("Search calculators...") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 8.dp)
                    .focusRequester(focusRequester),
                trailingIcon = {
                    if (query.text.isNotEmpty()) {
                        IconButton(onClick = { onQueryChange(TextFieldValue("")) }) {
                            Icon(imageVector = Icons.Default.Close, "clear")
                        }
                    }
                },
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSearch() }),
                shape = CircleShape,
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, "search")
                }
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
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
        leadingContent = { Icon(Icons.Rounded.History, null) },
        trailingContent = {
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Close, "delete", modifier = Modifier.size(18.dp))
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

    ListItem(
        headlineContent = { Text(history.toolName) },
        supportingContent = {
            if (resultPreview.isNotEmpty()) {
                Text("Result: $resultPreview", maxLines = 1)
            }
        },
        leadingContent = {
            Icon(
                imageVector = Icons.Rounded.History,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary
            )
        },
        trailingContent = { Icon(Icons.Rounded.NorthWest, null, modifier = Modifier.size(18.dp)) },
        modifier = Modifier.clickable(onClick = onClick),
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}

@Composable
fun SearchItem(
    route: SearchRoute,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(route.label, style = MaterialTheme.typography.titleMedium) },
        supportingContent = { Text(route.description, style = MaterialTheme.typography.bodySmall) },
        leadingContent = {
            Icon(
                imageVector = route.icon,
                contentDescription = route.label,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        modifier = Modifier.clickable(onClick = onClick),
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}

@Preview
@Composable
private fun SearchScreenSearchingPreview() {
    SearchScreenContent(
        query = TextFieldValue("EMI"),
        filteredRoutes = emptyList(),
        filteredHistory = emptyList(),
        recentQueries = emptyList(),
        suggestedTools = emptyList(),
        isSearching = true,
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

@Preview
@Composable
private fun SearchScreenSuggestionsPreview() {
    SearchScreenContent(
        query = TextFieldValue(""),
        filteredRoutes = emptyList(),
        filteredHistory = emptyList(),
        recentQueries = emptyList(),
        suggestedTools = listOf(
            SearchRoute(Route.Calculator.Emi(), "Financial", "EMI", "EMI Calculator", Icons.Rounded.History),
            SearchRoute(Route.Calculator.Bmi(), "Health", "BMI", "BMI Calculator", Icons.Rounded.History)
        ),
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

@Preview
@Composable
private fun SearchScreenPreview() {
    SearchScreenContent(
        query = TextFieldValue(""),
        filteredRoutes = emptyList(),
        filteredHistory = emptyList(),
        recentQueries = listOf("EMI", "BMI", "Interest"),
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
