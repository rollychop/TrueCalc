package com.brohit.truecalc.ui.screens.emi

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.intl.LocaleList
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brohit.truecalc.data.data_source.local.room.entity.FixedCharge
import com.brohit.truecalc.domain.model.rememberTextFieldState
import com.brohit.truecalc.ui.components.BackIconButton
import com.brohit.truecalc.ui.components.CustomTextField
import com.brohit.truecalc.ui.navigation.AppNavigator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmiSettingsScreen(
    navigator: AppNavigator,
    viewModel: EmiCalculatorViewModel = hiltViewModel()
) {
    val fixedCharges by viewModel.allFixedCharges.collectAsStateWithLifecycle(emptyList())
    val chargeName = rememberTextFieldState()
    val chargeAmount = rememberTextFieldState()
    var isPercentage by remember { mutableStateOf(false) }
    var editingCharge by remember { mutableStateOf<FixedCharge?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("EMI Charges Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = { BackIconButton(onClick = { navigator.navigateUp() }) }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .imePadding()
                .padding(16.dp)
        ) {
            LazyColumn {
                item(key = "header") {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.elevatedCardElevation(4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            CustomTextField(
                                state = chargeName,
                                label = "Charge Name",
                                placeholder = "Enter charge name",
                                modifier = Modifier.fillMaxWidth()
                            )

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = isPercentage,
                                    onCheckedChange = { isPercentage = it }
                                )
                                Text("Is Percentage?")
                            }

                            val label = if (isPercentage) "Rate (%)" else "Amount (₹)"
                            CustomTextField(
                                chargeAmount,
                                label = label,
                                placeholder = "Enter $label",
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions.Default.copy(
                                    keyboardType = KeyboardType.Number
                                )
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                if (editingCharge != null) {
                                    TextButton(onClick = {
                                        editingCharge = null
                                        chargeName.reset()
                                        chargeAmount.reset()
                                        isPercentage = false
                                    }) {
                                        Text("Cancel")
                                    }
                                }
                                Button(
                                    onClick = {
                                        if (!chargeName.isValid || !chargeAmount.isValid) {
                                            chargeName.enableShowErrors(true)
                                            chargeAmount.enableShowErrors(true)
                                            return@Button
                                        }

                                        val amount = chargeAmount.text.toDoubleOrNull() ?: 0.0
                                        val newCharge = FixedCharge(
                                            name = chargeName.text.trim()
                                                .capitalize(LocaleList.current),
                                            amount = amount,
                                            isPercentage = isPercentage
                                        )

                                        if (editingCharge == null) {
                                            viewModel.insertFixedCharges(newCharge)
                                        } else {
                                            viewModel.updateFixedCharge(
                                                editingCharge?.copy(
                                                    name = chargeName.text.trim()
                                                        .capitalize(LocaleList.current),
                                                    amount = amount,
                                                    isPercentage = isPercentage
                                                ) ?: return@Button
                                            )
                                            editingCharge = null
                                        }

                                        chargeName.reset()
                                        chargeAmount.reset()
                                        isPercentage = false
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary)
                                ) {
                                    Text(if (editingCharge == null) "Add Charge" else "Update Charge")
                                }


                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Added Charges",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                }

                items(fixedCharges, key = { it.id }) { charge ->
                    FixedChargeItem(
                        charge = charge,
                        onEdit = {
                            chargeName.updateText(it.name)
                            chargeAmount.updateText(it.amount.toString())
                            isPercentage = it.isPercentage
                            editingCharge = it
                        },
                        onDelete = { viewModel.deleteFixedCharge(it) }
                    )
                }
            }
        }
    }
}

@Composable
fun FixedChargeItem(
    charge: FixedCharge,
    onEdit: (FixedCharge) -> Unit,
    onDelete: (FixedCharge) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.elevatedCardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(charge.name, fontWeight = FontWeight.Bold)
                Text(
                    text = if (charge.isPercentage) "${charge.amount}%" else "₹${charge.amount}",
                    color = Color.Gray
                )
            }

            Row {
                Button(
                    onClick = { onEdit(charge) },
                    colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.secondary)
                ) {
                    Text("Edit")
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = { onDelete(charge) },
                    colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            }
        }
    }
}
