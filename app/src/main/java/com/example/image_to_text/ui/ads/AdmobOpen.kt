package com.example.image_to_text.ui.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.example.image_to_text.ui.splashscreen.SplashActivity
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd

class AdmobOpen :Activity() {
    companion object{
        var myOpenAd: AppOpenAd? = null
    }


    fun loadOpenAd(context: Context, adLoaded: (Boolean) -> Unit) {
        if (myOpenAd != null) {
            Log.d("MyOpenAd", "loadOpenAd: no need")
            adLoaded.invoke(false)
        } else {
            val request = AdRequest.Builder().build()
            AppOpenAd.load(
                context,
                SplashActivity.admobOpenId,
                request,
                object : AppOpenAd.AppOpenAdLoadCallback() {
                    override fun onAdFailedToLoad(p0: LoadAdError) {
                        adLoaded.invoke(true)
                        Log.d("MyOpenAd", "onAdFailedToLoad: open ad fail to load $p0")
                    }

                    override fun onAdLoaded(ad: AppOpenAd) {
                        myOpenAd = ad
                        adLoaded.invoke(true)
                        Log.d("MyOpenAd", "onAdLoaded: open ad loaded success")
                    }
                })
        }

    }

    fun showOpenAd(activity: Activity, isShow: (Boolean) -> Unit) {
        Log.d("MyOpenAd", "shown called")
        myOpenAd?.let { ad ->
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdShowedFullScreenContent() {
                    super.onAdShowedFullScreenContent()
                    Log.d("MyOpenAd", "onAdLoaded: ad shown")
                }

                override fun onAdDismissedFullScreenContent() {
                    super.onAdDismissedFullScreenContent()
                    myOpenAd = null
                    Log.d("MyOpenAd", "onAdDismissedFullScreenContent: ")
                    loadOpenAd(activity) {}
                    isShow.invoke(true)
                }

                override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                    super.onAdFailedToShowFullScreenContent(p0)
                    myOpenAd = null
                    loadOpenAd(activity) {}
                    isShow.invoke(true)
                }

            }
            ad.show(activity)
        } ?: isShow.invoke(true)
    }
}