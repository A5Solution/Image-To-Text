package com.example.image_to_text.ui
import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.image_to_text.ui.activities.SplashActivity.Companion.admobOpen
import com.example.image_to_text.ui.activities.SplashActivity.Companion.isPermissionPopupVisible
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics

class ApplicationClass : Application(), LifecycleObserver, Application.ActivityLifecycleCallbacks {

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(applicationContext)
        context = this
        registerActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        applicationHandler = Handler(
            applicationContext.mainLooper
        )
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
    }
    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
    }
    companion object {
        @Volatile
        lateinit var applicationHandler: Handler
        fun getAppContext(): Context {
            return context.applicationContext
        }
        lateinit var context: Context
        var counter=0


        lateinit var firebaseAnalytics: FirebaseAnalytics
    }
    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onMoveToForeground() {
        counter++
        Log.d("isPermissionPopupVisible", "onMoveToForeground: ${counter}")
        if(counter<=1){
            return
        }else{
            if (isPermissionPopupVisible) {
                admobOpen.showOpenAd(context as Activity) {}
            }
        }
    }
    override fun onActivityCreated(p0: Activity, p1: Bundle?) {
    }
    override fun onActivityStarted(p0: Activity) {
        context = p0
    }
    override fun onActivityResumed(p0: Activity) {

    }
    override fun onActivityPaused(p0: Activity) {
    }
    override fun onActivityStopped(p0: Activity) {
    }
    override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {
    }
    override fun onActivityDestroyed(p0: Activity) {
    }
}