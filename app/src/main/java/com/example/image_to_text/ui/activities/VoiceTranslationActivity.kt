package com.example.image_to_text.ui.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.content.pm.PackageManager
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
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.image_to_text.R
import com.example.image_to_text.databinding.ActivityVoiceTranslationBinding
import com.example.image_to_text.ui.ApplicationClass
import com.example.image_to_text.ui.ModelLanguage
import com.example.image_to_text.ui.ViewModel.SubscriptionManager.SubscriptionManager
import com.example.image_to_text.ui.utils.Utils
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.interstitial.InterstitialAd
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
import java.util.Locale
import kotlin.math.max

class VoiceTranslationActivity : AppCompatActivity() {
    private val RECOGNIZER_RESULT: Int = 1
    private lateinit var binding: ActivityVoiceTranslationBinding
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
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utils.logAnalytic("VoiceTranslation activity")
        binding = ActivityVoiceTranslationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        subscriptionManager = SubscriptionManager(this)
        val isMonthlySubscriptionActive = subscriptionManager.isMonthlySubscriptionActive()
        val isYearlySubscriptionActive = subscriptionManager.isYearlySubscriptionActive()
        val isLifetimeSubscriptionActive = subscriptionManager.isLifetimeSubscriptionActive()

        if (isMonthlySubscriptionActive || isYearlySubscriptionActive || isLifetimeSubscriptionActive) {
            // User is subscribed, hide ads
            binding.adView?.visibility= View.GONE
            //Toast.makeText(this, "Thank you for subscribing!", Toast.LENGTH_SHORT).show()
        } else {
            val adRequest = AdRequest.Builder().build()
            binding.adView.loadAd(adRequest)
        }
       binding.recognizedTextView.movementMethod = ScrollingMovementMethod() // Enable scrolling

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please Wait")
        progressDialog.setCanceledOnTouchOutside(false)
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        if (!isSpeechRecognitionPermissionGranted()) {
            requestSpeechRecognitionPermission()
        }
        binding.back.setOnClickListener {
            Utils.logAnalytic("VoiceTranslation close button clicked")
            finish()
        }
        loadAvailableLanguages()
        binding.copy.setOnClickListener {
            Utils.logAnalytic("VoiceTranslation copy first button clicked")
            yourString = binding.sourceLanguage.text.toString()
            yourString?.let { it1 -> copyTextToClipboard(it1) }
        }
        binding.share.setOnClickListener {
            Utils.logAnalytic("VoiceTranslation share first button clicked")
            yourString = binding.sourceLanguage.text.toString()
            yourString?.let { it1 -> shareImageAndText(it1) }
        }
        binding.copy1.setOnClickListener {
            Utils.logAnalytic("VoiceTranslation copy 2nd button clicked")
            yourString1 = binding.destinationLanguageTv.text.toString()
            yourString1?.let { it1 -> copyTextToClipboard(it1) }
        }
        binding.share1.setOnClickListener {
            Utils.logAnalytic("VoiceTranslation share 2nd button clicked")
            yourString1 = binding.destinationLanguageTv.text.toString()
            yourString1?.let { it1 -> shareImageAndText(it1) }
        }
        binding.sourceLanguageChooseBtn.setOnClickListener {
            Utils.logAnalytic("VoiceTranslation sourcelanguage button clicked")
            sourceLanguageCode()
        }

        binding.destinationLanguageChooseBtn.setOnClickListener {
            Utils.logAnalytic("VoiceTranslation destination button clicked")
            destinationLanguageChoose()
        }

        binding.sourceLanguage.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // This method is called to notify you that, within `s`, the `count` characters beginning at `start` are about to be replaced by new text with length `after`.
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // This method is called to notify you that, somewhere within `s`, the text has been changed.
            }

            override fun afterTextChanged(s: Editable?) {
                // This method is called to notify you that, somewhere within `s`, the text has been changed.
                binding.destinationLanguageTv.setText("Translating...")
                validateData()
            }
        })

        val eraseImageView = findViewById<ImageView>(R.id.erase)
        eraseImageView.setOnClickListener(){
            Utils.logAnalytic("VoiceTranslation erase text button clicked")
            binding.sourceLanguage.text.clear()
        }

        /*binding.mic.setOnLongClickListener {
            startSpeechRecognition()
            true // Return true to consume the long click event
        }*/

        binding.mic.setOnClickListener(){
            Utils.logAnalytic("VoiceTranslation mic button clicked")
            startSpeechRecognition()
        }

        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}

            override fun onError(error: Int) {
                Log.e("SpeechRecognition", "Error $error")
                Toast.makeText(applicationContext, "Error occurred during speech recognition", Toast.LENGTH_SHORT)
                    .show()
            }
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    val recognizedText = matches[0]
                    binding.sourceLanguage.setText(recognizedText)
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    val recognizedText = matches[0]
                    binding.sourceLanguage.append("$recognizedText ")
                    val scrollAmount =
                        binding.sourceLanguage.layout.getLineTop(binding.sourceLanguage.lineCount) - binding.sourceLanguage.height
                    binding.sourceLanguage.scrollTo(0, max(0, scrollAmount))
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
                textToSpeech?.language = Locale(destinationLanguageCode)
            } else {
                Toast.makeText(
                    this@VoiceTranslationActivity,
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
            if (binding.destinationLanguageTv.text.isNullOrEmpty()) {
                Toast.makeText(this, "Translate first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // If speaking, pause; otherwise, speak the text
            if (isSpeaking) {
                spokenText = binding.destinationLanguageTv.text.toString()
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RECOGNIZER_RESULT && resultCode == RESULT_OK) {
            val matches = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            // Check if matches list is not null and has at least one item
            if (matches != null && matches.isNotEmpty()) {
                // Set the first result from matches to the sourceLanguage TextView
                binding.sourceLanguage.setText(matches[0].toString())
            }
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
                binding.destinationLanguageTv.text = translatedText
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
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,"Speech to text")
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, sourceLanguageCode)
        startActivityForResult(intent,RECOGNIZER_RESULT)
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
            TranslationsActivity.RECORD_AUDIO_PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == TranslationsActivity.RECORD_AUDIO_PERMISSION_REQUEST_CODE) {
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
        val sourceLanguageText = binding.sourceLanguage.text.toString().trim()
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

                    binding.destinationLanguageTv.text = translatedText
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    //progressDialog.dismiss()
                    Toast.makeText(
                        this@VoiceTranslationActivity,
                        "Failed! ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun sourceLanguageCode() {
        val popupMenu = PopupMenu(this, binding.sourceLanguageChooseBtn)
        for (i in languageArrayList.indices) {
            popupMenu.menu.add(Menu.NONE, i, i, languageArrayList[i].languageTitle)
        }
        popupMenu.show()
        popupMenu.setOnMenuItemClickListener { item ->
            val position = item.itemId
            sourceLanguageCode = languageArrayList[position].languageCode
            sourceLanguageTitle = languageArrayList[position].languageTitle
            binding.sourceLanguageChooseBtn.text = sourceLanguageTitle
            binding.sourceLanguage.hint = "Enter $sourceLanguageTitle"
            false
        }
    }

    private fun destinationLanguageChoose() {
        val popupMenu = PopupMenu(this, binding.destinationLanguageChooseBtn)
        for (i in languageArrayList.indices) {
            popupMenu.menu.add(Menu.NONE, i, i, languageArrayList[i].languageTitle)
        }
        popupMenu.show()
        popupMenu.setOnMenuItemClickListener { item ->
            val position = item.itemId
            destinationLanguageCode = languageArrayList[position].languageCode
            destinationLanguageTitle = languageArrayList[position].languageTitle
            binding.destinationLanguageChooseBtn.text = destinationLanguageTitle
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
