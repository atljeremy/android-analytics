package com.jeremyfox.analytics.providers

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.jeremyfox.analytics.Analytics
import javax.inject.Inject

class FirebaseAnalyticsProvider @Inject constructor(
        private val firebase: FirebaseAnalytics
): Analytics.Provider {
    override fun track(event: Analytics.Event) {
        val bundle = Bundle().apply {
            event.data?.entries?.forEach {
                when (val value = it.value) {
                    is Int -> putInt(it.key, value)
                    is Long -> putLong(it.key, value)
                    is Double -> putDouble(it.key, value)
                    is Float -> putFloat(it.key, value)
                    is String -> putString(it.key, value)
                }
            }
        }
        firebase.logEvent(event.eventName, bundle)
    }

    override fun setGlobal(data: Map<String, String>) {
        // Choose how you'd like to track global attributes.
        // You might consider using user properties, something like:
        // data.entries.forEach {
        //     FirebaseAnalytics.getInstance(context).setUserProperty(it.key, it.value)
        // }
    }
}