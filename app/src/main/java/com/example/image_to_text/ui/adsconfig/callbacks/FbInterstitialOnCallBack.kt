package com.example.image_to_text.ui.adsconfig.callbacks

interface FbInterstitialOnCallBack {
    fun onInterstitialDisplayed()
    fun onInterstitialDismissed()
    fun onError(adError:String)
    fun onAdLoaded()
    fun onAdClicked()
    fun onLoggingImpression()
    fun onPreloaded()
}