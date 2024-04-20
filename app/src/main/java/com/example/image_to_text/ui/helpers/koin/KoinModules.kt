package com.example.image_to_text.ui.helpers.koin

import android.app.Activity
import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import com.example.image_to_text.ui.adsconfig.FbBannerAds
import com.example.image_to_text.ui.adsconfig.FbInterstitialAds
import com.example.image_to_text.ui.adsconfig.FbNativeAds
import com.example.image_to_text.ui.adsconfig.FbPreloadNativeAds
import com.example.image_to_text.ui.helpers.firebase.RemoteConfiguration
import com.example.image_to_text.ui.helpers.preferences.SharedPreferenceUtils
import com.example.image_to_text.ui.helpers.managers.InternetManager
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

private val managerModules = module {
    single { InternetManager(androidContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager) }
}

private val utilsModules = module {
    single { SharedPreferenceUtils(androidContext().getSharedPreferences("app_preferences", Application.MODE_PRIVATE)) }
}

private val firebaseModule = module {
    single { RemoteConfiguration(get()) }
}

private val adsModule = module {
    single { FbInterstitialAds() }
    single { FbPreloadNativeAds() }
    factory { FbNativeAds() }
    factory { FbBannerAds(this as Activity) }
}

val modulesList = listOf(utilsModules, managerModules, firebaseModule, adsModule)