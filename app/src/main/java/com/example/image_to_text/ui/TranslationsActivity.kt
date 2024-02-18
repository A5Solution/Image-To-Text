package com.example.image_to_text.ui


import android.app.ProgressDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.example.image_to_text.R
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

    private var sourceLanguageCode = "en"
    private var sourceLanguageTitle = "English"
    private var destinationLanguageCode = "ur"
    private var destinationLanguageTitle = "Urdu"
    private lateinit var copy: ImageView
    private lateinit var share: ImageView
    private lateinit var copy1: ImageView
    private lateinit var share1: ImageView
    private lateinit var languageArrayList: ArrayList<ModelLanguage>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_translations)

        sourceLanguage = findViewById(R.id.sourceLanguage)
        back = findViewById(R.id.back)
        destinationLanguageTv = findViewById(R.id.destinationLanguageTv)
        sourceLanguageChooseBtn = findViewById(R.id.sourceLanguageChooseBtn)
        destinationLanguageChooseBtn = findViewById(R.id.destinationLanguageChooseBtn)
        translateBtn = findViewById(R.id.translateBtn)

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please Wait")
        progressDialog.setCanceledOnTouchOutside(false)

        back.setOnClickListener(){
            finish()
        }
        loadAvailableLanguages()
        val yourString=sourceLanguage.text.toString()
        val yourString1=destinationLanguageTv.text.toString()
        copy = findViewById(R.id.copy)
        share = findViewById(R.id.share)
        copy.setOnClickListener {
            yourString?.let { it1 -> copyTextToClipboard(it1) }
        }
        share.setOnClickListener {
            yourString?.let { it1 -> shareImageAndText(it1) }
        }

        copy1 = findViewById(R.id.copy1)
        share1 = findViewById(R.id.share1)
        copy.setOnClickListener {
            yourString1?.let { it1 -> copyTextToClipboard(it1) }
        }
        share.setOnClickListener {
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
        Toast.makeText(this, "Text copied to clipboard", Toast.LENGTH_SHORT).show()
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