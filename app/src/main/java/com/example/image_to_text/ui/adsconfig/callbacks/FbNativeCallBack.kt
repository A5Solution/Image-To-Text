package com.example.image_to_text.ui.adsconfig.callbacks

interface FbNativeCallBack {
    fun onError(adError:String)
    fun onAdLoaded()
    fun onAdClicked()
    fun onLoggingImpression()
    fun onMediaDownloaded()
    fun onPreloaded()
}