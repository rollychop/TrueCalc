package com.brohit.truecalc.ui.screens.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.brohit.truecalc.domain.AppState
import com.brohit.truecalc.ui.navigation.AppNavigator
import com.brohit.truecalc.ui.navigation.FakeAppNavigator

val cardColors = listOf(
    Color(0xFFFFCDD2), Color(0xFFC8E6C9), Color(0xFFBBDEFB),
    Color(0xFFFFF9C4), Color(0xFFD1C4E9), Color(0xFFFFCCBC),
    Color(0xFFB3E5FC), Color(0xFFFFF176), Color(0xFFA5D6A7)
)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navigator: AppNavigator) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "True Calculator",
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        }
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(150.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(
                top = 8.dp,
                bottom = 64.dp,
                start = 8.dp,
                end = 8.dp
            ),
            modifier = Modifier.padding(innerPadding)
        ) {
            if (AppState.isPromotionalBannerShown) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Text(
                        text = "Promotions",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .padding(top = 8.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        items(5) { index ->
                            BannerCard(
                                title = "Promo $index",
                                description = "Special offer!",
                                backgroundColor = cardColors[index % cardColors.size]
                            )
                        }
                    }

                }
            }
            calculatorsWithCategories.forEach { category ->
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Text(
                        text = category.category,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                    )
                }
                itemsIndexed(category.calculators) { index, calculator ->
                    CalculatorCard(
                        calculator = calculator,
                        navigator = navigator,
                        backgroundColor = cardColors[index % cardColors.size],
                        modifier = Modifier.aspectRatio(1f)
                    )
                }
            }
        }
    }
}


@Composable
fun BannerCard(title: String, description: String, backgroundColor: Color) {
    Card(
        modifier = Modifier
            .size(250.dp, 120.dp)
            .shadow(4.dp, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = Color.DarkGray
            )
        }
    }
}


@Composable
fun CalculatorCard(
    calculator: Calculator,
    navigator: AppNavigator,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(12.dp))
            .clickable { navigator.navigate(calculator.route) },
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                modifier = Modifier.size(48.dp),
                imageVector = calculator.icon,
                contentDescription = calculator.name
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = calculator.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                textAlign = TextAlign.Center
            )
            Text(
                text = calculator.description,
                style = MaterialTheme.typography.bodySmall,
                color = Color.DarkGray,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview
@Composable
private fun HomeScreenPreview() {
    HomeScreen(FakeAppNavigator)
}
