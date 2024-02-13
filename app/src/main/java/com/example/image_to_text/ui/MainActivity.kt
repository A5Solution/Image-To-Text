package com.example.image_to_text.ui

import android.R
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.nl.languageid.LanguageIdentifier
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions


class MainActivity : AppCompatActivity() {
    private val REQUEST_CAMERA = 1
    private val REQUEST_GALLERY = 2

    private var editText: EditText? = null
    private var imageViewCamera: ImageView? = null
    private var imageViewGallery: ImageView? = null
    private var imageViewPhoto: ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.example.image_to_text.R.layout.activity_main)
        FirebaseApp.initializeApp(this)
        editText = findViewById<EditText>(com.example.image_to_text.R.id.editText)
        imageViewCamera = findViewById<ImageView>(com.example.image_to_text.R.id.imageViewCamera)
        imageViewGallery = findViewById<ImageView>(com.example.image_to_text.R.id.imageViewGallery)
        imageViewPhoto = findViewById<ImageView>(com.example.image_to_text.R.id.imageViewPhoto)

        imageViewCamera?.setOnClickListener(View.OnClickListener { openCamera() })

        imageViewGallery?.setOnClickListener(View.OnClickListener { openGallery() })
    }
    private fun openCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (cameraIntent.resolveActivity(packageManager) != null) {
            startActivityForResult(cameraIntent, REQUEST_CAMERA)
        }
    }
    private fun openGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(galleryIntent, REQUEST_GALLERY)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_CAMERA -> {
                    val extras: Bundle? = data?.extras
                    val imageBitmap: Bitmap? = extras?.get("data") as? Bitmap
                    imageViewPhoto?.setImageBitmap(imageBitmap)

                    // Convert the captured bitmap to InputImage
                    imageBitmap?.let { processImage(it) }
                }
                REQUEST_GALLERY -> {
                    val selectedImageUri: Uri? = data?.data
                    imageViewPhoto?.setImageURI(selectedImageUri)

                    // Convert the selected image URI to InputImage
                    selectedImageUri?.let {
                        val inputStream = contentResolver.openInputStream(it)
                        val bitmap = BitmapFactory.decodeStream(inputStream)
                        processImage(bitmap)
                    }
                }
            }
        }
    }
    private fun processImage(bitmap: Bitmap) {
        val image = InputImage.fromBitmap(bitmap, 0)
        val options = TextRecognizerOptions.Builder()
            // Add any desired options here
            .build()

        val recognizer = TextRecognition.getClient(options)

        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                val resultText = visionText.text
                editText?.setText(resultText)

                // Identify the language of the recognized text
                identifyLanguage(resultText)
            }
            .addOnFailureListener { e ->
                Log.e("TAG", "Text recognition failed: $e")
                Toast.makeText(this, "Text recognition failed", Toast.LENGTH_SHORT).show()
            }
    }

    private fun identifyLanguage(text: String) {
        val languageIdentifier = LanguageIdentification.getClient()

        languageIdentifier.identifyLanguage(text)
            .addOnSuccessListener { languageCode ->
                // Process the recognized language
                processRecognizedLanguage(languageCode, text)
            }
            .addOnFailureListener { e ->
                Log.e("TAG", "Language identification failed: $e")
                Toast.makeText(this, "Language identification failed", Toast.LENGTH_SHORT).show()
            }
    }


    private fun processRecognizedLanguage(languageCode: String, text: String) {
        // Now you have the language code (e.g., "en" for English)
        // You can implement logic here to handle different languages
        // For example, you could have different text processing logic for different languages
        Log.d("TAG", "Detected language: $languageCode")

        // Set the recognized text into the EditText
        editText?.setText(text)
    }






}