package com.brohit.truecalc.ui.screens.history

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brohit.truecalc.data.data_source.local.room.entity.CalculationHistory
import com.brohit.truecalc.ui.components.BackIconButton
import com.brohit.truecalc.ui.components.ConfirmationDialog
import com.brohit.truecalc.ui.components.ToolIcon
import com.brohit.truecalc.ui.navigation.AppNavigator
import com.brohit.truecalc.ui.navigation.Route
import com.brohit.truecalc.ui.navigation.withRestorationJson
import com.brohit.truecalc.ui.theme.Spacing
import com.brohit.truecalc.ui.tools.ToolRegistry
import kotlinx.coroutines.flow.collectLatest
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val GroupIconSize = 40.dp
private val EmptyIconSize = 64.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    navigator: AppNavigator,
    toolRouteFilter: String? = null,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val groupedHistory by viewModel.groupedHistory.collectAsStateWithLifecycle()
    var showClearAllDialog by remember { mutableStateOf(false) }
    var entryToDelete by remember { mutableStateOf<Long?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel.events) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is HistoryEvent.AllCleared -> {
                    snackbarHostState.showSnackbar("History cleared")
                }
            }
        }
    }

    val displayHistory = remember(groupedHistory, toolRouteFilter) {
        if (toolRouteFilter == null) groupedHistory
        else groupedHistory.filter { (_, entries) ->
            entries.any { it.toolRoute == toolRouteFilter }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(if (toolRouteFilter != null) "Calculation history" else "History")
                },
                navigationIcon = {
                    if (toolRouteFilter != null) {
                        BackIconButton(onClick = { navigator.navigateUp() })
                    }
                },
                actions = {
                    if (displayHistory.isNotEmpty()) {
                        IconButton(onClick = { showClearAllDialog = true }) {
                            Icon(Icons.Rounded.DeleteSweep, contentDescription = "Clear all history")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        if (displayHistory.isEmpty()) {
            HistoryEmptyState(modifier = Modifier.padding(innerPadding))
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(horizontal = Spacing.l, vertical = Spacing.s),
                verticalArrangement = Arrangement.spacedBy(Spacing.s)
            ) {
                displayHistory.forEach { (toolName, entries) ->
                    item(key = toolName) {
                        HistoryGroupItem(
                            toolName = toolName,
                            entries = entries,
                            isExpanded = toolRouteFilter != null || viewModel.expandedTools.contains(toolName),
                            onToggle = { viewModel.toggleExpansion(toolName) },
                            onDeleteEntry = { entryToDelete = it },
                            onRestore = { entry ->
                                val route = try {
                                    Json.decodeFromString<Route>(entry.toolRoute)
                                } catch (e: Exception) {
                                    null
                                }
                                if (route != null) {
                                    val restorationRoute = route.withRestorationJson(entry.restorationJson)
                                    navigator.navigate(restorationRoute) {
                                        popUpTo<Route.HistoryScreen> { inclusive = true }
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    ConfirmationDialog(
        show = showClearAllDialog,
        onDismissRequest = { showClearAllDialog = false },
        onConfirm = {
            viewModel.clearAllHistory()
            showClearAllDialog = false
        },
        title = "Clear All History?",
        text = "This will remove all your calculation history. This action cannot be undone."
    )

    ConfirmationDialog(
        show = entryToDelete != null,
        onDismissRequest = { entryToDelete = null },
        onConfirm = {
            entryToDelete?.let { viewModel.deleteHistory(it) }
            entryToDelete = null
        },
        title = "Delete Entry?",
        text = "Are you sure you want to delete this calculation?"
    )
}

@Composable
private fun HistoryGroupItem(
    toolName: String,
    entries: List<CalculationHistory>,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onDeleteEntry: (Long) -> Unit,
    onRestore: (CalculationHistory) -> Unit
) {
    val rotation by animateFloatAsState(if (isExpanded) 180f else 0f, label = "rotation")
    val tool = remember(entries) {
        entries.firstNotNullOfOrNull { ToolRegistry.forRouteKey(it.toolRoute) }
    }

    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClickLabel = if (isExpanded) "Collapse" else "Expand") { onToggle() }
                    .padding(Spacing.m),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (tool != null) {
                    ToolIcon(tool = tool, size = GroupIconSize)
                } else {
                    Box(
                        modifier = Modifier
                            .size(GroupIconSize)
                            .clip(MaterialTheme.shapes.medium)
                            .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.History,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.width(Spacing.m))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = toolName,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (entries.size == 1) "1 calculation" else "${entries.size} calculations",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = Icons.Rounded.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.rotate(rotation)
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(bottom = Spacing.xs)) {
                    entries.forEach { entry ->
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = Spacing.m),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                        HistoryEntryDetail(
                            entry = entry,
                            onDelete = { onDeleteEntry(entry.id) },
                            onRestore = { onRestore(entry) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryEntryDetail(
    entry: CalculationHistory,
    onDelete: () -> Unit,
    onRestore: () -> Unit
) {
    val inputs = remember(entry.inputsJson) {
        try { Json.decodeFromString<Map<String, String>>(entry.inputsJson) } catch (e: Exception) { emptyMap() }
    }
    val results = remember(entry.resultsJson) {
        try { Json.decodeFromString<Map<String, String>>(entry.resultsJson) } catch (e: Exception) { emptyMap() }
    }
    val date = remember(entry.timestamp) {
        SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(entry.timestamp))
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = Spacing.m, end = Spacing.xs, bottom = Spacing.m)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = date,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            IconButton(onClick = onRestore) {
                Icon(
                    imageVector = Icons.Rounded.Restore,
                    contentDescription = "Restore",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Rounded.DeleteOutline,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }

        Column(
            modifier = Modifier.padding(end = Spacing.s),
            verticalArrangement = Arrangement.spacedBy(Spacing.xs)
        ) {
            inputs.forEach { (key, value) ->
                Text(
                    text = "$key: $value",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            results.forEach { (key, value) ->
                Text(
                    text = "$key: $value",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun HistoryEmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(Spacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.History,
            contentDescription = null,
            modifier = Modifier.size(EmptyIconSize),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(Spacing.l))
        Text(
            text = "No calculations yet",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(Spacing.s))
        Text(
            text = "Results you calculate are saved here so you can restore them later.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
