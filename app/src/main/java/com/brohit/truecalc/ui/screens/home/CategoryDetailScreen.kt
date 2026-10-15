package com.brohit.truecalc.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brohit.truecalc.ui.components.CompactToolCard
import com.brohit.truecalc.ui.navigation.AppNavigator
import com.brohit.truecalc.ui.screens.favorites.FavoritesViewModel
import com.brohit.truecalc.ui.utils.getToolColors
import kotlinx.serialization.json.Json

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDetailScreen(
    categoryName: String,
    navigator: AppNavigator,
    favoritesViewModel: FavoritesViewModel = hiltViewModel()
) {
    val category = calculatorsWithCategories.find { it.category == categoryName }
    val tools = category?.calculators ?: emptyList()
    val favorites by favoritesViewModel.favorites.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = categoryName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navigator.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
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
            items(tools) { calculator ->
                val (bg, iconBg, iconFg) = getToolColors(calculator.name)
                val isFav = favorites.any { it.route == Json.encodeToString(calculator.route) }
                CompactToolCard(
                    name = calculator.name,
                    description = calculator.description,
                    icon = calculator.icon,
                    iconContainerColor = iconBg,
                    iconContentColor = iconFg,
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
                                category = categoryName,
                                route = calculator.route
                            )
                        }
                    }
                )
            }
        }
    }
}
