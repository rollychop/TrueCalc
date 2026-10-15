package com.brohit.truecalc.ui.components.tool

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.brohit.truecalc.ui.theme.Spacing
import com.brohit.truecalc.ui.theme.TrueCalcTheme

/** Light, dark and 200% font-scale previews for shared tool components and tool screens. */
@Preview(name = "Light", showBackground = true)
@Preview(
    name = "Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
)
@Preview(name = "Font 200%", showBackground = true, fontScale = 2f)
annotation class ToolPreviews

/** Theme + surface wrapper for component previews. */
@Composable
internal fun ToolPreviewSurface(content: @Composable () -> Unit) {
    TrueCalcTheme {
        Surface(color = MaterialTheme.colorScheme.surface) {
            Box(Modifier.padding(Spacing.l)) {
                content()
            }
        }
    }
}

/**
 * Theme + scrolling column laid out like [ToolScaffold]'s content, for previewing a tool's
 * content composable without the Hilt-backed [ToolScreen] frame.
 */
@Composable
internal fun ToolPreviewColumn(content: @Composable ColumnScope.() -> Unit) {
    TrueCalcTheme {
        Surface(color = MaterialTheme.colorScheme.surface) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(Spacing.l),
                verticalArrangement = Arrangement.spacedBy(Spacing.l),
                content = content,
            )
        }
    }
}
