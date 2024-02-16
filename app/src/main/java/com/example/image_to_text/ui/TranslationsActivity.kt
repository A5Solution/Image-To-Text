package com.example.image_to_text.ui


import android.app.ProgressDialog
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
import java.util.*
import kotlin.collections.ArrayList
class TranslationsActivity : AppCompatActivity() {
    private lateinit var sourceLanguage: EditText
    private lateinit var destinationLanguageTv: TextView
    private lateinit var sourceLanguageChooseBtn: MaterialButton
    private lateinit var destinationLanguageChooseBtn: MaterialButton
    private lateinit var translateBtn: MaterialButton
    private lateinit var translator: Translator
    private lateinit var translatorOptions: TranslatorOptions
    private lateinit var progressDialog: ProgressDialog

    private val TAG = "MAIN_TAG"

    private var sourceLanguageCode = "en"
    private var sourceLanguageTitle = "English"
    private var destinationLanguageCode = "ur"
    private var destinationLanguageTitle = "Urdu"

    private lateinit var languageArrayList: ArrayList<ModelLanguage>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_translations)

        sourceLanguage = findViewById(R.id.sourceLanguage)
        destinationLanguageTv = findViewById(R.id.destinationLanguageTv)
        sourceLanguageChooseBtn = findViewById(R.id.sourceLanguageChooseBtn)
        destinationLanguageChooseBtn = findViewById(R.id.destinationLanguageChooseBtn)
        translateBtn = findViewById(R.id.translateBtn)

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please Wait")
        progressDialog.setCanceledOnTouchOutside(false)

        loadAvailableLanguages()

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