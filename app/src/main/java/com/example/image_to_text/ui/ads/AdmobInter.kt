package com.example.image_to_text.ui.ads

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.ProgressBar
import android.widget.TextView
import com.example.image_to_text.R
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

class AdmobInter {

    private var mInterstitialAd: InterstitialAd? = null
    private var count=0
    companion object{
        public var isClicked = false
    }
    fun loadInterAd(context: Context, admobInterId: String) {
        count++
        if(count%3!=0){
            isClicked =true
            return
        }
        isClicked =false
        if(mInterstitialAd != null)
            return
        var adRequest = AdRequest.Builder().build()

        InterstitialAd.load(
            context,
            admobInterId,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.d("AdmobInterAd", adError?.toString()!!)
                    mInterstitialAd = null
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    Log.d("AdmobInterAd", "inter ad loaded successfully")
                    mInterstitialAd = interstitialAd
                }
            })
    }

    fun showInterAd(context: Context, adEvent: (Boolean) -> Unit) {

        if (mInterstitialAd != null) {
            Log.d("AdmobInterAd", "showInterAd: if")
            val dialog = Dialog(context)
            dialog.setContentView(R.layout.dialog_loading_ad)
            dialog.setCancelable(false)

            val loadingText = dialog.findViewById<TextView>(R.id.loadingText)

            // Set dialog window to fullscreen
            val window = dialog.window
            window?.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT
            )
            window?.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
            dialog.show()

            mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    super.onAdDismissedFullScreenContent()
                    Log.d("AdmobInterAd", "onAdDismissedFullScreenContent")
                    mInterstitialAd = null
                    dialog.dismiss()
                    adEvent.invoke(true)
                }

                override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                    super.onAdFailedToShowFullScreenContent(p0)
                    Log.d("AdmobInterAd", "onAdFailedToShowFullScreenContent")
                    dialog.dismiss()
                    adEvent.invoke(true)
                }

                override fun onAdShowedFullScreenContent() {
                    super.onAdShowedFullScreenContent()
                    Log.d("AdmobInterAd", "onAdShowedFullScreenContent")
                }

                override fun onAdClicked() {
                    super.onAdClicked()
                    Log.d("AdmobInterAd", "onAdClicked")
                }

                override fun onAdImpression() {
                    super.onAdImpression()
                    Log.d("AdmobInterAd", "onAdImpression")
                }
            }
            // Set loading text
            loadingText.text = "Ad is loading..."
            Handler(Looper.getMainLooper()).postDelayed({
                dialog.dismiss()
                mInterstitialAd?.show(context as Activity)
            }, 1500)
        } else {
            adEvent.invoke(true)
            Log.d("AdmobInterAd", "The interstitial ad wasn't ready yet.")
        }
    }

}