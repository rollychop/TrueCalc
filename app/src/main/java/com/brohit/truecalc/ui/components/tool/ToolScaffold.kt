package com.brohit.truecalc.ui.components.tool

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import com.brohit.truecalc.domain.model.TextFieldState
import com.brohit.truecalc.ui.components.BackIconButton
import com.brohit.truecalc.ui.theme.Spacing
import com.brohit.truecalc.ui.theme.TrueCalcTheme

/**
 * Standard frame for every tool screen: a collapsing [LargeTopAppBar] with back, favourite,
 * history and an overflow menu (Share, Reset), and a scrolling, keyboard-aware content column
 * with [Spacing.l] padding and spacing.
 *
 * Actions are shown only when their callback (or [shareText]) is provided. Most screens should
 * use [ToolScreen], which wires favourite and history for a registered tool.
 *
 * @param shareText text to share; when null the Share item is disabled (e.g. no result yet).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolScaffold(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    isFavorite: Boolean = false,
    onToggleFavorite: (() -> Unit)? = null,
    onHistory: (() -> Unit)? = null,
    onReset: (() -> Unit)? = null,
    shareText: String? = null,
    showShare: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val context = LocalContext.current

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            LargeTopAppBar(
                title = { Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = { BackIconButton(onClick = onBack) },
                actions = {
                    if (onToggleFavorite != null) {
                        IconButton(onClick = onToggleFavorite) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                                contentDescription = if (isFavorite) "Remove from favourites" else "Add to favourites",
                                tint = if (isFavorite) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    if (onHistory != null) {
                        IconButton(onClick = onHistory) {
                            Icon(Icons.Rounded.History, contentDescription = "History")
                        }
                    }
                    if (showShare || onReset != null) {
                        ToolOverflowMenu(
                            onShare = if (showShare && shareText != null) {
                                { shareResult(context, title, shareText) }
                            } else null,
                            showShare = showShare,
                            onReset = onReset,
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(Spacing.l),
            verticalArrangement = Arrangement.spacedBy(Spacing.l),
            content = content,
        )
    }
}

@Composable
private fun ToolOverflowMenu(
    onShare: (() -> Unit)?,
    showShare: Boolean,
    onReset: (() -> Unit)?,
) {
    var expanded by remember { mutableStateOf(false) }
    IconButton(onClick = { expanded = true }) {
        Icon(Icons.Rounded.MoreVert, contentDescription = "More actions")
    }
    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
        if (showShare) {
            DropdownMenuItem(
                text = { Text("Share result") },
                leadingIcon = { Icon(Icons.Rounded.Share, contentDescription = null) },
                enabled = onShare != null,
                onClick = {
                    expanded = false
                    onShare?.invoke()
                },
            )
        }
        if (onReset != null) {
            DropdownMenuItem(
                text = { Text("Reset") },
                leadingIcon = { Icon(Icons.Rounded.RestartAlt, contentDescription = null) },
                onClick = {
                    expanded = false
                    onReset()
                },
            )
        }
    }
}

/** Opens the system share sheet with [text]. */
internal fun shareResult(context: Context, subject: String, text: String) {
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, text)
    }
    try {
        context.startActivity(Intent.createChooser(send, null))
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(context, "No app available to share", Toast.LENGTH_SHORT).show()
    }
}

@ToolPreviews
@Composable
private fun ToolScaffoldPreview() {
    TrueCalcTheme {
        ToolScaffold(
            title = "EMI Calculator",
            onBack = {},
            isFavorite = true,
            onToggleFavorite = {},
            onHistory = {},
            onReset = {},
            shareText = "EMI: ₹9,650.22",
        ) {
            InputSection {
                NumberField(
                    state = TextFieldState().apply { updateText("1000000") },
                    label = "Loan amount",
                    currency = true,
                )
                NumberField(state = TextFieldState(), label = "Interest rate", suffix = "%")
            }
            ResultHero(label = "Monthly EMI", value = "₹9,650.22", caption = "for 240 months")
            BreakdownSection(rows = breakdownRows("Principal" to "₹10,00,000.00", "Interest" to "₹13,16,052.74"))
        }
    }
}
