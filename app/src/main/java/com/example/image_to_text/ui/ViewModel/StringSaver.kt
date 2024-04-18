package com.example.image_to_text.ui.ViewModel

class StringSaver(private var savedString: String? = null) {

    // Function to save a string
    fun saveString(string: String) {
        savedString = string
    }

    // Function to retrieve the saved string
    fun getSavedString(): String? {
        return savedString
    }
}
