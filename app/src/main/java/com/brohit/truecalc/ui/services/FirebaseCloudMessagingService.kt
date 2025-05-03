package com.brohit.truecalc.ui.services

import android.Manifest
import android.app.Notification
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.edit
import com.brohit.truecalc.R
import com.brohit.truecalc.ui.utils.NotificationHelper
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FirebaseCloudMessagingService : FirebaseMessagingService() {


    override fun onMessageReceived(message: RemoteMessage) {


        val notification: Notification.Builder =
            Notification.Builder(
                this,
                NotificationHelper.OFFER_NOTIFICATION_CHANNEL_ID
            )
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(message.notification?.title)
                .setContentText(message.notification?.body)
                .setAutoCancel(true)

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        NotificationManagerCompat.from(this)
            .notify(1, notification.build())
        super.onMessageReceived(message)

    }

    override fun onNewToken(token: String) {
        println("FCM TOKEN BEING SAVED $token")
        getSharedPreferences("_", MODE_PRIVATE).edit { putString("fcm_token", token) }
        super.onNewToken(token)
    }

    companion object {
        fun getFCMToken(context: Context): String {
            return context.getSharedPreferences("_", MODE_PRIVATE).getString("fcm_token", "empty")
                ?: ""
        }
    }


}