package com.brohit.truecalc.ui.utils

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import com.brohit.truecalc.BuildConfig

enum class MoreIcon(val label: String) {
    Rate("Leave feedback"),
    Share("Share app"),
    Update("Check for updates")
}


fun MoreIcon.toIntent(context: Context): Intent {
    // The GitHub build has its own package id, so store links always use the Play package.
    val packageName = BuildConfig.PLAY_PACKAGE
    return when (this) {
        MoreIcon.Rate -> Intent(Intent.ACTION_VIEW).apply {
            data = "market://details?id=$packageName".toUri()
        }

        MoreIcon.Share -> Intent(Intent.ACTION_SEND).run {
            type = "text/plain"
            putExtra(
                Intent.EXTRA_TEXT,
                "Check out TrueCalc app: https://play.google.com/store/apps/details?id=$packageName"
            )
            return@run Intent.createChooser(this, "Share via")
        }

        MoreIcon.Update -> Intent(Intent.ACTION_VIEW).apply {
            data = if (BuildConfig.IS_PLAY_BUILD) "market://details?id=$packageName".toUri()
            else BuildConfig.RELEASES_URL.toUri()
        }
    }
}
