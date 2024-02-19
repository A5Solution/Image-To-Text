package com.example.image_to_text.ui.inapp

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.billingclient.api.*
import com.example.image_to_text.R
import com.example.image_to_text.ui.SubscriptionManager.SubscriptionManager

class InAppActivity : AppCompatActivity(), PurchasesUpdatedListener {

    private lateinit var billingClient: BillingClient
    private lateinit var subscriptionManager: SubscriptionManager

    private var monthlySkuDetails: SkuDetails? = null
    private var yearlySkuDetails: SkuDetails? = null
    private var lifetimeSkuDetails: SkuDetails? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_in_app)

        billingClient = BillingClient.newBuilder(this)
            .setListener(this)
            .enablePendingPurchases()
            .build()

        subscriptionManager = SubscriptionManager(this)

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    loadSkuDetails()
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
    }

    private fun loadSkuDetails() {
        val skuList = listOf("monthly_subscription_sku", "yearly_subscription_sku", "lifetime_subscription_sku")
        val params = SkuDetailsParams.newBuilder()
            .setType(BillingClient.SkuType.SUBS)
            .setSkusList(skuList)
            .build()

        billingClient.querySkuDetailsAsync(params) { billingResult, skuDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && skuDetailsList != null) {
                for (skuDetails in skuDetailsList) {
                    when (skuDetails.sku) {
                        "monthly_subscription_sku" -> monthlySkuDetails = skuDetails
                        "yearly_subscription_sku" -> yearlySkuDetails = skuDetails
                        "lifetime_subscription_sku" -> lifetimeSkuDetails = skuDetails
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
            "monthly_subscription_sku" -> {
                subscriptionManager.setMonthlySubscriptionActive(true)
                removeAdsForMonth()
            }
            "yearly_subscription_sku" -> {
                subscriptionManager.setYearlySubscriptionActive(true)
                removeAdsForYear()
            }
            "lifetime_subscription_sku" -> {
                subscriptionManager.setLifetimeSubscriptionActive(true)
                removeAdsPermanently()
            }
            else -> showToast("Unknown SKU: $sku")
        }
    }

    private fun removeAdsForMonth() {
        // Implement logic to remove ads for a month

    }

    private fun removeAdsForYear() {
        // Implement logic to remove ads for a year
    }

    private fun removeAdsPermanently() {
        // Implement logic to remove ads permanently
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}