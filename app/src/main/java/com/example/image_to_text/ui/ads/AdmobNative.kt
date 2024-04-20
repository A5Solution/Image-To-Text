package com.example.image_to_text.ui.ads

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import com.example.image_to_text.R
import com.example.image_to_text.ui.activities.MainActivity
import com.example.image_to_text.ui.utils.Utils
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView


class AdmobNative {
    private var mNativeAd1: NativeAd? = null
    private var nativeLoading1: Boolean = false

    fun loadNativeAd1(
        context: Context, adUnitId: String
    ) {
        if (mNativeAd1 != null) {
            return
        }
        nativeLoading1 = true
        Log.d("AdmobNative", "Native 1 requested: ")
        Utils.logAnalytic("The native ad requested.")
        val adLoader = AdLoader.Builder(context, adUnitId)
            .forNativeAd { nativeAd ->
                mNativeAd1 = nativeAd
            }
            .withAdListener(object : AdListener() {
                override fun onAdLoaded() {
                    nativeLoading1 = false
//                    Toast.makeText(context, "native onAdLoaded 1: ", Toast.LENGTH_SHORT).show()
                    Log.d("AdmobNative", "onAdLoaded 1: ")
                    Utils.logAnalytic("The native ad loaded.")
                }

                override fun onAdFailedToLoad(p0: LoadAdError) {
                    mNativeAd1 = null // Reset mNativeAd1 on failure
                    nativeLoading1 = false
//                    Toast.makeText(context, "native onAdFailedToLoad 1: ", Toast.LENGTH_SHORT)
//                        .show()
                    Log.d("AdmobNative", "onAdFailedToLoad 1: ")
                    Utils.logAnalytic("The native ad failed admob.")
                }

                override fun onAdClicked() {
                    if (mNativeAd1 != null) mNativeAd1 = null
                    nativeLoading1 = false
                    loadNativeAd1(context, adUnitId)
                    Log.d("AdmobNative", "onAdClicked 1: ")
                    Utils.logAnalytic("The native ad clicked.")
                }

                override fun onAdImpression() {
                    if (mNativeAd1 != null) mNativeAd1 = null
                    nativeLoading1 = false
//                    Toast.makeText(context, "native onAdImpression 1: ", Toast.LENGTH_SHORT).show()
                    Log.d("AdmobNative", "onAdImpression 1: ")
                    Utils.logAnalytic("The native ad impression.")
                    loadNativeAd1(context, adUnitId)
                }
            })
            .build()

        adLoader.loadAd(AdRequest.Builder().build())
    }

    fun showNative(
        context: Context,
        container: FrameLayout,
        adUnitId: String
    ) {
        mNativeAd1?.let {
            showNativeAd1(mNativeAd1!!, container)
        }

        if (!nativeLoading1) loadNativeAd1(context, adUnitId)
    }

    fun showNativeAd1(nativeAd1: NativeAd, nativeAdContainer: FrameLayout) {
        Log.d("AdmobNative", "showNativeAd1 : ")
        Utils.logAnalytic("The native ad showed.")
        val adView = LayoutInflater.from(nativeAdContainer.context)
            .inflate(R.layout.ad_native, null) as NativeAdView
        populateNativeAdView(nativeAd1, adView)
        nativeAdContainer.removeAllViews()
        nativeAdContainer.addView(adView)
    }

    private fun populateNativeAdView(nativeAd: NativeAd, adView: NativeAdView) {
        // Set the media view.
        adView.mediaView = adView.findViewById<MediaView>(R.id.ad_media)

        // Set other ad assets.
        adView.headlineView = adView.findViewById<View>(R.id.ad_headline)
        /*adView.bodyView = adView.findViewById<View>(R.id.ad_body)*/
        adView.callToActionView = adView.findViewById<View>(R.id.ad_call_to_action)
        adView.iconView = adView.findViewById<View>(R.id.ad_app_icon)
        /*
                adView.priceView = adView.findViewById<View>(R.id.ad_price)
        */
        /*adView.starRatingView = adView.findViewById<View>(R.id.ad_stars)*/
        /*
                adView.storeView = adView.findViewById<View>(R.id.ad_store)
        */
        adView.advertiserView = adView.findViewById<View>(R.id.ad_advertiser)

        // The headline and mediaContent are guaranteed to be in every UnifiedNativeAd.
        (adView.headlineView as TextView?)?.setText(nativeAd.getHeadline())
        adView.mediaView!!.mediaContent = nativeAd.getMediaContent()

        // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
        // check before trying to display them.
        /*if (nativeAd.getBody() == null) {
            adView.bodyView!!.visibility = View.INVISIBLE
        } else {
            adView.bodyView!!.visibility = View.VISIBLE
            (adView.bodyView as TextView?)?.setText(nativeAd.getBody())
        }*/
        if (nativeAd.getCallToAction() == null) {
            adView.callToActionView!!.visibility = View.INVISIBLE
        } else {
            adView.callToActionView!!.visibility = View.VISIBLE
            (adView.callToActionView as Button?)?.setText(nativeAd.getCallToAction())
        }
        if (nativeAd.getIcon() == null) {
            adView.iconView!!.visibility = View.GONE
        } else {
            (adView.iconView as ImageView?)!!.setImageDrawable(
                nativeAd.getIcon()!!.getDrawable()
            )
            adView.iconView!!.visibility = View.VISIBLE
        }
        /*if (nativeAd.getPrice() == null) {
            adView.priceView!!.visibility = View.INVISIBLE
        } else {
            adView.priceView!!.visibility = View.VISIBLE
            (adView.priceView as TextView?)?.setText(nativeAd.getPrice())
        }*/
        /*if (nativeAd.getStore() == null) {
            adView.storeView!!.visibility = View.INVISIBLE
        } else {
            adView.storeView!!.visibility = View.VISIBLE
            (adView.storeView as TextView?)?.setText(nativeAd.getStore())
        }*/
        /* if (nativeAd.getStarRating() == null) {
             adView.starRatingView!!.visibility = View.INVISIBLE
         } else {
             (adView.starRatingView as RatingBar?)
                 ?.setRating(nativeAd.getStarRating()!!.toFloat())
             adView.starRatingView!!.visibility = View.VISIBLE
         }*/
        if (nativeAd.getAdvertiser() == null) {
            adView.advertiserView!!.visibility = View.INVISIBLE
        } else {
            (adView.advertiserView as TextView?)?.setText(nativeAd.getAdvertiser())
            adView.advertiserView!!.visibility = View.VISIBLE
        }

        // This method tells the Google Mobile Ads SDK that you have finished populating your
        // native ad view with this native ad.
        adView.setNativeAd(nativeAd)
    }
    private fun populateNativeAdViewWithoutMedia(nativeAd: NativeAd, adView: NativeAdView) {
        // Set other ad assets.
        adView.headlineView = adView.findViewById<View>(R.id.ad_headline)
        adView.bodyView = adView.findViewById<View>(R.id.ad_body)
        adView.callToActionView = adView.findViewById<View>(R.id.ad_call_to_action)
        adView.iconView = adView.findViewById<View>(R.id.ad_app_icon)
        adView.starRatingView = adView.findViewById<View>(R.id.ad_stars)
        adView.advertiserView = adView.findViewById<View>(R.id.ad_advertiser)

        // The headline is guaranteed to be in every NativeAd.
        (adView.headlineView as TextView?)?.text = nativeAd.headline

        // These assets aren't guaranteed to be in every NativeAd, so it's important to
        // check before trying to display them.
        if (nativeAd.body == null) {
            adView.bodyView!!.visibility = View.INVISIBLE
        } else {
            adView.bodyView!!.visibility = View.VISIBLE
            (adView.bodyView as TextView?)?.text = nativeAd.body
        }
        if (nativeAd.callToAction == null) {
            adView.callToActionView!!.visibility = View.INVISIBLE
        } else {
            adView.callToActionView!!.visibility = View.VISIBLE
            (adView.callToActionView as Button?)?.text = nativeAd.callToAction
        }
        if (nativeAd.icon == null) {
            adView.iconView!!.visibility = View.GONE
        } else {
            (adView.iconView as ImageView?)!!.setImageDrawable(nativeAd.icon!!.drawable)
            adView.iconView!!.visibility = View.VISIBLE
        }

        if (nativeAd.starRating == null) {
            adView.starRatingView!!.visibility = View.INVISIBLE
        } else {
            (adView.starRatingView as RatingBar?)?.rating = nativeAd.starRating!!.toFloat()
            adView.starRatingView!!.visibility = View.VISIBLE
        }
        if (nativeAd.advertiser == null) {
            adView.advertiserView!!.visibility = View.INVISIBLE
        } else {
            (adView.advertiserView as TextView?)?.text = nativeAd.advertiser
            adView.advertiserView!!.visibility = View.VISIBLE
        }

        // This method tells the Google Mobile Ads SDK that you have finished populating your
        // native ad view with this native ad.
        adView.setNativeAd(nativeAd)
    }
}