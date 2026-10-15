package com.brohit.truecalc.ui.components.tool

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.brohit.truecalc.ui.theme.Spacing
import com.brohit.truecalc.ui.theme.TrueCalcTheme

/** Light, dark and 200% font-scale previews for shared tool components. */
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
