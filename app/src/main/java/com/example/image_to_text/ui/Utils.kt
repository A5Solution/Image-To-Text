package com.example.image_to_text.ui

import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.Log
import android.view.View
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

