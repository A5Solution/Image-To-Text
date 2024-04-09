package com.example.image_to_text.ui.splashscreen

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.android.billingclient.api.*
import com.example.image_to_text.R
import com.example.image_to_text.ui.MainActivity
import com.example.image_to_text.ui.SubscriptionManager.SubscriptionManager
import com.android.billingclient.api.Purchase
import com.example.image_to_text.ui.Utils.Companion.logAnalytic
import com.example.image_to_text.ui.ads.AdmobInter
import com.example.image_to_text.ui.ads.AdmobNative
import com.example.image_to_text.ui.ads.AdmobOpen
import com.google.android.gms.ads.MobileAds
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity(), PurchasesUpdatedListener {

    private val SPLASH_SCREEN_DURATION = 1000 // 3 seconds
    private var progressBar: ProgressBar? = null
    //private var adView: AdView? = null
    private lateinit var subscriptionManager: SubscriptionManager
    private lateinit var billingClient: BillingClient
    private var isAnimating = false
    private lateinit var scanImageView: ImageView
    private lateinit var scanLine: ImageView
    private var isAdLoaded = false

    companion object {
        val admobInter = AdmobInter()
        var admobInterId: String = ""
        var isPermissionPopupVisible: Boolean = true
        lateinit var admobNative: AdmobNative
        lateinit var admobNativeId: String
        val admobOpen = AdmobOpen()
        var admobOpenId = ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        admobNative = AdmobNative()
        admobNativeId = getString(R.string.admob_native_id)
        admobNative.loadNativeAd1(this, admobNativeId)
        scanImageView = findViewById(R.id.scan)
        scanLine = findViewById(R.id.linescan)
        admobOpenId = getString(R.string.admob_open_id)
        subscriptionManager = SubscriptionManager(this)

        MobileAds.initialize(this) {
            logAnalytic("Splash ads initialized")
            lifecycleScope.launch {
                val isLifetimeSubscriptionActive = subscriptionManager.isLifetimeSubscriptionActive()

                if (isLifetimeSubscriptionActive) {
                    startProgressBar()
                } else {
                    admobOpen.loadOpenAd(this@SplashActivity) { loaded ->
                        isAdLoaded = loaded
                        if (!loaded) {
                            logAnalytic("Splash open app Ad not loaded")
                            logAnalytic("Moved Next from splash")
                            startProgressBar()
                            return@loadOpenAd
                        }

                        logAnalytic("Splash open app Ad loaded")
                        admobOpen.showOpenAd(this@SplashActivity) {
                            logAnalytic("Splash open app Ad showed")
                            startProgressBar()
                        }
                    }
                }
            }
        }
        val isLifetimeSubscriptionActive = subscriptionManager.isLifetimeSubscriptionActive()
        if (isLifetimeSubscriptionActive) {
            hideAds()
        } else {
            showAds()
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
                    lifetime()
                } else {
                }
            }

            override fun onBillingServiceDisconnected() {
            }
        })

        // Find splash image view

        startAnimation()
        // Find progress bar view
        progressBar = findViewById<ProgressBar>(R.id.progress_bar)

        // Load banner ad
        //adView = findViewById<AdView>(R.id.banner_ad)
        //val adRequest = AdRequest.Builder().build()
        //adView?.loadAd(adRequest)

        // Set ad listener
        /*adView?.adListener = object : AdListener() {
            override fun onAdLoaded() {
                startProgressBar()
            }

            override fun onAdFailedToLoad(p0: LoadAdError) {
                startProgressBar()
            }
        }*/
    }
    private fun hideAds() {

    }

    private fun showAds() {
        admobInterId = getString(R.string.admob_inter_id)
        admobNativeId = getString(R.string.admob_native_id)
        MobileAds.initialize(this) {
            admobInter.loadInterAd(this, admobInterId)
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
            if (subscriptionManager.isMonthlySubscriptionActive() ||
                subscriptionManager.isYearlySubscriptionActive() ||
                subscriptionManager.isLifetimeSubscriptionActive()) {
                startProgressBar()
            }else{
                //adView?.visibility = View.VISIBLE
                val admobInterId = getString(R.string.admob_inter_id)

                MobileAds.initialize(this) {
                    admobInter.loadInterAd(this, admobInterId)
                }
            }

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
                !subscriptionManager.isYearlySubscriptionActive()) {
                //adView?.visibility = View.VISIBLE
            }

        }
    }
    private fun lifetime() {
        billingClient.queryPurchasesAsync(BillingClient.SkuType.INAPP) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                if (!purchases.isNullOrEmpty()) {
                    handlePurchases(purchases)
                    return@queryPurchasesAsync
                }
            }
            // Show ad if not subscribed
            if (!subscriptionManager.isLifetimeSubscriptionActive()) {
                //adView?.visibility = View.VISIBLE
            }
        }
    }

    private fun handlePurchases(purchases: List<Purchase>) {
        // Handle the purchases here
        var hasLifetimeSubscription = false

        for (purchase in purchases) {
            val skuDetails = purchase.skus.firstOrNull()
            when (skuDetails) {
                "monthly_subscription_sku" -> subscriptionManager.setMonthlySubscriptionActive(true)
                "yearly_subscription_sku" -> subscriptionManager.setYearlySubscriptionActive(true)
                "lifetime_subscription_sku" -> {

                    subscriptionManager.setLifetimeSubscriptionActive(true)
                    hasLifetimeSubscription = true

                }
                else -> {
                    // Handle unknown SKUs
                }
            }
        }
        if (hasLifetimeSubscription) {
            startProgressBar()
            startMainActivity()
        } else {
            // If the user does not have a lifetime subscription, continue with the progress bar animation
            startProgressBar()
        }
    }
    private fun startAnimation() {
        // Check if animation is already running
        if (!isAnimating) {
            // Create and start the animator for translationY property
            val animator = ObjectAnimator.ofFloat(scanImageView, "translationY", 0f, -300f).apply {
                duration = 2000 // Set the duration of each animation cycle
                repeatCount = ObjectAnimator.INFINITE // Repeat the animation infinitely
                repeatMode = ObjectAnimator.REVERSE // Reverse the animation direction after each cycle
                start() // Start the animation
            }
            val animator1 = ObjectAnimator.ofFloat(scanLine, "translationY", 0f, -300f).apply {
                duration = 2000 // Set the duration of each animation cycle
                repeatCount = ObjectAnimator.INFINITE // Repeat the animation infinitely
                repeatMode = ObjectAnimator.REVERSE // Reverse the animation direction after each cycle
                start() // Start the animation
            }
            isAnimating = true
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Stop the animation when the activity is destroyed to prevent memory leaks
        scanImageView.clearAnimation()
    }
}
