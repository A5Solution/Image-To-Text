package com.example.image_to_text.ui.utils

import android.util.Log
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase

class Utils {
companion object{
    fun logAnalytic(screenName: String) {
        Log.d(
            "events_firebase",
            "logAnalytic: $screenName"
        )
        Firebase.analytics.logEvent (screenName, null)
    }
}
public fun logAnalytic(screenName: String) {
        Log.d(
            "events_firebase",
            "logAnalytic: $screenName"
        )
        Firebase.analytics.logEvent (screenName, null)
    }
}

