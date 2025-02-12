package com.brohit.truecalc.ui.screens.compund_interest

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brohit.truecalc.domain.model.IndianCurrencyVisualTransformation
import com.brohit.truecalc.ui.components.CustomTextField
import com.brohit.truecalc.ui.components.CompoundInterestPieChart
import com.brohit.truecalc.ui.components.ResultRow
import com.brohit.truecalc.ui.navigation.AppNavigator
import com.brohit.truecalc.ui.navigation.FakeAppNavigator
import com.brohit.truecalc.ui.theme.Blue

@Composable
fun CompoundInterestUI(
    navigator: AppNavigator,
    inputState: CompoundInterestInputState,
    state: CompoundInterestState
) {
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Compound Interest",
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
                    state = inputState.principal,
                    label = "PRINCIPAL AMOUNT (₹)",
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    visualTransformation = IndianCurrencyVisualTransformation,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = "Enter Principal"
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CustomTextField(
                        state = inputState.interestRate,
                        label = "Annual Interest Rate (%)",
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        modifier = Modifier.weight(1f),
                        placeholder = "Rate"
                    )
                    CustomTextField(
                        state = inputState.time,
                        label = "Time Period (${if (inputState.isTimeInYears) "Years" else "Months"})",
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        trailingContent = {
                            Text(
                                text = if (inputState.isTimeInYears) "Year(s)" else "Month(s)",
                                modifier = Modifier.clickable {
                                    inputState.isTimeInYears = !inputState.isTimeInYears
                                }, color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.labelSmall,
                                textDecoration = TextDecoration.Underline
                            )
                        },
                        modifier = Modifier.weight(1f),
                        placeholder = "Time"
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, shape = MaterialTheme.shapes.medium)
                    .padding(8.dp)
            ) {
                var isVisible by remember { mutableStateOf(false) }
                Row(
                    modifier = Modifier
                        .clickable { isVisible = !isVisible }
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Compounding Frequency",
                        modifier = Modifier.weight(1f),
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(inputState.compoundingFrequency.name, color = Blue)
                }
                AnimatedVisibility(isVisible) {
                    Column {
                        CompoundingFrequency.entries.forEach { freq ->
                            Text(
                                text = freq.name,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        inputState.compoundingFrequency = freq
                                        isVisible = false
                                    }
                                    .padding(8.dp)
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, shape = MaterialTheme.shapes.medium)
                    .border(1.dp, Color(0xFFD7D7D7), shape = MaterialTheme.shapes.medium)
                    .padding(16.dp)
            ) {
                Text("Calculations", style = MaterialTheme.typography.headlineSmall)
                ResultRow("Final Amount", state.finalAmount)
                ResultRow("Interest Earned", state.interestEarned)
                ResultRow("Total Invested", state.totalInvestment)
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, shape = MaterialTheme.shapes.medium)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                CompoundInterestPieChart(
                    totalAmount = state.amount,
                    interest = state.interest,
                    principal = state.principal,
                    title = "Breakdown of Compound Interest",
                    modifier = Modifier
                )
            }
        }
    }
}


@Composable
fun CompoundInterestScreen(
    navigator: AppNavigator,
    viewModel: CompoundInterestViewModel = hiltViewModel()
) {
    CompoundInterestUI(
        navigator = navigator,
        inputState = viewModel.inputState,
        state = viewModel.state.collectAsStateWithLifecycle().value
    )
}

@Preview(showBackground = true)
@Composable
private fun CompoundInterestScreenPreview() {
    CompoundInterestUI(
        navigator = FakeAppNavigator,
        inputState = CompoundInterestInputState(),
        state = CompoundInterestState()
    )
}
