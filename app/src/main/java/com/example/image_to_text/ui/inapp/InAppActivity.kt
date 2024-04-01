package com.example.image_to_text.ui.inapp

import android.animation.ObjectAnimator
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.billingclient.api.*
import com.example.image_to_text.R
import com.example.image_to_text.ui.ApplicationClass
import com.example.image_to_text.ui.SubscriptionManager.SubscriptionManager

class InAppActivity : AppCompatActivity(), PurchasesUpdatedListener {

    private lateinit var billingClient: BillingClient
    private lateinit var subscriptionManager: SubscriptionManager

    private var monthlySkuDetails: SkuDetails? = null
    private var yearlySkuDetails: SkuDetails? = null
    private var lifetimeSkuDetails: SkuDetails? = null
    private var close: ImageView? = null
    private var price_monthly: TextView? = null
    private var price_yearly: TextView? = null
    private var price_life_time: TextView? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_in_app)
        price_monthly=findViewById(R.id.monthly_price)
        price_yearly=findViewById(R.id.yearly_price)
        price_life_time=findViewById(R.id.life_time_price)
        close=findViewById(R.id.close)
        close?.setOnClickListener(){
            finish()
        }
        billingClient = BillingClient.newBuilder(this)
            .setListener(this)
            .enablePendingPurchases()
            .build()

        subscriptionManager = SubscriptionManager(this)

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    loadSkuDetails()
                    loadLifeTime()
                }
            }

            override fun onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to Google Play by calling the startConnection() method.
            }
        })

        val monthlyImageView = findViewById<ImageView>(R.id.monthly)
        val yearlyImageView = findViewById<ImageView>(R.id.yearly)
        val lifetimeImageView = findViewById<ImageView>(R.id.lifetime)

        monthlyImageView.setOnClickListener { initiatePurchase(monthlySkuDetails) }
        yearlyImageView.setOnClickListener { initiatePurchase(yearlySkuDetails) }
        lifetimeImageView.setOnClickListener { initiatePurchase(lifetimeSkuDetails) }
        val imageView = findViewById<ImageView>(R.id.imageView2)

        // Define the initial position of the ImageView
        imageView.translationX = 1000f // Adjust the value as per your requirement

        // Create an ObjectAnimator to move the ImageView from right to its current position
        val animator = ObjectAnimator.ofFloat(imageView, "translationX", 0f)
        animator.duration = 1000 // Set the duration of the animation in milliseconds

        // Start the animation
        animator.start()
    }
    private fun loadLifeTime() {
        val skuList = listOf(
            "img_sparx_lifetime_purchase"
        )
        val params = SkuDetailsParams.newBuilder()
            .setType(BillingClient.SkuType.INAPP) // Change to INAPP for in-app products
            .setSkusList(skuList)
            .build()

        billingClient.querySkuDetailsAsync(params) { billingResult, skuDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && skuDetailsList != null) {
                for (skuDetails in skuDetailsList) {
                    val sku = skuDetails.sku
                    when (sku) {
                        "img_sparx_lifetime_purchase" -> {
                            lifetimeSkuDetails = skuDetails
                            runOnUiThread {
                                price_life_time?.text = skuDetails.price
                            }
                        }
                    }
                }
            } else {
                // Handle error case when fetching SKU details fails
            }
        }
    }

    private fun loadSkuDetails() {
        val skuList = listOf(
            "img_sparx_one_month_subscription",
            "img_sparx_yearly_subcription"
        )
        val params = SkuDetailsParams.newBuilder()
            .setType(BillingClient.SkuType.SUBS) // Keep it SUBS for subscriptions
            .setSkusList(skuList)
            .build()

        billingClient.querySkuDetailsAsync(params) { billingResult, skuDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && skuDetailsList != null) {
                for (skuDetails in skuDetailsList) {
                    val sku = skuDetails.sku
                    when (sku) {
                        "img_sparx_one_month_subscription" -> {
                            monthlySkuDetails = skuDetails
                            runOnUiThread {
                                price_monthly?.text = skuDetails.price
                            }
                        }
                        "img_sparx_yearly_subcription" -> {
                            yearlySkuDetails = skuDetails
                            runOnUiThread {
                                price_yearly?.text = skuDetails.price
                            }
                        }
                    }
                }
            } else {
                // Handle error case when fetching SKU details fails
            }
        }
    }


    private fun initiatePurchase(skuDetails: SkuDetails?) {
        skuDetails?.let { details ->
            val flowParams = BillingFlowParams.newBuilder()
                .setSkuDetails(details)
                .build()
            billingClient.launchBillingFlow(this, flowParams)
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                handlePurchase(purchase)
            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            subscriptionManager.setMonthlySubscriptionActive(false)
            // Handle user cancellation
            Log.e("cancel","cancelled")
        } else {
            Log.e("error","error")
            // Handle other errors
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        val sku = purchase.skus.firstOrNull()
        when (sku) {
            "img_sparx_one_month_subscription" -> {
                subscriptionManager.setMonthlySubscriptionActive(true)

            }
            "img_sparx_yearly_subcription" -> {
                subscriptionManager.setYearlySubscriptionActive(true)

            }
            "img_sparx_lifetime_purchase" -> {
                subscriptionManager.setLifetimeSubscriptionActive(true)
                loadLifeTime()
            }
            //else -> showToast("Unknown SKU: $sku")
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        Log.d("InAppActivity", "onDestroy() called")
        billingClient.endConnection()
    }

    override fun onResume() {
        super.onResume()
        ApplicationClass.counter++
        Log.d("InAppActivity", "onResume called")

        // Yahan par additional code bhi add kar sakte hain, jaise MainActivity pe navigate karne ki check
    }
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

}