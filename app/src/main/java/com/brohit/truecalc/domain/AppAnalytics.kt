package com.brohit.truecalc.domain

interface AppAnalytics {

    fun logAppOpen()
    fun logScreenView(screenName: String)
}