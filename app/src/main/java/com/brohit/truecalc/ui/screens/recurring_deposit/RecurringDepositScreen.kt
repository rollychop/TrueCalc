package com.brohit.truecalc.ui.screens.recurring_deposit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.rounded.History
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brohit.truecalc.domain.model.CompoundingFrequency
import com.brohit.truecalc.domain.model.IndianCurrencyVisualTransformation
import com.brohit.truecalc.domain.model.InvestmentResultState
import com.brohit.truecalc.ui.components.BackIconButton
import com.brohit.truecalc.ui.components.CustomTextField
import com.brohit.truecalc.ui.components.ExpandableSelector
import com.brohit.truecalc.ui.components.InvestmentBreakdownChart
import com.brohit.truecalc.ui.components.ResultRow
import com.brohit.truecalc.ui.navigation.AppNavigator
import com.brohit.truecalc.ui.navigation.FakeAppNavigator
import com.brohit.truecalc.ui.navigation.Route
import com.brohit.truecalc.ui.screens.compund_interest.CalculationType
import kotlinx.serialization.json.Json

@Composable
fun RecurringDepositScreen(
    navigator: AppNavigator,
    viewModel: RecurringDepositViewModel = hiltViewModel()
) {
    RecurringDepositUI(
        navigator = navigator,
        inputState = viewModel.inputState,
        state = viewModel.state.collectAsStateWithLifecycle().value
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurringDepositUI(
    navigator: AppNavigator,
    inputState: RecurringDepositInputState,
    state: InvestmentResultState
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recurring Deposit", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    BackIconButton(onClick = { navigator.navigateUp() })
                },
                actions = {
                    IconButton(onClick = { 
                        val routeStr = Json.encodeToString(Route.Calculator.RecurringDeposit() as Route)
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
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Description
            Text(
                text = "Plan your future by calculating exactly how much your monthly installments will grow over time.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Input Section
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ),
                shape = MaterialTheme.shapes.large
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ExpandableSelector(
                        title = "Installment Frequency",
                        options = RecurringInterval.entries,
                        selectedOption = inputState.recurringInterval,
                        onOptionSelected = { inputState.recurringInterval = it },
                        optionTitleSelector = { it.label },
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                    )

                    CustomTextField(
                        state = inputState.principal,
                        label = "Installment Amount",
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        visualTransformation = IndianCurrencyVisualTransformation,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = "₹0.00"
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        CustomTextField(
                            state = inputState.interestRate,
                            label = "Interest Rate (%)",
                            keyboardOptions = KeyboardOptions.Default.copy(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next
                            ),
                            modifier = Modifier.weight(1f),
                            placeholder = "0.0%"
                        )
                        CustomTextField(
                            state = inputState.time,
                            label = "Time Period",
                            keyboardOptions = KeyboardOptions.Default.copy(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                            modifier = Modifier.weight(1f),
                            placeholder = "0"
                        )
                    }

                    // Unit Selector
                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        SegmentedButton(
                            selected = inputState.isTimeInYears,
                            onClick = { inputState.isTimeInYears = true },
                            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                            label = { Text("Years") }
                        )
                        SegmentedButton(
                            selected = !inputState.isTimeInYears,
                            onClick = { inputState.isTimeInYears = false },
                            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                            label = { Text("Months") }
                        )
                    }

                    ExpandableSelector(
                        title = "Interest Payout",
                        options = CompoundingFrequency.entries,
                        selectedOption = inputState.compoundingFrequency,
                        onOptionSelected = { inputState.compoundingFrequency = it },
                        optionTitleSelector = { it.getLabelFor(CalculationType.FIXED) },
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                    )
                }
            }

            // Results Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (state.amount > 0)
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    else
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                ),
                shape = MaterialTheme.shapes.large
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (state.amount > 0) {
                        Text(
                            text = "Estimated Maturity Amount",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = state.finalAmount,
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Outlined.Calculate,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Text(
                            text = "Enter details to calculate",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            if (state.amount > 0) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = MaterialTheme.shapes.large,
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        ResultRow("Total Invested", state.totalInvestment)
                        ResultRow("Interest Earned", state.interestEarned, isLast = true)
                    }
                }

                // Chart Section
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = MaterialTheme.shapes.large,
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        InvestmentBreakdownChart(
                            totalAmount = state.amount,
                            interest = state.interest,
                            principal = state.principal,
                            title = "RD Breakdown",
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RecurringDepositScreenPreview() {
    RecurringDepositUI(
        navigator = FakeAppNavigator,
        inputState = RecurringDepositInputState(),
        state = InvestmentResultState(
            amount = 12600.0,
            interest = 600.0,
            principal = 12000.0
        )
    )
}

@Preview(showBackground = true)
@Composable
private fun RecurringDepositScreenEmptyPreview() {
    RecurringDepositUI(
        navigator = FakeAppNavigator,
        inputState = RecurringDepositInputState(),
        state = InvestmentResultState()
    )
}
