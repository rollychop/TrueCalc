package com.brohit.truecalc.ui.utils

import android.content.Intent
import android.provider.Settings
import android.view.View
import com.google.android.material.snackbar.Snackbar

fun View.showSnackBar(
    message: String,
    actionLabel: String? = null,
    onAction: ((View) -> Unit)? = null,
    duration: Int = if (actionLabel != null) Snackbar.LENGTH_INDEFINITE
    else Snackbar.LENGTH_SHORT,
) {
    Snackbar.make(this, message, duration)
        .also {
            if (actionLabel != null) {
                it.setAction(actionLabel, onAction)
            }
        }
        .show()
}

fun View.showAllowNotificationSnackbar() {
    if (!NotificationHelper.hasNotificationPermission(context)) {
        showSnackBar(
            message = "Notification permission not granted",
            actionLabel = "Allow",
            duration = Snackbar.LENGTH_LONG,
            onAction = {
                // Go to app notification settings
                val intent = Intent().apply {
                    action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                }
                context.startActivity(intent)
            }
        )
    }
}