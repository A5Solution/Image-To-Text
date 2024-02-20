package com.example.image_to_text.ui


import android.app.ProgressDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.example.image_to_text.R
import com.example.image_to_text.ui.SubscriptionManager.SubscriptionManager
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.material.button.MaterialButton
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.*
import kotlin.collections.ArrayList
class TranslationsActivity : AppCompatActivity() {
    private lateinit var sourceLanguage: EditText
    private lateinit var destinationLanguageTv: TextView
    private lateinit var sourceLanguageChooseBtn: MaterialButton
    private lateinit var destinationLanguageChooseBtn: MaterialButton
    private lateinit var translateBtn: MaterialButton
    private lateinit var back: ImageView
    private lateinit var translator: Translator
    private lateinit var translatorOptions: TranslatorOptions
    private lateinit var progressDialog: ProgressDialog

    private val TAG = "MAIN_TAG"
    private var mInterstitialAd: InterstitialAd? = null

    private var sourceLanguageCode = "en"
    private var sourceLanguageTitle = "English"
    private var destinationLanguageCode = "es"
    private var destinationLanguageTitle = "Spanish"
    private var yourString = ""
    private var yourString1 = ""
    private lateinit var copy: ImageView
    private lateinit var share: ImageView
    private lateinit var copy1: ImageView
    private lateinit var share1: ImageView
    private lateinit var subscriptionManager: SubscriptionManager

    private lateinit var languageArrayList: ArrayList<ModelLanguage>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_translations)
        subscriptionManager = SubscriptionManager(this)

        sourceLanguage = findViewById(R.id.sourceLanguage)
        back = findViewById(R.id.back)
        destinationLanguageTv = findViewById(R.id.destinationLanguageTv)
        sourceLanguageChooseBtn = findViewById(R.id.sourceLanguageChooseBtn)
        destinationLanguageChooseBtn = findViewById(R.id.destinationLanguageChooseBtn)
        translateBtn = findViewById(R.id.translateBtn)

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please Wait")
        progressDialog.setCanceledOnTouchOutside(false)

        val adView: AdView = findViewById(R.id.adView)
        val isMonthlySubscriptionActive = subscriptionManager.isMonthlySubscriptionActive()
        val isYearlySubscriptionActive = subscriptionManager.isYearlySubscriptionActive()
        val isLifetimeSubscriptionActive = subscriptionManager.isLifetimeSubscriptionActive()

        if (isMonthlySubscriptionActive || isYearlySubscriptionActive || isLifetimeSubscriptionActive) {
            // User is subscribed, hide ads
            adView?.visibility= View.GONE
            //Toast.makeText(this, "Thank you for subscribing!", Toast.LENGTH_SHORT).show()
        } else {
            // User is not subscribed, show ads
            val adRequest = AdRequest.Builder().build()
            adView.loadAd(adRequest)
        }
        back.setOnClickListener {
            val isMonthlySubscriptionActive = subscriptionManager.isMonthlySubscriptionActive()
            val isYearlySubscriptionActive = subscriptionManager.isYearlySubscriptionActive()
            val isLifetimeSubscriptionActive = subscriptionManager.isLifetimeSubscriptionActive()

            if (isMonthlySubscriptionActive || isYearlySubscriptionActive || isLifetimeSubscriptionActive) {
                // User is subscribed, hide ads
                finish()
                //Toast.makeText(this, "Thank you for subscribing!", Toast.LENGTH_SHORT).show()
            } else {
                // User is not subscribed, show ads
                AlertDialog.Builder(this)
                    .setMessage("Loading...")
                    .setCancelable(false)
                    .create()
                    .show()
                val adRequest = AdRequest.Builder().build()

                InterstitialAd.load(this, "ca-app-pub-7055337155394452/3471919069", adRequest, object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(interstitialAd: InterstitialAd) {
                        Log.d(ContentValues.TAG, "Ad was loaded.")
                        mInterstitialAd = interstitialAd
                        mInterstitialAd?.fullScreenContentCallback=object :FullScreenContentCallback(){
                            override fun onAdDismissedFullScreenContent() {
                                super.onAdDismissedFullScreenContent()
                                // Dismiss the loading dialog when ad is dismissed
                                val dialog = (this as? AppCompatActivity)?.supportFragmentManager?.findFragmentByTag("loading_dialog")
                                if (dialog is AlertDialog) {
                                    dialog.dismiss()
                                }
                            }
                        }
                        // Show the ad
                        mInterstitialAd?.show(this@TranslationsActivity)

                        // Finish the activity after showing the ad
                        finish()
                    }

                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        val dialog = (this as? AppCompatActivity)?.supportFragmentManager?.findFragmentByTag("loading_dialog")
                        if (dialog is AlertDialog) {
                            dialog.dismiss()
                        }
                        Log.e(ContentValues.TAG, "Ad failed to load: $adError")

                        // If ad failed to load, simply finish the activity
                        finish()
                    }
                })
            }

        }

        loadAvailableLanguages()


        copy = findViewById(R.id.copy)
        share = findViewById(R.id.share)
        copy.setOnClickListener {
            yourString=sourceLanguage.text.toString()
            yourString?.let { it1 -> copyTextToClipboard(it1) }
        }
        share.setOnClickListener {
            yourString=sourceLanguage.text.toString()
            yourString?.let { it1 -> shareImageAndText(it1) }
        }

        copy1 = findViewById(R.id.copy1)
        share1 = findViewById(R.id.share1)
        copy1.setOnClickListener {

            yourString1=destinationLanguageTv.text.toString()
            yourString1?.let { it1 -> copyTextToClipboard(it1) }
        }
        share1.setOnClickListener {
            yourString1=destinationLanguageTv.text.toString()
            yourString1?.let { it1 -> shareImageAndText(it1) }
        }
        sourceLanguageChooseBtn.setOnClickListener {
            sourceLanguageCode()
        }

        destinationLanguageChooseBtn.setOnClickListener {
            destinationLanguageChoose()
        }

        translateBtn.setOnClickListener {
            validateData()
        }
    }
    private fun copyTextToClipboard(text: String) {
        val clipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("text", text)
        clipboardManager.setPrimaryClip(clipData)
        //Toast.makeText(this, "Text copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    private fun shareImageAndText(text: String) {
        // Create a directory to store text file
        val directory = File(filesDir, "images")
        if (!directory.exists()) {
            directory.mkdirs()
        }

        // Create a text file and save the text
        val textFile = File(directory, "text.txt")
        FileOutputStream(textFile).use { outputStream ->
            outputStream.write(text.toByteArray())
            outputStream.flush()
        }

        // Get the URI of the text file using FileProvider
        val textUri = FileProvider.getUriForFile(this, "com.example.image_to_text.fileprovider", textFile)

        // Create a share intent
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, textUri)
            type = "text/plain"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        // Start the activity to share text
        startActivity(Intent.createChooser(shareIntent, "Share via"))
    }
    private fun validateData() {
        val sourceLanguageText = sourceLanguage.text.toString().trim()
        if (sourceLanguageText.isEmpty()) {
            Toast.makeText(this, "Enter text to translate...", Toast.LENGTH_SHORT).show()
        } else {
            startTranslations(sourceLanguageText)
        }
    }

    private fun startTranslations(sourceLanguageText: String) {
        progressDialog.setMessage("Translating...")
        progressDialog.show()

        val translatorOptions = TranslatorOptions.Builder()
            .setSourceLanguage(sourceLanguageCode)
            .setTargetLanguage(destinationLanguageCode)
            .build()
        val translator = Translation.getClient(translatorOptions)
        val downloadConditions = DownloadConditions.Builder()
            .requireWifi()
            .build()

        // Using Kotlin coroutine to move translation to background thread
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val translationResult = translator.downloadModelIfNeeded(downloadConditions)
                    .await()
                val translatedText = translator.translate(sourceLanguageText).await()
                withContext(Dispatchers.Main) {
                    progressDialog.dismiss()
                    destinationLanguageTv.text = translatedText
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    progressDialog.dismiss()
                    Toast.makeText(this@TranslationsActivity, "Failed! ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun sourceLanguageCode() {
        val popupMenu = PopupMenu(this, sourceLanguageChooseBtn)
        for (i in languageArrayList.indices) {
            popupMenu.menu.add(Menu.NONE, i, i, languageArrayList[i].languageTitle)
        }
        popupMenu.show()
        popupMenu.setOnMenuItemClickListener { item ->
            val position = item.itemId
            sourceLanguageCode = languageArrayList[position].languageCode
            sourceLanguageTitle = languageArrayList[position].languageTitle
            sourceLanguageChooseBtn.text = sourceLanguageTitle
            sourceLanguage.hint = "Enter $sourceLanguageTitle"
            false
        }
    }

    private fun destinationLanguageChoose() {
        val popupMenu = PopupMenu(this, destinationLanguageChooseBtn)
        for (i in languageArrayList.indices) {
            popupMenu.menu.add(Menu.NONE, i, i, languageArrayList[i].languageTitle)
        }
        popupMenu.show()
        popupMenu.setOnMenuItemClickListener { item ->
            val position = item.itemId
            destinationLanguageCode = languageArrayList[position].languageCode
            destinationLanguageTitle = languageArrayList[position].languageTitle
            destinationLanguageChooseBtn.text = destinationLanguageTitle
            false
        }
    }

    private fun loadAvailableLanguages() {
        languageArrayList = ArrayList()
        val languageCodeList = TranslateLanguage.getAllLanguages()
        for (languageCode in languageCodeList) {
            val languageTitle = Locale(languageCode).displayLanguage
            val modelLanguage = ModelLanguage(languageCode, languageTitle)
            languageArrayList.add(modelLanguage)
        }
    }
}