package com.brohit.truecalc.ui.screens.everyday.calculator

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Backspace
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.brohit.truecalc.domain.calc.AngleUnit
import com.brohit.truecalc.domain.calc.CalcKey
import com.brohit.truecalc.domain.calc.CalcKeyKind
import com.brohit.truecalc.domain.calc.CalculatorEditor
import com.brohit.truecalc.domain.calc.CalculatorLayouts
import com.brohit.truecalc.ui.components.tool.ToolInfo
import com.brohit.truecalc.ui.components.tool.ToolPreviews
import com.brohit.truecalc.ui.components.tool.ToolScreen
import com.brohit.truecalc.ui.components.tool.UnitToggle
import com.brohit.truecalc.ui.navigation.AppNavigator
import com.brohit.truecalc.ui.navigation.Route
import com.brohit.truecalc.ui.theme.Spacing
import com.brohit.truecalc.ui.theme.TrueCalcTheme
import com.brohit.truecalc.ui.theme.numberDisplay
import com.brohit.truecalc.ui.theme.numberLarge
import com.brohit.truecalc.ui.tools.ToolRegistry

@Composable
fun CalculatorScreen(
    navigator: AppNavigator,
    viewModel: CalculatorViewModel = hiltViewModel(),
) {
    ToolScreen(
        tool = checkNotNull(ToolRegistry.forRoute(Route.Calculator.Basic())),
        navigator = navigator,
        onReset = viewModel::reset,
        shareText = viewModel.shareText,
        scrollable = false,
    ) {
        CalculatorContent(
            expression = viewModel.expression,
            preview = viewModel.preview,
            error = viewModel.error?.message,
            scientific = viewModel.scientific,
            angleUnit = viewModel.angleUnit,
            tape = viewModel.tape,
            onKey = viewModel::onKey,
            onScientificChange = viewModel::onScientificChange,
            onTapeEntryClick = viewModel::useTapeEntry,
            onClearTape = viewModel::clearTape,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

/**
 * Stateless calculator: mode toggle and tape button, the display, and a keypad that fills the
 * remaining height (4 columns basic, 5 scientific).
 */
@Composable
fun CalculatorContent(
    expression: String,
    preview: String?,
    error: String?,
    scientific: Boolean,
    angleUnit: AngleUnit,
    tape: List<TapeEntry>,
    onKey: (CalcKey) -> Unit,
    onScientificChange: (Boolean) -> Unit,
    onTapeEntryClick: (TapeEntry) -> Unit,
    onClearTape: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showTape by rememberSaveable { mutableStateOf(false) }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Spacing.m)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            UnitToggle(
                options = listOf(false, true),
                selected = scientific,
                onSelect = onScientificChange,
                label = { if (it) "Scientific" else "Basic" },
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = { showTape = true }) {
                Icon(Icons.AutoMirrored.Rounded.ReceiptLong, contentDescription = "Tape: recent calculations")
            }
        }

        CalculatorDisplay(
            expression = expression,
            preview = preview,
            error = error,
            angleLabel = if (scientific) angleUnit.label else null,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        )

        Keypad(
            rows = if (scientific) CalculatorLayouts.scientific else CalculatorLayouts.basic,
            angleUnit = angleUnit,
            onKey = onKey,
            modifier = Modifier
                .fillMaxWidth()
                .weight(if (scientific) KeypadWeightScientific else KeypadWeightBasic),
        )
    }

    if (showTape) {
        TapeSheet(
            tape = tape,
            onDismiss = { showTape = false },
            onEntryClick = {
                onTapeEntryClick(it)
                showTape = false
            },
            onClear = onClearTape,
        )
    }
}

/** Keypad share of the height relative to the display (weight 1). */
private const val KeypadWeightBasic = 2.6f
private const val KeypadWeightScientific = 3.6f

@Composable
private fun CalculatorDisplay(
    expression: String,
    preview: String?,
    error: String?,
    angleLabel: String?,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        color = scheme.surfaceContainerLow,
    ) {
        Column(
            modifier = Modifier.padding(Spacing.l),
            verticalArrangement = Arrangement.spacedBy(Spacing.xs, Alignment.Bottom),
            horizontalAlignment = Alignment.End,
        ) {
            if (angleLabel != null) {
                Text(
                    text = angleLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = scheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Box(modifier = Modifier.weight(1f))
            val shown = CalculatorEditor.displayText(expression).ifEmpty { "0" }
            // End-aligned; a long expression scrolls, and reverse scrolling keeps its end in view.
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                Text(
                    text = shown,
                    style = MaterialTheme.typography.numberDisplay,
                    color = if (expression.isEmpty()) scheme.onSurfaceVariant else scheme.onSurface,
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState(), reverseScrolling = true)
                        .semantics { contentDescription = "Expression $shown" },
                )
            }
            val secondary = error ?: preview?.let { "= $it" }
            Text(
                text = secondary.orEmpty(),
                style = MaterialTheme.typography.numberLarge,
                color = if (error != null) scheme.error else scheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun Keypad(
    rows: List<List<CalcKey>>,
    angleUnit: AngleUnit,
    onKey: (CalcKey) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
        rows.forEach { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(Spacing.s),
            ) {
                row.forEach { key ->
                    KeypadKey(
                        key = key,
                        label = if (key == CalcKey.AngleToggle) angleUnit.label else key.label,
                        compact = rows.first().size > 4,
                        onClick = { onKey(key) },
                        onLongClick = if (key == CalcKey.Backspace) {
                            { onKey(CalcKey.Clear) }
                        } else null,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                    )
                }
            }
        }
    }
}

/** Container and content colours for a key, by kind (theme roles only). */
@Composable
private fun keyColors(kind: CalcKeyKind): Pair<Color, Color> {
    val scheme = MaterialTheme.colorScheme
    return when (kind) {
        CalcKeyKind.Digit -> scheme.surfaceContainerHighest to scheme.onSurface
        CalcKeyKind.Operator -> scheme.primaryContainer to scheme.onPrimaryContainer
        CalcKeyKind.Function -> scheme.secondaryContainer to scheme.onSecondaryContainer
        CalcKeyKind.Control -> scheme.tertiaryContainer to scheme.onTertiaryContainer
        CalcKeyKind.Equals -> scheme.primary to scheme.onPrimary
    }
}

private val KeyContentPadding = PaddingValues(Spacing.xs)

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun KeypadKey(
    key: CalcKey,
    label: String,
    compact: Boolean,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val (container, content) = keyColors(key.kind)
    val shape = MaterialTheme.shapes.large
    val textStyle: TextStyle = when {
        key.kind == CalcKeyKind.Function -> MaterialTheme.typography.titleMedium
        compact -> MaterialTheme.typography.titleLarge
        else -> MaterialTheme.typography.headlineSmall
    }

    when {
        onLongClick != null -> {
            // Button has no long-press, so the backspace key is a tonal surface with combinedClickable.
            val haptics = LocalHapticFeedback.current
            Surface(
                modifier = modifier
                    .clip(shape)
                    .combinedClickable(
                        role = Role.Button,
                        onClickLabel = "Delete",
                        onLongClickLabel = "Clear all",
                        onLongClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            onLongClick()
                        },
                        onClick = onClick,
                    ),
                shape = shape,
                color = container,
                contentColor = content,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.AutoMirrored.Rounded.Backspace,
                        contentDescription = "Backspace (hold to clear)",
                    )
                }
            }
        }
        key.kind == CalcKeyKind.Equals -> {
            Button(
                onClick = onClick,
                modifier = modifier,
                shape = shape,
                colors = ButtonDefaults.buttonColors(containerColor = container, contentColor = content),
                contentPadding = KeyContentPadding,
            ) {
                KeyLabel(label, textStyle)
            }
        }
        else -> {
            FilledTonalButton(
                onClick = onClick,
                modifier = modifier.semantics { contentDescription = keyDescription(key, label) },
                shape = shape,
                colors = ButtonDefaults.filledTonalButtonColors(containerColor = container, contentColor = content),
                contentPadding = KeyContentPadding,
            ) {
                KeyLabel(label, textStyle)
            }
        }
    }
}

@Composable
private fun KeyLabel(label: String, style: TextStyle) {
    Text(text = label, style = style, maxLines = 1, softWrap = false)
}

/** Spoken names for symbol keys. */
private fun keyDescription(key: CalcKey, label: String): String = when (key) {
    CalcKey.Add -> "Plus"
    CalcKey.Subtract -> "Minus"
    CalcKey.Multiply -> "Multiply"
    CalcKey.Divide -> "Divide"
    CalcKey.Percent -> "Percent"
    CalcKey.Parens -> "Parentheses"
    CalcKey.Dot -> "Decimal point"
    CalcKey.Clear -> "All clear"
    CalcKey.Sqrt -> "Square root"
    CalcKey.Square -> "Square"
    CalcKey.Power -> "Power"
    CalcKey.Factorial -> "Factorial"
    CalcKey.Pi -> "Pi"
    CalcKey.E -> "Euler's number"
    CalcKey.Ans -> "Last answer"
    CalcKey.OpenParen -> "Open parenthesis"
    CalcKey.CloseParen -> "Close parenthesis"
    CalcKey.AngleToggle -> if (label == AngleUnit.Degrees.label) "Degrees, switch to radians" else "Radians, switch to degrees"
    else -> label
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TapeSheet(
    tape: List<TapeEntry>,
    onDismiss: () -> Unit,
    onEntryClick: (TapeEntry) -> Unit,
    onClear: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.xl),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Tape",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f),
            )
            if (tape.isNotEmpty()) {
                TextButton(onClick = onClear) { Text("Clear") }
            }
        }
        LazyColumn(
            modifier = Modifier.navigationBarsPadding(),
            contentPadding = PaddingValues(bottom = Spacing.l),
        ) {
            if (tape.isEmpty()) {
                item {
                    Text(
                        text = "Results appear here when you press =. Tap one to use it again.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = Spacing.xl, vertical = Spacing.l),
                    )
                }
            }
            items(tape) { entry ->
                ListItem(
                    headlineContent = {
                        Text(entry.result, style = MaterialTheme.typography.numberLarge)
                    },
                    supportingContent = {
                        Text(CalculatorEditor.displayText(entry.expression), maxLines = 2, overflow = TextOverflow.Ellipsis)
                    },
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    modifier = Modifier.clickable(
                        onClickLabel = "Use this result",
                        role = Role.Button,
                        onClick = { onEntryClick(entry) },
                    ),
                )
            }
            item {
                ToolInfo(
                    formula = "× and ÷ before + and −; powers before ×; brackets first.\n" +
                            "50 + 10% = 55 (10% of 50 added); 200 × 10% = 20.",
                    notes = "After + or −, a percentage is taken of the number before it, like " +
                            "phone calculators; elsewhere X% means X ÷ 100. Results show up to " +
                            "10 significant digits, and + − × ÷ use exact decimals so " +
                            "0.1 + 0.2 = 0.3. Trig uses degrees or radians (DEG/RAD key). Hold " +
                            "backspace to clear. The tape keeps this session's last " +
                            "${CalculatorViewModel.MAX_TAPE} results; History keeps them for good.",
                    modifier = Modifier.padding(horizontal = Spacing.l),
                )
            }
        }
    }
}

/** Fixed height so the full-height keypad lays out in previews. */
private val PreviewHeight = 720.dp

@ToolPreviews
@Composable
private fun CalculatorContentPreview() {
    TrueCalcTheme {
        Surface(color = MaterialTheme.colorScheme.surface) {
            CalculatorContent(
                expression = "1234+56×7",
                preview = "1,626",
                error = null,
                scientific = false,
                angleUnit = AngleUnit.Degrees,
                tape = emptyList(),
                onKey = {},
                onScientificChange = {},
                onTapeEntryClick = {},
                onClearTape = {},
                modifier = Modifier
                    .height(PreviewHeight)
                    .padding(Spacing.l),
            )
        }
    }
}

@ToolPreviews
@Composable
private fun CalculatorScientificPreview() {
    TrueCalcTheme {
        Surface(color = MaterialTheme.colorScheme.surface) {
            CalculatorContent(
                expression = "2×sin(30)+√9",
                preview = "4",
                error = null,
                scientific = true,
                angleUnit = AngleUnit.Degrees,
                tape = emptyList(),
                onKey = {},
                onScientificChange = {},
                onTapeEntryClick = {},
                onClearTape = {},
                modifier = Modifier
                    .height(PreviewHeight)
                    .padding(Spacing.l),
            )
        }
    }
}
