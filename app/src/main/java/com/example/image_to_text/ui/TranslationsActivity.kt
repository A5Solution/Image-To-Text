package com.example.image_to_text.ui


import android.Manifest
import android.app.ProgressDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.text.Editable
import android.text.TextWatcher
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.image_to_text.R
import com.example.image_to_text.ui.SubscriptionManager.SubscriptionManager
import com.example.image_to_text.ui.splashscreen.SplashActivity
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
import com.google.mlkit.nl.languageid.LanguageIdentification
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
import kotlin.math.max

class TranslationsActivity : AppCompatActivity() {
    private lateinit var sourceLanguage: EditText
    private lateinit var destinationLanguageTv: TextView
    private lateinit var sourceLanguageChooseBtn: Button
    private lateinit var destinationLanguageChooseBtn: Button
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
    private lateinit var navContainer: FrameLayout
    private lateinit var subscriptionManager: SubscriptionManager
    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var micImageView: ImageView
    private lateinit var recognizedTextView: TextView
    private lateinit var languageArrayList: ArrayList<ModelLanguage>
    private var textToSpeech: TextToSpeech? = null

    companion object {
        private const val RECORD_AUDIO_PERMISSION_REQUEST_CODE = 100
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_translations)
        navContainer = findViewById(R.id.nativeAdContainer)
        subscriptionManager = SubscriptionManager(this)
        val isMonthlySubscriptionActive = subscriptionManager.isMonthlySubscriptionActive()
        val isYearlySubscriptionActive = subscriptionManager.isYearlySubscriptionActive()
        val isLifetimeSubscriptionActive = subscriptionManager.isLifetimeSubscriptionActive()

        if (isMonthlySubscriptionActive || isYearlySubscriptionActive || isLifetimeSubscriptionActive) {
            // User is subscribed, hide ads
            navContainer.visibility=View.GONE
            //Toast.makeText(this, "Thank you for subscribing!", Toast.LENGTH_SHORT).show()
        } else {
            SplashActivity.admobNative.showNative(this,navContainer , SplashActivity.admobNativeId)
        }



        micImageView = findViewById(R.id.mic)
        sourceLanguage = findViewById(R.id.sourceLanguage)
        back = findViewById(R.id.back)
        destinationLanguageTv = findViewById(R.id.destinationLanguageTv)
        sourceLanguageChooseBtn = findViewById(R.id.sourceLanguageChooseBtn)
        destinationLanguageChooseBtn = findViewById(R.id.destinationLanguageChooseBtn)
//        translateBtn = findViewById(R.id.translateBtn)
        recognizedTextView = findViewById(R.id.recognizedTextView)
        recognizedTextView.movementMethod = ScrollingMovementMethod() // Enable scrolling

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please Wait")
        progressDialog.setCanceledOnTouchOutside(false)
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        if (!isSpeechRecognitionPermissionGranted()) {
            requestSpeechRecognitionPermission()
        }


        back.setOnClickListener {
            /*val isMonthlySubscriptionActive = subscriptionManager.isMonthlySubscriptionActive()
            val isYearlySubscriptionActive = subscriptionManager.isYearlySubscriptionActive()
            val isLifetimeSubscriptionActive = subscriptionManager.isLifetimeSubscriptionActive()

            if (isMonthlySubscriptionActive || isYearlySubscriptionActive || isLifetimeSubscriptionActive) {
                // User is subscribed, hide ads
                finish()
                //Toast.makeText(this, "Thank you for subscribing!", Toast.LENGTH_SHORT).show()
            } else {
                SplashActivity.admobInter.showInterAd(this) {
                    SplashActivity.admobInter.loadInterAd(
                        this,
                        getString(R.string.inter_ad_unit_id)
                    )
                    finish()
                }
            }
*/
            finish()
        }
        loadAvailableLanguages()


        copy = findViewById(R.id.copy)
        share = findViewById(R.id.share)
        copy.setOnClickListener {
            yourString = sourceLanguage.text.toString()
            yourString?.let { it1 -> copyTextToClipboard(it1) }
        }
        share.setOnClickListener {
            yourString = sourceLanguage.text.toString()
            yourString?.let { it1 -> shareImageAndText(it1) }
        }

        copy1 = findViewById(R.id.copy1)
        share1 = findViewById(R.id.share1)
        copy1.setOnClickListener {

            yourString1 = destinationLanguageTv.text.toString()
            yourString1?.let { it1 -> copyTextToClipboard(it1) }
        }
        share1.setOnClickListener {
            yourString1 = destinationLanguageTv.text.toString()
            yourString1?.let { it1 -> shareImageAndText(it1) }
        }
        sourceLanguageChooseBtn.setOnClickListener {
            sourceLanguageCode()
        }

        destinationLanguageChooseBtn.setOnClickListener {
            destinationLanguageChoose()
        }

        sourceLanguage.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // This method is called to notify you that, within `s`, the `count` characters beginning at `start` are about to be replaced by new text with length `after`.
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // This method is called to notify you that, somewhere within `s`, the text has been changed.
            }

            override fun afterTextChanged(s: Editable?) {
                // This method is called to notify you that, somewhere within `s`, the text has been changed.
                destinationLanguageTv.setText("Translating...")
                validateData()
            }
        })

        val eraseImageView = findViewById<ImageView>(R.id.erase)
        eraseImageView.setOnClickListener(){
            sourceLanguage.text.clear()
        }

        micImageView.setOnLongClickListener {
            startSpeechRecognition()
            true // Return true to consume the long click event
        }

        micImageView.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                speechRecognizer.stopListening()
            }
            false
        }

        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}

            override fun onBeginningOfSpeech() {}

            override fun onRmsChanged(rmsdB: Float) {}

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {}

            override fun onError(error: Int) {
                Log.e("SpeechRecognition", "Error $error")
                Toast.makeText(applicationContext, "Error occurred during speech recognition", Toast.LENGTH_SHORT).show()
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    val recognizedText = matches[0]
                    // Set recognized text to the sourceLanguage EditText
                    sourceLanguage.setText(recognizedText)
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    val recognizedText = matches[0]
                    // Append the recognized text to the text view

                    recognizedTextView.append(recognizedText + " ")

                    // Scroll to the end to show the latest text
                    val scrollAmount = recognizedTextView.layout.getLineTop(recognizedTextView.lineCount) - recognizedTextView.height
                    recognizedTextView.scrollTo(0, max(0, scrollAmount))
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
        var isSpeaking = false
        var spokenText: String? = null

        textToSpeech = TextToSpeech(
            applicationContext
        ) { status ->
            if (status != TextToSpeech.ERROR) {
                // Set language to Arabic
                textToSpeech?.language = Locale("ar")
            } else {
                Toast.makeText(
                    this@TranslationsActivity,
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
            if (destinationLanguageTv.text.isNullOrEmpty()) {
                Toast.makeText(this, "Translate first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // If speaking, pause; otherwise, speak the text
            if (isSpeaking) {
                spokenText = destinationLanguageTv.text.toString()
                speakText(spokenText!!)
            } else {
                stopSpeaking()
            }
        }
    }
    // Function to speak the text
    private fun speakText(text: String) {
        // Speak the text
        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    // Function to stop speaking
    private fun stopSpeaking() {
        textToSpeech?.stop()
    }
    private fun identifyLanguage(text: String) {
        val languageIdentifier = LanguageIdentification.getClient()

        languageIdentifier.identifyLanguage(text)
            .addOnSuccessListener { languageCode ->
                // Language identified successfully, translate and set the text
                translateAndSetText(text, languageCode)
            }
            .addOnFailureListener { e ->
                // Language identification failed, handle the error
                Log.e("LanguageIdentification", "Language identification failed: ${e.message}")
                Toast.makeText(applicationContext, "Language identification failed", Toast.LENGTH_SHORT).show()
            }
    }

    private fun translateAndSetText(text: String, languageCode: String) {
        // Translate the text to the identified language
        val translatorOptions = TranslatorOptions.Builder()
            .setSourceLanguage(languageCode)
            .setTargetLanguage(destinationLanguageCode)
            .build()
        val translator = Translation.getClient(translatorOptions)

        translator.translate(text)
            .addOnSuccessListener { translatedText ->
                // Set the translated text to the destinationLanguageTv TextView
                destinationLanguageTv.text = translatedText
            }
            .addOnFailureListener { e ->
                // Translation failed, handle the error
                Log.e("Translation", "Translation failed: ${e.message}")
                Toast.makeText(applicationContext, "Translation failed", Toast.LENGTH_SHORT).show()
            }
    }
    private fun copyTextToClipboard(text: String) {
        val clipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("text", text)
        clipboardManager.setPrimaryClip(clipData)
        Toast.makeText(this, "Text copied!", Toast.LENGTH_SHORT).show()
    }
    private fun startSpeechRecognition() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        speechRecognizer.startListening(intent)
    }
    private fun isSpeechRecognitionPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestSpeechRecognitionPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.RECORD_AUDIO),
            RECORD_AUDIO_PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == RECORD_AUDIO_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, start speech recognition
                startSpeechRecognition()
            } else {
                // Permission denied, handle accordingly (e.g., show a message)
                Toast.makeText(this, "Speech recognition permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun shareImageAndText(text: String) {
        // Create a directory to store text file
        // Create a share intent
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text) // Set the text content
            type = "text/plain" // Set the MIME type
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
        //progressDialog.show()

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
                    //progressDialog.dismiss()

                    destinationLanguageTv.text = translatedText
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    //progressDialog.dismiss()
                    Toast.makeText(
                        this@TranslationsActivity,
                        "Failed! ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
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
            validateData()
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
    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    override fun onResume() {
        super.onResume()
        ApplicationClass.counter++
    }

}