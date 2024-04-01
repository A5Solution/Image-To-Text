package com.example.image_to_text.ui

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.FileProvider
import com.example.image_to_text.R
import com.example.image_to_text.databinding.ActivityViewTranslationBinding
import com.example.image_to_text.ui.SubscriptionManager.SubscriptionManager
import com.example.image_to_text.ui.database.DatabaseHelper
import com.example.image_to_text.ui.splashscreen.SplashActivity
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ViewTranslationActivity : AppCompatActivity() {
    private lateinit var back: ImageView
    private lateinit var imageView: ImageView
    private lateinit var copy: ImageView
    private lateinit var share: ImageView
    private lateinit var save: ImageView
    private lateinit var sourceLanguage: TextView
    private var CHANNEL_ID = "your_channel_id"
    private  val CHANNEL_NAME = "Your Channel Name"
    private val NOTIFICATION_ID = 1001
    private var mInterstitialAd: InterstitialAd? = null
    private lateinit var subscriptionManager: SubscriptionManager
    private var textToSpeech: TextToSpeech? = null
    private lateinit var binding: ActivityViewTranslationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewTranslationBinding.inflate(layoutInflater)
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

        val filePath = intent.getStringExtra("imageFilePath")
        val yourString = intent.getStringExtra("yourString")

        val bitmap = BitmapFactory.decodeFile(filePath)
        back = findViewById(R.id.back)
        imageView = findViewById(R.id.imageView)
        copy = findViewById(R.id.copy)
        share = findViewById(R.id.share)
        save = findViewById(R.id.save)
        imageView = findViewById(R.id.imageView)
        imageView = findViewById(R.id.imageView)
        sourceLanguage = findViewById(R.id.sourceLanguage)

        if (filePath.equals(null)){
            imageView.visibility=View.GONE
        }
        back.setOnClickListener {
                finish()
        }

        copy.setOnClickListener {
            yourString?.let { it1 -> copyTextToClipboard(it1) }
        }

        save.setOnClickListener {
            yourString?.let { it1 -> saveImageAndTextToSQLite(applicationContext,bitmap, it1) }
        }

        share.setOnClickListener {
            yourString?.let { it1 -> shareImageAndText(bitmap, it1) }
        }
        imageView.setImageBitmap(bitmap)
        sourceLanguage.text = yourString

        var isSpeaking = false

// Define a variable to store the text being spoken
        var spokenText: String? = null

// Initialize TextToSpeech in your activity
        textToSpeech = TextToSpeech(
            applicationContext
        ) { status ->
            if (status != TextToSpeech.ERROR) {
                // Set language to Arabic
                textToSpeech?.language = Locale("ar")
            } else {
                Toast.makeText(
                    this@ViewTranslationActivity,
                    "Text-to-Speech initialization failed",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        val speaker: ImageView = findViewById(R.id.speaker)

// Set click listener for the ImageView
        speaker.setOnClickListener {
            // Toggle playback state
            isSpeaking = !isSpeaking

            // Check if text is empty
            if (sourceLanguage.text.isNullOrEmpty()) {
                Toast.makeText(this, "Translate first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // If speaking, pause; otherwise, speak the text
            if (isSpeaking) {
                spokenText = sourceLanguage.text.toString()
                speakText(spokenText!!)
            } else {
                stopSpeaking()
            }
        }
    }
    private fun speakText(text: String) {
        // Speak the text
        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    // Function to stop speaking
    private fun stopSpeaking() {
        textToSpeech?.stop()
    }
    private fun saveImageAndTextToSQLite(context: Context, bitmap: Bitmap, text: String) {
        val databaseHelper = DatabaseHelper(context)
        val imageData = convertBitmapToByteArray(bitmap)
        val insertedRowId = databaseHelper.insertData( text)
        if (insertedRowId != -1L) {
            //Toast.makeText(context, "Image and text saved to SQLite database!", Toast.LENGTH_SHORT).show()
            val handler = Handler(Looper.getMainLooper())
            val progressBar: ProgressBar
            progressBar = findViewById(R.id.progressBar)
            progressBar.visibility = View.VISIBLE
            handler.postDelayed({
                Toast.makeText(context, "Image Saved to Collections!", Toast.LENGTH_SHORT).show()
                progressBar.visibility = View.GONE
            }, 2000)
            //showNotification(context, "Image and text saved", "Image and text saved to SQLite database!")
        } else {
            //Toast.makeText(context, "Failed to save image and text to SQLite database", Toast.LENGTH_SHORT).show()
        }
    }
    private fun convertBitmapToByteArray(bitmap: Bitmap): ByteArray {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        return outputStream.toByteArray()
    }
    private fun copyTextToClipboard(text: String) {
        val clipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("text", text)
        clipboardManager.setPrimaryClip(clipData)
        Toast.makeText(this, "Text copied!", Toast.LENGTH_SHORT).show()
    }
    private fun showNotification(context: Context, title: String, message: String) {
        val notificationManager = NotificationManagerCompat.from(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create notification channel
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Image Saved Notification",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

            // Build the notification
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Image Saved")
            .setContentText("Image has been saved successfully")
            .setSmallIcon(R.drawable.notification)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

            // Show the notification
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
                return
            }
    }


    private fun shareImageAndText(bitmap: Bitmap, text: String) {
        // Save the image to a temporary file
        val directory = File(filesDir, "images")
        if (!directory.exists()) {
            directory.mkdirs()
        }
        val imageFile = File(directory, "image.png")
        FileOutputStream(imageFile).use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
            it.flush()
        }

        // Create a share intent
        val imageUri = FileProvider.getUriForFile(this, "com.example.image_to_text.fileprovider", imageFile)
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, imageUri)
            putExtra(Intent.EXTRA_TEXT, text) // Attach the text as the message body
            type = "image/png"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        // Launch the share intent
        startActivity(Intent.createChooser(shareIntent, "Share via"))
    }

    override fun onPause() {
        super.onPause()
        textToSpeech?.stop()
    }

    override fun onResume() {
        super.onResume()
        ApplicationClass.counter++
    }
}