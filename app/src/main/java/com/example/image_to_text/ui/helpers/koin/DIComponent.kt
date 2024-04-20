package com.example.image_to_text.ui.helpers.koin

import com.example.image_to_text.ui.adsconfig.FbBannerAds
import com.example.image_to_text.ui.adsconfig.FbInterstitialAds
import com.example.image_to_text.ui.adsconfig.FbNativeAds
import com.example.image_to_text.ui.adsconfig.FbPreloadNativeAds
import com.example.image_to_text.ui.helpers.firebase.RemoteConfiguration
import com.example.image_to_text.ui.helpers.managers.InternetManager
import com.example.image_to_text.ui.helpers.preferences.SharedPreferenceUtils
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class DIComponent : KoinComponent {

    // Utils
    val sharedPreferenceUtils by inject<SharedPreferenceUtils>()

    // Managers
    val internetManager by inject<InternetManager>()

    // Remote Configuration
    val remoteConfiguration by inject<RemoteConfiguration>()

    // Ads
    val fbBannerAds by inject<FbBannerAds>()
    val fbNativeAds by inject<FbNativeAds>()
    val fbPreLoadNativeAds by inject<FbPreloadNativeAds>()
    val fbInterstitialAds by inject<FbInterstitialAds>()
}