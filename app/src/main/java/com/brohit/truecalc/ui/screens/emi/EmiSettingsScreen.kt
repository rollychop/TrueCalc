package com.brohit.truecalc.ui.screens.emi

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.intl.LocaleList
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brohit.truecalc.data.data_source.local.room.entity.FixedCharge
import com.brohit.truecalc.domain.model.TextFieldState
import com.brohit.truecalc.domain.model.rememberTextFieldState
import com.brohit.truecalc.ui.components.BackIconButton
import com.brohit.truecalc.ui.navigation.AppNavigator
import com.brohit.truecalc.ui.components.tool.InputSection
import com.brohit.truecalc.ui.components.tool.NumberField
import com.brohit.truecalc.ui.components.tool.UnitToggle
import com.brohit.truecalc.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmiSettingsScreen(
    navigator: AppNavigator,
    viewModel: EmiCalculatorViewModel = hiltViewModel()
) {
    val fixedCharges by viewModel.allFixedCharges.collectAsStateWithLifecycle(emptyList())
    val chargeName = rememberTextFieldState()
    val chargeAmount = rememberTextFieldState()
    var isPercentage by rememberSaveable { mutableStateOf(false) }
    var editingCharge by remember { mutableStateOf<FixedCharge?>(null) }

    fun clearForm() {
        editingCharge = null
        chargeName.reset()
        chargeAmount.reset()
        isPercentage = false
    }

    val amount = chargeAmount.text.toDoubleOrNull()
    val canSave = chargeName.text.isNotBlank() && amount != null

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TopAppBar(
                title = { Text("EMI charges") },
                navigationIcon = { BackIconButton(onClick = { navigator.navigateUp() }) }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .imePadding(),
            contentPadding = PaddingValues(Spacing.l),
            verticalArrangement = Arrangement.spacedBy(Spacing.m)
        ) {
            item(key = "form") {
                InputSection(title = if (editingCharge == null) "Add a charge" else "Edit charge") {
                    ChargeNameField(state = chargeName)

                    UnitToggle(
                        options = listOf(false, true),
                        selected = isPercentage,
                        onSelect = { isPercentage = it },
                        label = { if (it) "% of loan" else "Fixed amount" },
                    )

                    NumberField(
                        state = chargeAmount,
                        label = if (isPercentage) "Rate" else "Amount",
                        prefix = if (isPercentage) null else "₹",
                        suffix = if (isPercentage) "%" else null,
                        imeAction = ImeAction.Done,
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.s, Alignment.End),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (editingCharge != null) {
                            TextButton(onClick = ::clearForm) {
                                Text("Cancel")
                            }
                        }
                        Button(
                            enabled = canSave,
                            onClick = {
                                val value = amount ?: return@Button
                                val name = chargeName.text.trim().capitalize(LocaleList.current)
                                val editing = editingCharge
                                if (editing == null) {
                                    viewModel.insertFixedCharges(
                                        FixedCharge(name = name, amount = value, isPercentage = isPercentage)
                                    )
                                } else {
                                    viewModel.updateFixedCharge(
                                        editing.copy(name = name, amount = value, isPercentage = isPercentage)
                                    )
                                }
                                clearForm()
                            }
                        ) {
                            Text(if (editingCharge == null) "Add charge" else "Update charge")
                        }
                    }
                }
            }

            item(key = "list_header") {
                Text(
                    text = if (fixedCharges.isEmpty()) "No charges added yet" else "Added charges",
                    modifier = Modifier.padding(top = Spacing.s, start = Spacing.xs),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            items(fixedCharges, key = { it.id }) { charge ->
                FixedChargeItem(
                    charge = charge,
                    onEdit = {
                        chargeName.updateText(it.name)
                        chargeAmount.updateText(formatAmountForEditing(it.amount))
                        isPercentage = it.isPercentage
                        editingCharge = it
                    },
                    onDelete = { viewModel.deleteFixedCharge(it) }
                )
            }
        }
    }
}

/** Free-text field for the charge name, styled like [NumberField]. */
@Composable
private fun ChargeNameField(state: TextFieldState) {
    TextField(
        value = state.text,
        onValueChange = { state.updateText(it) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        label = { Text("Charge name") },
        placeholder = { Text("e.g. Processing fee") },
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Words,
            imeAction = ImeAction.Next,
        ),
        shape = MaterialTheme.shapes.medium,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
        ),
    )
}

/** "500.0" → "500", "2.5" → "2.5", so editing doesn't show a stray ".0". */
private fun formatAmountForEditing(amount: Double): String =
    if (amount % 1.0 == 0.0) amount.toLong().toString() else amount.toString()

@Composable
fun FixedChargeItem(
    charge: FixedCharge,
    onEdit: (FixedCharge) -> Unit,
    onDelete: (FixedCharge) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        ListItem(
            headlineContent = { Text(charge.name) },
            supportingContent = {
                Text(
                    text = if (charge.isPercentage) "${charge.amount}%" else "₹${charge.amount}",
                    style = MaterialTheme.typography.bodyMedium.copy(fontFeatureSettings = "tnum")
                )
            },
            trailingContent = {
                Row {
                    IconButton(onClick = { onEdit(charge) }) {
                        Icon(Icons.Rounded.Edit, contentDescription = "Edit ${charge.name}")
                    }
                    IconButton(onClick = { onDelete(charge) }) {
                        Icon(
                            Icons.Rounded.DeleteOutline,
                            contentDescription = "Delete ${charge.name}",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        )
    }
}
