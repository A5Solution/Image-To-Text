package com.example.image_to_text.ui.splashscreen
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import com.example.image_to_text.R
import com.example.image_to_text.ui.MainActivity
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

class SplashActivity : AppCompatActivity() {

    private val SPLASH_SCREEN_DURATION = 3000 // 3 seconds
    private var progressBar: ProgressBar? = null
    private var adView: AdView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

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
                // Ad loaded successfully
                startProgressBar()
            }

            override fun onAdFailedToLoad(p0: LoadAdError) {
                // Ad failed to load
                startMainActivity()
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
}

