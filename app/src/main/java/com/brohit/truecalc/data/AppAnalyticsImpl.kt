package com.brohit.truecalc.data

import android.content.Context
import androidx.core.os.bundleOf
import com.brohit.truecalc.domain.AppAnalytics
import com.google.firebase.analytics.FirebaseAnalytics

class AppAnalyticsImpl(appContext: Context) : AppAnalytics {

    private val analytics: FirebaseAnalytics = FirebaseAnalytics.getInstance(appContext)

    override fun logAppOpen() {
        analytics.logEvent(FirebaseAnalytics.Event.APP_OPEN, null)
    }

    override fun logScreenView(screenName: String) {
        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundleOf(
            FirebaseAnalytics.Param.SCREEN_NAME to screenName
        ))
    }
}