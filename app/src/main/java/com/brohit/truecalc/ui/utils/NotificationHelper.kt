package com.brohit.truecalc.ui.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

object NotificationHelper {


    fun createOfferNotificationChannel(context: Context) {
        createNotificationChannel(context, OFFER_NOTIFICATION_CHANNEL_ID, "Offers")
    }

    fun createNotificationChannel(
        context: Context,
        channelId: String,
        name: String
    ) {
        val offerChannel = NotificationChannel(
            channelId,
            name,
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(offerChannel)
    }

    fun hasNotificationPermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        }
        return true
    }

    const val OFFER_NOTIFICATION_CHANNEL_ID = "offer_notification_channel"
}