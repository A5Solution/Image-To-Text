package com.example.image_to_text.ui.ViewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.mlkit.vision.text.Text.TextBlock

class SharedViewModel: ViewModel() {
    val textBlocks = MutableLiveData<List<TextBlock>>()

    fun setTextBlocks(payload: List<TextBlock>) {
        textBlocks.value = payload
    }
    fun clearData() {
        textBlocks.value = emptyList()
    }
    fun getSourceText(): String {
        // You can implement your logic to get the source text here
        // For example, you can concatenate all text blocks into one string
        val blocks = textBlocks.value ?: return ""
        val stringBuilder = StringBuilder()
        for (block in blocks) {
            stringBuilder.append(block.text).append("\n")
        }
        return stringBuilder.toString().trim()
    }
}