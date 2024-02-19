package com.example.image_to_text.ui.SubscriptionManager

import android.content.Context
import android.content.SharedPreferences

class SubscriptionManager(context: Context) {
    private val PREF_NAME = "subscription_prefs"
    private val KEY_MONTHLY_SUBSCRIPTION = "monthly_subscription_active"
    private val KEY_YEARLY_SUBSCRIPTION = "yearly_subscription_active"
    private val KEY_LIFETIME_SUBSCRIPTION = "lifetime_subscription_active"

    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun setMonthlySubscriptionActive(active: Boolean) {
        prefs.edit().putBoolean(KEY_MONTHLY_SUBSCRIPTION, active).apply()
    }

    fun setYearlySubscriptionActive(active: Boolean) {
        prefs.edit().putBoolean(KEY_YEARLY_SUBSCRIPTION, active).apply()
    }

    fun setLifetimeSubscriptionActive(active: Boolean) {
        prefs.edit().putBoolean(KEY_LIFETIME_SUBSCRIPTION, active).apply()
    }
}