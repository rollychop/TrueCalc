package com.brohit.truecalc.ui.screens.health.ideal_weight

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brohit.truecalc.ui.components.CustomTextField
import com.brohit.truecalc.ui.components.ExpandableSelector
import com.brohit.truecalc.ui.navigation.AppNavigator
import com.brohit.truecalc.ui.navigation.FakeAppNavigator
import com.brohit.truecalc.ui.theme.TrueCalcTheme

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

@Composable
fun IdealWeightCalculatorUI(
    navigator: AppNavigator,
    inputState: IdealWeightInputState,
    state: IdealWeightResultState
) {
    val genders = listOf("Male", "Female")

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ideal Weight Calculator",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { navigator.navigateUp() }) {
                    Icon(Icons.Filled.Close, contentDescription = "Close")
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, shape = MaterialTheme.shapes.medium)
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
                    .background(Color.White, shape = MaterialTheme.shapes.medium)
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