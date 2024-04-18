    package com.example.image_to_text.ui.activities

    import android.app.Dialog
    import android.content.ClipData
    import android.content.ClipboardManager
    import android.content.Intent
    import android.graphics.Bitmap
    import android.graphics.BitmapFactory
    import android.os.Bundle
    import android.os.Environment
    import android.speech.tts.TextToSpeech
    import android.view.Menu
    import android.view.View
    import android.widget.Button
    import android.widget.EditText
    import android.widget.ImageView
    import android.widget.PopupMenu
    import android.widget.Toast
    import androidx.appcompat.app.AppCompatActivity
    import androidx.core.content.FileProvider
    import androidx.core.graphics.drawable.toBitmap
    import com.example.image_to_text.R
    import com.example.image_to_text.databinding.ActivityTextExtractionBinding
    import com.example.image_to_text.ui.ModelLanguage
    import com.example.image_to_text.ui.ViewModel.SubscriptionManager.SubscriptionManager
    import com.example.image_to_text.ui.utils.Utils
    import com.google.mlkit.common.model.DownloadConditions
    import com.google.mlkit.nl.translate.TranslateLanguage
    import com.google.mlkit.nl.translate.Translation
    import com.google.mlkit.nl.translate.TranslatorOptions
    import kotlinx.coroutines.CoroutineScope
    import kotlinx.coroutines.Dispatchers
    import kotlinx.coroutines.launch
    import kotlinx.coroutines.tasks.await
    import kotlinx.coroutines.withContext
    import java.io.File
    import java.io.FileOutputStream
    import java.io.IOException
    import java.util.Locale

    class TextExtractionActivity : AppCompatActivity() {
        private lateinit var binding: ActivityTextExtractionBinding // Declare binding variable
        private lateinit var subscriptionManager: SubscriptionManager
        private lateinit var languageArrayList: ArrayList<ModelLanguage>
        private lateinit var destinationLanguageChooseBtn: Button
        private var destinationLanguageCode = "es"
        private var destinationLanguageTitle = "Spanish"
        private var sourceLanguageCode = "en"
        private lateinit var translateBtn: Button
        private lateinit var sourceLanguage: EditText
        private lateinit var copy: ImageView
        private lateinit var share: ImageView
        private var textToSpeech: TextToSpeech? = null
        private lateinit var progressDialog: Dialog
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            binding = ActivityTextExtractionBinding.inflate(layoutInflater)
            setContentView(binding.root)
            Utils.logAnalytic("TextExtraction")
            subscriptionManager = SubscriptionManager(this)
            sourceLanguage = binding.sourceLanguage

            // Check subscription status
            val isMonthlySubscriptionActive = subscriptionManager.isMonthlySubscriptionActive()
            val isYearlySubscriptionActive = subscriptionManager.isYearlySubscriptionActive()
            val isLifetimeSubscriptionActive = subscriptionManager.isLifetimeSubscriptionActive()

            if (isMonthlySubscriptionActive || isYearlySubscriptionActive || isLifetimeSubscriptionActive) {
                binding.nativeAdContainer.visibility= View.GONE
            } else {
                // User is not subscribed, show ads
                SplashActivity.admobNative.showNative(this, binding.nativeAdContainer, SplashActivity.admobNativeId)
            }
            loadAvailableLanguages()
            val imageFilePath = intent.getStringExtra("imageFilePath")
            if (imageFilePath != null) {
                val bitmap = BitmapFactory.decodeFile(imageFilePath)
                binding.imageViewPhoto.setImageBitmap(bitmap)
                val string = intent.getStringExtra("string")
                binding.sourceLanguage.setText(string)
            }

            binding.close.setOnClickListener(){
                Utils.logAnalytic("TextExtraction close button clicked")
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
            destinationLanguageChooseBtn = binding.destinationLanguageChooseBtn
            destinationLanguageChooseBtn.setOnClickListener {
                Utils.logAnalytic("TextExtraction destinationlanguage button clicked")
                destinationLanguageChoose()
            }
            translateBtn = binding.translateBtn
            translateBtn.setOnClickListener {
                Utils.logAnalytic("TextExtraction translate button clicked")
                validateData()
            }
            val yourString = sourceLanguage.text.toString()
            copy = binding.copy
            share = binding.share
            copy.setOnClickListener {
                Utils.logAnalytic("TextExtraction copy button clicked")
                yourString?.let { it1 -> copyTextToClipboard(it1) }
            }
            share.setOnClickListener {
                Utils.logAnalytic("TextExtraction share button clicked")
                yourString?.let { it1 -> shareImageAndText(it1) }
            }
            textToSpeech = TextToSpeech(
                applicationContext
            ) { status ->
                if (status != TextToSpeech.ERROR) {
                    // Set language to Arabic
                    textToSpeech?.language = Locale(sourceLanguageCode)
                } else {
                    Toast.makeText(
                        this@TextExtractionActivity,
                        "Text-to-Speech initialization failed",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            val speaker: ImageView = findViewById(R.id.speaker)
            var isSpeaking = false
            var spokenText: String? = null

            speaker.setOnClickListener {
                // Toggle playback state
                isSpeaking = !isSpeaking

                // Check if text is empty
                if (sourceLanguage.text.isNullOrEmpty()) {
                    Toast.makeText(this, "Scan Image First!", Toast.LENGTH_SHORT).show()
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
            val countryFlags= TranslateLanguage.getAllLanguages()
            for (languageCode in languageCodeList) {
                val languageTitle = Locale(languageCode).displayLanguage
                val modelLanguage = ModelLanguage(languageCode, languageTitle)
                languageArrayList.add(modelLanguage)
            }
        }
        public fun validateData() {
            val sourceLanguageText = sourceLanguage.text.toString().trim()
            if (sourceLanguageText.isEmpty()) {
                Toast.makeText(this, "Enter text to translate...", Toast.LENGTH_SHORT).show()
            } else {
                startTranslations(sourceLanguageText)
            }
        }

        private fun startTranslations(sourceLanguageText: String) {
            progressDialog = Dialog(this)
            progressDialog.setCanceledOnTouchOutside(false)
            //progressDialog.setTitle("Translating...")
            progressDialog.setContentView(R.layout.custom_dialog)
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
                    val imageViewPhoto = findViewById<ImageView>(R.id.imageViewPhoto)
                    val bitmap = imageViewPhoto.drawable.toBitmap()

                    val file = saveBitmapToFile(bitmap)
                    val filePath = file.absolutePath

                    withContext(Dispatchers.Main) {
                        progressDialog.dismiss()
                        navigateToViewTranslationActivity(filePath, translatedText.toString())
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        progressDialog.dismiss()
                        Toast.makeText(this@TextExtractionActivity, "Failed! ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        private fun navigateToViewTranslationActivity(filePath: String, translatedText: String) {
            val intent = Intent(applicationContext, ViewTranslationActivity::class.java)
            intent.putExtra("imageFilePath", filePath)
            intent.putExtra("yourString", translatedText)
            intent.putExtra("code", destinationLanguageCode)
            startActivity(intent)
        }
        private fun saveBitmapToFile(bitmap: Bitmap): File {
            // Get the directory for storing images
            val directory = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "MyApp")
            // Create the directory if it doesn't exist
            if (!directory.exists()) {
                directory.mkdirs()
            }
            // Create a file to save the image
            val file = File(directory, "image_${System.currentTimeMillis()}.png")
            try {
                // Save the Bitmap to the file
                val outputStream = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                outputStream.flush()
                outputStream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return file
        }
        override fun onDestroy() {
            super.onDestroy()
            // Shutdown Text-to-Speech engine when activity is destroyed to release resources

            textToSpeech?.stop()
            textToSpeech?.shutdown()
        }
        private fun copyTextToClipboard(text: String) {
            val clipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText("text", text)
            clipboardManager.setPrimaryClip(clipData)
            Toast.makeText(this, "Text copied!", Toast.LENGTH_SHORT).show()
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
        override fun onPause() {
            super.onPause()
            textToSpeech?.stop()
        }
        private fun speakText(text: String) {
            // Speak the text
            textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        }
        private fun stopSpeaking() {
            textToSpeech?.stop()
        }

        override fun onBackPressed() {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
            super.onBackPressed()
        }
    }