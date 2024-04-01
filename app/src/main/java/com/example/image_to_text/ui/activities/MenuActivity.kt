package com.example.image_to_text.ui.activities

import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.ImageView
import com.example.image_to_text.R
import com.example.image_to_text.databinding.ActivityMenuBinding
import com.example.image_to_text.ui.ApplicationClass
import com.example.image_to_text.ui.SubscriptionManager.SubscriptionManager
import com.example.image_to_text.ui.TranslationsActivity
import com.example.image_to_text.ui.ads.AdmobInter
import com.example.image_to_text.ui.inapp.InAppActivity
import com.example.image_to_text.ui.splashscreen.SplashActivity

class MenuActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMenuBinding
    private lateinit var progressDialog: Dialog
    private lateinit var subscriptionManager: SubscriptionManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)
        subscriptionManager = SubscriptionManager(this)

        val isMonthlySubscriptionActive = subscriptionManager.isMonthlySubscriptionActive()
        val isYearlySubscriptionActive = subscriptionManager.isYearlySubscriptionActive()
        val isLifetimeSubscriptionActive = subscriptionManager.isLifetimeSubscriptionActive()

        if (isMonthlySubscriptionActive || isYearlySubscriptionActive || isLifetimeSubscriptionActive) {
            // User is subscribed, hide ads
            binding.nativeAdContainer.visibility=View.GONE
            //Toast.makeText(this, "Thank you for subscribing!", Toast.LENGTH_SHORT).show()
        } else {
            // User is not subscribed, show ads
            SplashActivity.admobNative.showNative(this,binding.nativeAdContainer , SplashActivity.admobNativeId)

        }
        // Set OnClickListener on the close ImageView
        val closeImageView: ImageView = findViewById(R.id.close)

        // Set OnClickListener on the close ImageView
        closeImageView.setOnClickListener {
            // Handle the click event here
            finish() // Close the activity
        }
        // Set click listeners for menu items
        binding.removeAdsTextView.setOnClickListener {
            startActivity(Intent(this, InAppActivity::class.java))
        }

        binding.translateTextToTextTextView.setOnClickListener {
            SplashActivity.admobInter.showInterAd(this@MenuActivity) {
                SplashActivity.admobInter.loadInterAd(
                    this,
                    SplashActivity.admobInterId
                )
                startActivity(Intent(this, TranslationsActivity::class.java))
            }
            if(AdmobInter.isClicked){
                startActivity(Intent(this, TranslationsActivity::class.java))
            }

        }

        // Uncomment the lines below to add click listeners for other menu items
        /*
        binding.appLanguageTextView.setOnClickListener {
            // Handle click on App Language item
        }
        */

        binding.shareAppTextView.setOnClickListener {
            val websiteUri = Uri.parse("https://play.google.com/store/apps/details?id=com.image.to.text.ocrscanner.textconverter.extract.text.translateapp")
            val intent = Intent(Intent.ACTION_VIEW, websiteUri)
            startActivity(intent)
        }

        binding.privacyPolicyTextView.setOnClickListener {
            val websiteUri = Uri.parse("https://sites.google.com/view/image-to-text-ocr-extract/home")
            val intent = Intent(Intent.ACTION_VIEW, websiteUri)
            startActivity(intent)
        }

        binding.rateAppTextView.setOnClickListener {
            progressDialog = Dialog(this)
            progressDialog.setCanceledOnTouchOutside(false)
            progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE) // Hide the title bar
            progressDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) // Set background to transparent
            progressDialog.setContentView(R.layout.rate_dialogue)

            val imageView1 = progressDialog.findViewById<ImageView>(R.id.rate)
            val imageView2 = progressDialog.findViewById<ImageView>(R.id.cancel)

            imageView1.setOnClickListener {
                // Handle click on imageView1
                progressDialog.dismiss()

                // URI for opening the rating page on Play Store
                val appPackageName = packageName
                try {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appPackageName")))
                } catch (e: ActivityNotFoundException) {
                    // If Play Store app is not available, open the website version
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")))
                }
            }

            imageView2.setOnClickListener {
                // Handle click on imageView2
                // For example:
                progressDialog.dismiss()
            }

            progressDialog.show()

        }

        binding.moreAppTextView.setOnClickListener {
            val websiteUri = Uri.parse("https://play.google.com/store/apps/developer?id=Sparx+Developer")
            val intent = Intent(Intent.ACTION_VIEW, websiteUri)
            startActivity(intent)
        }

        binding.termsAndConditionsTextView.setOnClickListener {
            val websiteUri = Uri.parse("https://sites.google.com/view/terms-and-conditions-image-to-/home")
            val intent = Intent(Intent.ACTION_VIEW, websiteUri)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        ApplicationClass.counter++
    }
}