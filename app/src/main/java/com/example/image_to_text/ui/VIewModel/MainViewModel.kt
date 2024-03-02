package com.example.image_to_text.ui.VIewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MainViewModel : ViewModel() {

    fun translateText(
        sourceLanguageCode: String,
        destinationLanguageCode: String,
        sourceLanguageText: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val translatorOptions = TranslatorOptions.Builder()
                    .setSourceLanguage(sourceLanguageCode)
                    .setTargetLanguage(destinationLanguageCode)
                    .build()
                val translator = Translation.getClient(translatorOptions)
                val translatedText = translator.translate(sourceLanguageText).await()
                onSuccess(translatedText)
            } catch (e: Exception) {
                onError("Failed! ${e.message}")
            }
        }
    }
}
