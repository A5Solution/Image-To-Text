package com.example.image_to_text.ui

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.example.image_to_text.R
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.*
import java.util.Locale

class TranslationActivity : AppCompatActivity() {
    private lateinit var sourceLanguageSpinner: Spinner
    private lateinit var targetLanguageSpinner: Spinner
    private lateinit var sourceEditText: EditText
    private lateinit var targetEditText: EditText
    private lateinit var translator: Translator
    private lateinit var translationScope: CoroutineScope

    data class ModelLanguage(
        var languageCode: String,
        var languageTitle: String
    )

    var languageList = ArrayList<ModelLanguage>() // Store language titles
    var languageListTitle = ArrayList<String>() // Store language titles

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_translation)
        sourceLanguageSpinner = findViewById(R.id.sourceLanguageSpinner)
        targetLanguageSpinner = findViewById(R.id.targetLanguageSpinner)
        sourceEditText = findViewById(R.id.sourceEditText)
        targetEditText = findViewById(R.id.targetEditText)

        translationScope = CoroutineScope(Dispatchers.IO)

        loadAvailableLanguages()

        // Set up language dropdowns
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, languageListTitle)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        sourceLanguageSpinner.adapter = adapter
        targetLanguageSpinner.adapter = adapter

        // Set up translation functionality
        sourceEditText.addTextChangedListener {
            translateText(it.toString())
        }

        sourceLanguageSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                initializeTranslator(position)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        targetLanguageSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // No need to do anything here
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun loadAvailableLanguages() {
        languageList = ArrayList()

        val languageCodeList = TranslateLanguage.getAllLanguages()

        for (languageCode in languageCodeList) {
            val languageTitle = Locale(languageCode).displayLanguage // e.g., en -> English

            Log.d(TAG, "LoadAvailableLanguages: LanguageCode: $languageCode")
            Log.d(TAG, "LoadAvailableLanguages: languageTitle: $languageTitle")

            val modelLanguage = ModelLanguage(languageCode, languageTitle)
            languageListTitle.add(modelLanguage.languageTitle)
            languageList.add(modelLanguage)
        }
    }

    private fun initializeTranslator(position: Int) {
        val sourceLanguage = languageList[position]
        val targetLanguage = languageList[targetLanguageSpinner.selectedItemPosition]

        val options = TranslatorOptions.Builder()
            .setSourceLanguage(sourceLanguage.languageCode)
            .setTargetLanguage(targetLanguage.languageCode)
            .build()

        // translator = TranslatorOptions.getDefaultTranslator(options)
    }

    private fun translateText(text: String) {
//        translationScope.launch {
//            try {
//                val translationResult = translator.translate(text).await()
//                withContext(Dispatchers.Main) {
//                    targetEditText.setText(translationResult)
//                }
//            } catch (e: Exception) {
//                e.printStackTrace()
//                // Handle translation error
//            }
//        }
    }

    override fun onDestroy() {
        super.onDestroy()
        translationScope.cancel()
    }
}
