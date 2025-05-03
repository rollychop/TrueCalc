package com.brohit.truecalc

import android.app.Application
import com.brohit.truecalc.ui.utils.NotificationHelper
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class TrueCalcApp : Application() {

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createOfferNotificationChannel(this)
    }


}