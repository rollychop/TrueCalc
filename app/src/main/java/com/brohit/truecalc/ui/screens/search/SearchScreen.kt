package com.brohit.truecalc.ui.screens.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brohit.truecalc.domain.model.SearchRoute
import com.brohit.truecalc.ui.navigation.AppNavigator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    navigator: AppNavigator,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val query by viewModel.query
    val filteredRoutes = viewModel.allItems.collectAsStateWithLifecycle().value

    Scaffold(
        topBar = {
            SearchTopBar(
                query = query,
                onQueryChange = {
                    viewModel.filter(it)
                },
                onBack = { navigator.navigateUp() }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredRoutes) { route ->
                SearchItem(route = route, onClick = { navigator.navigate(route.route) })
            }
            item { Spacer(Modifier.imePadding()) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchTopBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onBack: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    CenterAlignedTopAppBar(
        title = {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = { Text("Search calculators...") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                trailingIcon = {
                    IconButton(onClick = {
                        if (query.isNotBlank()) {
                            onQueryChange("")
                        } else {
                            onBack()
                        }
                    }) {
                        Icon(imageVector = Icons.Default.Close, "clear")
                    }
                },
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
                shape = CircleShape,
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, "search")
                }
            )
        }
    )
    LaunchedEffect(Unit) { focusRequester.requestFocus() }
}

@Composable
fun SearchItem(
    route: SearchRoute,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = route.icon,
                contentDescription = route.label,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(16.dp))
            Column {
                Text(route.label, style = MaterialTheme.typography.titleMedium)
                Text(route.description, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
