package com.brohit.truecalc.ui.components.tool

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brohit.truecalc.ui.navigation.AppNavigator
import com.brohit.truecalc.ui.navigation.Route
import com.brohit.truecalc.ui.screens.favorites.FavoritesViewModel
import com.brohit.truecalc.ui.tools.ToolSpec
import kotlinx.serialization.json.Json

/**
 * [ToolScaffold] wired for a registered tool: title from [tool], back via [navigator],
 * favourite toggle through [FavoritesViewModel] (keyed by the route JSON, as everywhere else),
 * and History filtered to this tool.
 *
 * ```
 * ToolScreen(tool = ToolRegistry.byId("emi")!!, navigator = navigator,
 *            onReset = viewModel::reset, shareText = state.shareText) { … }
 * ```
 */
@Composable
fun ToolScreen(
    tool: ToolSpec,
    navigator: AppNavigator,
    modifier: Modifier = Modifier,
    onReset: (() -> Unit)? = null,
    shareText: String? = null,
    favoritesViewModel: FavoritesViewModel = hiltViewModel(),
    content: @Composable ColumnScope.() -> Unit,
) {
    val route: Route = tool.route
    val isFavorite by remember(route) { favoritesViewModel.isFavorite(route) }
        .collectAsStateWithLifecycle(initialValue = false)

    ToolScaffold(
        title = tool.name,
        onBack = { navigator.navigateUp() },
        modifier = modifier,
        isFavorite = isFavorite,
        onToggleFavorite = {
            if (isFavorite) {
                favoritesViewModel.removeFavorite(route)
            } else {
                favoritesViewModel.addFavorite(
                    name = tool.name,
                    description = tool.description,
                    category = tool.category.title,
                    route = route,
                )
            }
        },
        onHistory = {
            navigator.navigate(Route.HistoryScreen(toolRouteFilter = Json.encodeToString(route)))
        },
        onReset = onReset,
        shareText = shareText,
        content = content,
    )
}
