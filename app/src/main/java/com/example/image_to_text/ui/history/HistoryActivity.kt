package com.example.image_to_text.ui.history

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.image_to_text.R
import com.example.image_to_text.ui.ApplicationClass
import com.example.image_to_text.ui.SubscriptionManager.SubscriptionManager
import com.example.image_to_text.ui.ads.AdmobInter
import com.example.image_to_text.ui.database.DatabaseHelper
import com.example.image_to_text.ui.splashscreen.SplashActivity
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.VideoOptions
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAd.OnNativeAdLoadedListener
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var back: ImageView
    private lateinit var delete: ImageView
    private lateinit var databaseHelper: DatabaseHelper
    private var mNativeAd: NativeAd? = null
    private lateinit var navContainer: FrameLayout
    private lateinit var subscriptionManager: SubscriptionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)
        subscriptionManager = SubscriptionManager(this)

        navContainer = findViewById(R.id.nativeAdContainer)

        val isMonthlySubscriptionActive = subscriptionManager.isMonthlySubscriptionActive()
        val isYearlySubscriptionActive = subscriptionManager.isYearlySubscriptionActive()
        val isLifetimeSubscriptionActive = subscriptionManager.isLifetimeSubscriptionActive()

        if (isMonthlySubscriptionActive || isYearlySubscriptionActive || isLifetimeSubscriptionActive) {
            // User is subscribed, hide ads
            navContainer.visibility=View.GONE
            //Toast.makeText(this, "Thank you for subscribing!", Toast.LENGTH_SHORT).show()
        } else {
            // User is not subscribed, show ads
            SplashActivity.admobNative.showNative(this,navContainer , SplashActivity.admobNativeId)

        }
        //loadNativeAd()
        recyclerView = findViewById(R.id.recyclerView)
        back = findViewById(R.id.back)
        delete = findViewById(R.id.delete)

        back.setOnClickListener {
            finish()
        }

        delete.setOnClickListener {
            val builder = AlertDialog.Builder(this)

            // Set the dialog message and buttons
            builder.setMessage("Do you want to delete?")
                .setPositiveButton("Delete") { dialog, id ->
                    val adapter = recyclerView.adapter as? ImageTextAdapter
                    val selectedItems = adapter?.getSelectedItems()?.toList() // Convert to List
                    selectedItems?.forEach { position ->
                        adapter.removeItemAt(position)
                    }
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel") { dialog, id ->

                    dialog.dismiss()
                }

            // Create the AlertDialog object and show it
            builder.create().show()

        }

        recyclerView.layoutManager = LinearLayoutManager(this)

        databaseHelper = DatabaseHelper(this)
        val items = loadSavedItems()
        recyclerView.adapter = ImageTextAdapter(this,items.toMutableList(), databaseHelper)


    }

    private fun loadSavedItems(): List<ImageTextItem> {
        val items = mutableListOf<ImageTextItem>()
        val data = databaseHelper.getAllData()
        for (row in data) {
            try {
                //val imageByteArray = row.get("image_data") as ByteArray
                //val bitmap = BitmapFactory.decodeStream(ByteArrayInputStream(imageByteArray))
                val text = row.get("text") as String
                items.add(ImageTextItem(text))
            } catch (e: Exception) {
                Log.e("HistoryActivity", "Error loading data", e)
            }
        }
        return items

    }
    private fun loadNativeAd() {
        val adBuilder =
            AdLoader.Builder(applicationContext, "ca-app-pub-3940256099942544/2247696110")
        adBuilder.forNativeAd(OnNativeAdLoadedListener { nativeAd ->
            if (isDestroyed || isFinishing || isChangingConfigurations) {
                nativeAd.destroy()
                return@OnNativeAdLoadedListener
            }
            if (mNativeAd != null) {
                mNativeAd?.destroy()
            }
            mNativeAd = nativeAd
            val frameLayout = findViewById<FrameLayout>(R.id.frameNative)
            val adView = layoutInflater.inflate(R.layout.native_ad_layout, null) as NativeAdView
            populateNativeAdView(nativeAd, adView)
            frameLayout.removeAllViews()
            frameLayout.addView(adView)
        })
        val videoOptions = VideoOptions.Builder().setStartMuted(true).build()
        val nativeAdOptions = NativeAdOptions.Builder().setVideoOptions(videoOptions).build()
        val adLoader = adBuilder.withAdListener(object : AdListener() {
            override fun onAdClicked() {
                super.onAdClicked()
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                super.onAdFailedToLoad(loadAdError)
                Toast.makeText(this@HistoryActivity, "Error Loading Ad", Toast.LENGTH_SHORT).show()
            }

            override fun onAdLoaded() {
                super.onAdLoaded()
                //Toast.makeText(NativeAdActivity.this, "Loaded", Toast.LENGTH_SHORT).show();
            }
        }).build()
        adLoader.loadAd(AdRequest.Builder().build())
    }

    private fun populateNativeAdView(nativeAd: NativeAd, adView: NativeAdView) {
        // Set the media view.
        adView.mediaView = adView.findViewById(R.id.ad_media)

        // Set other ad assets.
        adView.headlineView = adView.findViewById(R.id.ad_headline)
        adView.bodyView = adView.findViewById(R.id.ad_body)
        adView.callToActionView = adView.findViewById(R.id.ad_call_to_action)
        adView.iconView = adView.findViewById(R.id.ad_app_icon)
        adView.priceView = adView.findViewById(R.id.ad_price)
        adView.starRatingView = adView.findViewById(R.id.ad_stars)
        adView.storeView = adView.findViewById(R.id.ad_store)
        adView.advertiserView = adView.findViewById(R.id.ad_advertiser)

        // The headline and mediaContent are guaranteed to be in every UnifiedNativeAd.
        (adView.headlineView as TextView?)!!.text = nativeAd.headline
        adView.mediaView!!.mediaContent = nativeAd.mediaContent

        // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
        // check before trying to display them.
        if (nativeAd.body == null) {
            adView.bodyView!!.visibility = View.INVISIBLE
        } else {
            adView.bodyView!!.visibility = View.VISIBLE
            (adView.bodyView as TextView?)!!.text = nativeAd.body
        }
        if (nativeAd.callToAction == null) {
            adView.callToActionView!!.visibility = View.INVISIBLE
        } else {
            adView.callToActionView!!.visibility = View.VISIBLE
            (adView.callToActionView as Button?)!!.text = nativeAd.callToAction
        }
        if (nativeAd.icon == null) {
            adView.iconView!!.visibility = View.GONE
        } else {
            (adView.iconView as ImageView?)!!.setImageDrawable(
                nativeAd.icon!!.drawable
            )
            adView.iconView!!.visibility = View.VISIBLE
        }
        if (nativeAd.price == null) {
            adView.priceView!!.visibility = View.INVISIBLE
        } else {
            adView.priceView!!.visibility = View.VISIBLE
            (adView.priceView as TextView?)!!.text = nativeAd.price
        }
        if (nativeAd.store == null) {
            adView.storeView!!.visibility = View.INVISIBLE
        } else {
            adView.storeView!!.visibility = View.VISIBLE
            (adView.storeView as TextView?)!!.text = nativeAd.store
        }
        if (nativeAd.starRating == null) {
            adView.starRatingView!!.visibility = View.INVISIBLE
        } else {
            (adView.starRatingView as RatingBar?)
                ?.setRating(nativeAd.starRating!!.toFloat())
            adView.starRatingView!!.visibility = View.VISIBLE
        }
        if (nativeAd.advertiser == null) {
            adView.advertiserView!!.visibility = View.INVISIBLE
        } else {
            (adView.advertiserView as TextView?)!!.text = nativeAd.advertiser
            adView.advertiserView!!.visibility = View.VISIBLE
        }

        // This method tells the Google Mobile Ads SDK that you have finished populating your
        // native ad view with this native ad.
        adView.setNativeAd(nativeAd)
    }

    data class ImageTextItem( val text: String)

    override fun onResume() {
        super.onResume()
        ApplicationClass.counter++
    }

}
