package com.brohit.truecalc.ui.screens.health.ideal_weight

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.History
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brohit.truecalc.ui.components.BackIconButton
import com.brohit.truecalc.ui.components.CustomTextField
import com.brohit.truecalc.ui.components.ExpandableSelector
import com.brohit.truecalc.ui.navigation.AppNavigator
import com.brohit.truecalc.ui.navigation.FakeAppNavigator
import com.brohit.truecalc.ui.navigation.Route
import com.brohit.truecalc.ui.theme.TrueCalcTheme
import kotlinx.serialization.json.Json

@Composable
fun IdealWeightCalculatorScreen(
    navigator: AppNavigator,
    viewModel: IdealWeightCalculatorViewModel = hiltViewModel()
) {
    IdealWeightCalculatorUI(
        navigator = navigator,
        inputState = viewModel.inputState,
        state = viewModel.state.collectAsStateWithLifecycle().value
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IdealWeightCalculatorUI(
    navigator: AppNavigator,
    inputState: IdealWeightInputState,
    state: IdealWeightResultState
) {
    val genders = listOf("Male", "Female")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ideal Weight Calculator", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    BackIconButton(onClick = { navigator.navigateUp() })
                },
                actions = {
                    IconButton(onClick = { 
                        val routeStr = Json.encodeToString(Route.IdealWeightCalculator() as Route)
                        navigator.navigate(Route.HistoryScreen(toolRouteFilter = routeStr)) 
                    }) {
                        Icon(Icons.Rounded.History, contentDescription = "History")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.medium)
                    .padding(16.dp)
            ) {
                CustomTextField(
                    state = inputState.height,
                    label = "Height (cm)",
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    placeholder = "Enter height",
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                ExpandableSelector(
                    title = "Gender",
                    options = genders,
                    selectedOption = inputState.gender,
                    onOptionSelected = { inputState.gender = it },
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.medium)
                    .padding(16.dp)
            ) {
                Text("Result", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Ideal Weight: ${state.idealWeight} kg",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}


@Preview
@Composable
private fun IdealWeightCalculatorScreenPreview() {
    TrueCalcTheme {
        IdealWeightCalculatorUI(
            navigator = FakeAppNavigator,
            inputState = IdealWeightInputState(),
            state = IdealWeightResultState()
        )
    }
}