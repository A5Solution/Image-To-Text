package com.example.image_to_text.ui.splashscreen

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.android.billingclient.api.*
import com.example.image_to_text.R
import com.example.image_to_text.ui.MainActivity
import com.example.image_to_text.ui.SubscriptionManager.SubscriptionManager
import com.android.billingclient.api.Purchase
import com.example.image_to_text.ui.AdmobInter
import com.google.android.gms.ads.MobileAds

class SplashActivity : AppCompatActivity(), PurchasesUpdatedListener {

    private val SPLASH_SCREEN_DURATION = 4000 // 3 seconds
    private var progressBar: ProgressBar? = null
    private var adView: AdView? = null
    private lateinit var subscriptionManager: SubscriptionManager
    private lateinit var billingClient: BillingClient
    companion object {
        val admobInter = AdmobInter()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val admobInterId = getString(R.string.inter_ad_unit_id)

        subscriptionManager = SubscriptionManager(this)

        MobileAds.initialize(this) {
            admobInter.loadInterAd(this, admobInterId)
        }

        // Initialize BillingClient
        billingClient = BillingClient.newBuilder(this)
            .setListener(this)
            .enablePendingPurchases()
            .build()

        // Connect to BillingClient
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    // Query purchases after billing setup
                    queryPurchases()
                } else {
                    startMainActivity()
                }
            }

            override fun onBillingServiceDisconnected() {
                startMainActivity()
            }
        })

        // Find splash image view
        val splashImage = findViewById<ImageView>(R.id.splash_image)

        // Define the animation (from right to left)
        val animation = TranslateAnimation(
            Animation.RELATIVE_TO_PARENT, 1.0f,
            Animation.RELATIVE_TO_PARENT, 0.0f,
            Animation.RELATIVE_TO_PARENT, 0.0f,
            Animation.RELATIVE_TO_PARENT, 0.0f
        )

        // Set animation duration
        animation.duration = 1000 // Adjust duration as needed

        // Start the animation
        splashImage.startAnimation(animation)

        // Find progress bar view
        progressBar = findViewById<ProgressBar>(R.id.progress_bar)

        // Load banner ad
        adView = findViewById<AdView>(R.id.banner_ad)
        val adRequest = AdRequest.Builder().build()
        adView?.loadAd(adRequest)

        // Set ad listener
        adView?.adListener = object : AdListener() {
            override fun onAdLoaded() {
                startProgressBar()
            }

            override fun onAdFailedToLoad(p0: LoadAdError) {
                startProgressBar()
            }
        }
    }

    private fun startProgressBar() {
        // Start the progress bar animation
        val animationDuration = SPLASH_SCREEN_DURATION.toLong()
        progressBar?.progress = 0
        progressBar?.max = 100 // Set max progress value
        progressBar?.animate()?.apply {
            duration = animationDuration
            setUpdateListener { animation ->
                val progress = (animation.animatedFraction * progressBar?.max!!).toInt()
                progressBar?.progress = progress
            }
            start()
        }

        // Start the next activity after the animation duration
        Handler().postDelayed({
            startMainActivity()
        }, animationDuration)
    }

    private fun startMainActivity() {
        // Start MainActivity and finish the SplashActivity
        startActivity(Intent(this@SplashActivity, MainActivity::class.java))
        finish()
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            handlePurchases(purchases)
        } else {
            // Show ad if not subscribed
            if (!subscriptionManager.isMonthlySubscriptionActive() &&
                !subscriptionManager.isYearlySubscriptionActive() &&
                !subscriptionManager.isLifetimeSubscriptionActive()) {
                adView?.visibility = View.VISIBLE
            }
            startProgressBar()
        }
    }

    private fun queryPurchases() {
        billingClient.queryPurchasesAsync(BillingClient.SkuType.SUBS) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                if (!purchases.isNullOrEmpty()) {
                    handlePurchases(purchases)
                    return@queryPurchasesAsync
                }
            }
            // Show ad if not subscribed
            if (!subscriptionManager.isMonthlySubscriptionActive() &&
                !subscriptionManager.isYearlySubscriptionActive() &&
                !subscriptionManager.isLifetimeSubscriptionActive()) {
                adView?.visibility = View.VISIBLE
            }
            startProgressBar()
        }
    }

    private fun handlePurchases(purchases: List<Purchase>) {
        // Handle the purchases here
        for (purchase in purchases) {
            val skuDetails = purchase.skus.firstOrNull()
            when (skuDetails) {
                "monthly_subscription_sku" -> subscriptionManager.setMonthlySubscriptionActive(true)
                "yearly_subscription_sku" -> subscriptionManager.setYearlySubscriptionActive(true)
                "lifetime_subscription_sku" -> subscriptionManager.setLifetimeSubscriptionActive(true)
                else -> {
                    // Handle unknown SKUs
                }
            }
        }
        startMainActivity()
    }
}
