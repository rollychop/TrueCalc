package com.brohit.truecalc.ui.screens.emi

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brohit.truecalc.data.data_source.local.room.entity.FixedCharge
import com.brohit.truecalc.ui.components.CustomTextField
import com.brohit.truecalc.ui.navigation.AppNavigator
import com.brohit.truecalc.ui.navigation.FakeAppNavigator
import com.brohit.truecalc.ui.navigation.Route
import com.brohit.truecalc.ui.theme.Blue


@Composable
fun LoanCalculatorUI(
    navigator: AppNavigator,
    fixedCharges: List<FixedCharge>,
    inputState: EmiCalculatorInputState,
    state: EmiCalculatorState
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
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "EMI Calculator",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                IconButton(
                    onClick = { navigator.navigateUp() }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Close"
                    )
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
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = "Principal (₹)",
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    )
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    CustomTextField(
                        state = inputState.term,
                        label = "Loan Term (${
                            if (inputState.isTermInYears) "Years"
                            else "Months"
                        })",
                        placeholder = "Term",
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    CustomTextField(
                        inputState.interestRate,
                        label = "Interest Rate (%)",
                        modifier = Modifier.weight(1f),
                        placeholder = "Rate (%)",
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        )
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, shape = MaterialTheme.shapes.medium)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                var isVisible by remember { mutableStateOf(true) }
                Row(
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.medium)
                        .clickable {
                            isVisible = !isVisible
                        }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Advanced settings",
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .weight(1f),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Icon(
                        if (isVisible) Icons.Default.ExpandLess
                        else Icons.Default.ExpandMore,
                        "expand"
                    )
                }
                AnimatedVisibility(isVisible) {
                    Column(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .clip(MaterialTheme.shapes.medium)
                            .background(Color(0xfff4f5fe))
                    ) {
                        Row(
                            modifier = Modifier
                                .clickable { navigator.navigate(Route.Settings) }
                                .padding(vertical = 8.dp, horizontal = 8.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Fixed Charges",
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.titleSmall,
                                color = Color(0xff16171d)
                            )
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "add charges",
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(Blue)
                                    .size(24.dp)
                                    .padding(4.dp),
                                tint = Color.White
                            )
                        }
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )

                        fixedCharges.forEach {
                            ResultRow(
                                label = it.name,
                                value = if (it.isPercentage) "${it.amount}%"
                                else "₹${it.amount}",
                                modifier = Modifier.padding(horizontal = 8.dp)
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
                Text(
                    "Calculations",
                    modifier = Modifier.padding(bottom = 8.dp),
                    style = MaterialTheme.typography.headlineSmall
                )
                ResultRow(label = "Monthly Payment", value = state.monthlyPayment)
                ResultRow(label = "Total Paid", value = state.totalPaid)
                ResultRow(label = "Total Interest", value = state.totalInterest)
                ResultRow(label = "Total Charges", value = state.totalCharges)
            }

            Button(
                onClick = { },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50)
                )
            ) {
                Text("Save", color = Color.White)
            }
        }
    }
}

@Composable
fun ResultRow(
    label: String, value: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
    }
}


@Composable
fun EmiCalculatorScreen(
    navigator: AppNavigator,
    viewModel: EmiCalculatorViewModel = hiltViewModel()
) {
    LoanCalculatorUI(
        navigator = navigator,
        fixedCharges = viewModel.allFixedCharges
            .collectAsStateWithLifecycle()
            .value,
        inputState = viewModel.inputState,
        state = viewModel.state.collectAsStateWithLifecycle().value
    )
}

@Preview(showBackground = true)
@Composable
fun LoanCalculatorPreview() {
    LoanCalculatorUI(
        remember { FakeAppNavigator() },
        listOf(
            FixedCharge(0, "Stamp Duty", 200.0, false)
        ),
        inputState = EmiCalculatorInputState(),
        state = EmiCalculatorState()
    )
}
