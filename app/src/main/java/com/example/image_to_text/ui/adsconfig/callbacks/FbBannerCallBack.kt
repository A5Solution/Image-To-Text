package com.example.image_to_text.ui.adsconfig.callbacks

interface FbBannerCallBack {
    fun onError(adError:String)
    fun onAdLoaded()
    fun onAdClicked()
    fun onLoggingImpression()
}