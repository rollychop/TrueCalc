package com.brohit.truecalc.ui.screens.health.bmi

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brohit.truecalc.ui.components.CustomTextField
import com.brohit.truecalc.ui.navigation.AppNavigator

@Composable
fun BMICalculatorScreen(
    navigator: AppNavigator,
    viewModel: BMICalculatorViewModel = hiltViewModel()
) {
    BMICalculatorUI(
        navigator = navigator,
        inputState = viewModel.inputState,
        state = viewModel.state.collectAsStateWithLifecycle().value
    )
}

@Composable
fun BMICalculatorUI(
    navigator: AppNavigator,
    inputState: BMIInputState,
    state: BMIResultState
) {
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .verticalScroll(rememberScrollState())
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
                    text = "BMI Calculator",
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
                    state = inputState.weight,
                    label = "Weight (kg)",
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    placeholder = "Enter weight in kg",
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                CustomTextField(
                    state = inputState.height,
                    label = "Height (cm)",
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    placeholder = "Enter height in cm",
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, shape = MaterialTheme.shapes.medium)
                    .padding(16.dp)
            ) {
                Text("Results", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(8.dp))
                Text("BMI Value: ${state.bmiValue}", style = MaterialTheme.typography.bodyLarge)
                Text("Category: ${state.category}", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}
