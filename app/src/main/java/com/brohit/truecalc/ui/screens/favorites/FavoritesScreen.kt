package com.brohit.truecalc.ui.screens.favorites

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.StarOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brohit.truecalc.ui.components.CompactToolCard
import com.brohit.truecalc.ui.navigation.AppNavigator
import com.brohit.truecalc.ui.navigation.Route
import com.brohit.truecalc.ui.screens.home.Calculator
import com.brohit.truecalc.ui.screens.home.toCalculator
import com.brohit.truecalc.ui.utils.getToolColors
import kotlinx.serialization.json.Json

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    navigator: AppNavigator,
    viewModel: FavoritesViewModel = hiltViewModel()
) {
    val favorites by viewModel.favorites.collectAsStateWithLifecycle()
    val suggestions by viewModel.suggestions.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Favorites",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        }
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (favorites.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    FavoritesEmptyState()
                }

                if (suggestions.isNotEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Text(
                            text = "Suggested for you",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
                        )
                    }

                    items(suggestions) { calculator ->
                        val (bg, iconBg) = getToolColors(calculator.name)
                        CompactToolCard(
                            name = calculator.name,
                            description = calculator.description,
                            icon = calculator.icon,
                            iconContainerColor = iconBg,
                            backgroundColor = bg,
                            onClick = { 
                                viewModel.logUsage(calculator.route)
                                navigator.navigate(calculator.route) 
                            },
                            isFavorite = false,
                            onFavoriteClick = { 
                                viewModel.addFavorite(
                                    calculator.name, 
                                    calculator.description, 
                                    "", 
                                    calculator.route
                                ) 
                            }
                        )
                    }
                }
            } else {
                items(favorites) { fav ->
                    val route = try {
                        Json.decodeFromString<Route>(fav.route)
                    } catch (e: Exception) {
                        null
                    }
                    val calculator = route?.toCalculator()
                    if (calculator != null) {
                        val (bg, iconBg) = getToolColors(calculator.name)
                        CompactToolCard(
                            name = calculator.name,
                            description = calculator.description,
                            icon = calculator.icon,
                            iconContainerColor = iconBg,
                            backgroundColor = bg,
                            onClick = { 
                                viewModel.logUsage(calculator.route)
                                navigator.navigate(calculator.route) 
                            },
                            isFavorite = true,
                            onFavoriteClick = { viewModel.removeFavorite(route) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FavoritesEmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.StarOutline,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No favorites yet",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Start adding tools you use often to find them here quickly.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}
