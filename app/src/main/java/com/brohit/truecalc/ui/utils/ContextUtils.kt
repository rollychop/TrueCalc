package com.brohit.truecalc.ui.utils

import android.content.Context
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri

fun Context.openUrl(url: String) {
    try {
        val uri = url.toUri()
        val customTabsIntent = CustomTabsIntent.Builder()
            .setShowTitle(true)
            .build()
        customTabsIntent.launchUrl(this, uri)
    } catch (e: Exception) {
        Toast.makeText(this, "Could not open link", Toast.LENGTH_SHORT).show()
    }
}
