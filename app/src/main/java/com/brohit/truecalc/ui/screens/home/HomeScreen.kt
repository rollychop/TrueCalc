package com.brohit.truecalc.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brohit.truecalc.ui.components.CompactToolCard
import com.brohit.truecalc.ui.components.PersistentSearchBar
import com.brohit.truecalc.ui.navigation.AppNavigator
import com.brohit.truecalc.ui.navigation.FakeAppNavigator
import com.brohit.truecalc.ui.navigation.Route
import com.brohit.truecalc.ui.screens.favorites.FavoritesViewModel
import com.brohit.truecalc.ui.utils.MoreIcon
import com.brohit.truecalc.ui.utils.getToolColors
import com.brohit.truecalc.ui.utils.toIntent
import kotlinx.serialization.json.Json

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navigator: AppNavigator,
    favoritesViewModel: FavoritesViewModel = hiltViewModel()
) {
    val favorites by favoritesViewModel.favorites.collectAsStateWithLifecycle()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "True Calculator",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    MoreMenuIconButton(navigator)
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Search Bar
            item(span = { GridItemSpan(maxLineSpan) }) {
                PersistentSearchBar(
                    onClick = { navigator.navigate(Route.SearchScreen) },
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            // Categories & Tools
            calculatorsWithCategories.forEach { category ->
                item(span = { GridItemSpan(maxLineSpan) }) {
                    CategoryHeader(
                        title = category.category,
                        count = category.calculators.size,
                        onViewAllClick = { 
                            navigator.navigate(Route.CategoryDetail(category.category))
                        },
                        modifier = Modifier.padding(horizontal = 0.dp)
                    )
                }

                items(category.calculators) { calculator ->
                    val (bg, iconBg) = getToolColors(calculator.name)
                    val isFav = favorites.any { it.route == Json.encodeToString(calculator.route) }
                    CompactToolCard(
                        name = calculator.name,
                        description = calculator.description,
                        icon = calculator.icon,
                        iconContainerColor = iconBg,
                        backgroundColor = bg,
                        isFavorite = isFav,
                        onClick = { 
                            favoritesViewModel.logUsage(calculator.route)
                            navigator.navigate(calculator.route) 
                        },
                        onFavoriteClick = {
                            if (isFav) {
                                favoritesViewModel.removeFavorite(calculator.route)
                            } else {
                                favoritesViewModel.addFavorite(
                                    name = calculator.name,
                                    description = calculator.description,
                                    category = category.category,
                                    route = calculator.route
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryHeader(
    title: String,
    count: Int,
    onViewAllClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "$count tools",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        TextButton(onClick = onViewAllClick) {
            Text("View All")
            Spacer(Modifier.size(4.dp))
            Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
fun MoreMenuIconButton(navigator: AppNavigator) {
    var isShown by remember { mutableStateOf(false) }
    val allTypes = MoreIcon.entries
    val context = LocalContext.current
    Box(
        contentAlignment = Alignment.Center
    ) {
        IconButton(
            onClick = {
                isShown = true
            }
        ) {
            Icon(Icons.Default.MoreVert, "more")
        }
        DropdownMenu(
            expanded = isShown,
            onDismissRequest = { isShown = false }
        ) {
            allTypes.forEach { type ->
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

@Preview
@Composable
private fun HomeScreenPreview() {
    HomeScreen(FakeAppNavigator)
}
