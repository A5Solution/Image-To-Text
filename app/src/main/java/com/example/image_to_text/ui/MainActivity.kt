package com.example.image_to_text.ui

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
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.text.FirebaseVisionText
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private val REQUEST_CAMERA = 1
    private val REQUEST_GALLERY = 2

    private var editText: EditText? = null
    private var imageViewCamera: ImageView? = null
    private var imageViewGallery: ImageView? = null
    private var imageViewPhoto: ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        setContentView(com.example.image_to_text.R.layout.activity_main)

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

                    // Convert the captured bitmap to FirebaseVisionImage
                    imageBitmap?.let { processImage(it) }
                }
                REQUEST_GALLERY -> {
                    val selectedImageUri: Uri? = data?.data
                    imageViewPhoto?.setImageURI(selectedImageUri)

                    // Convert the selected image URI to FirebaseVisionImage
                    selectedImageUri?.let {
                        try {
                            val inputStream = contentResolver.openInputStream(it)
                            val bitmap = BitmapFactory.decodeStream(inputStream)
                            processImage(bitmap)
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
    }

    private fun processImage(bitmap: Bitmap) {
        val image = FirebaseVisionImage.fromBitmap(bitmap)
        val recognizer = FirebaseVision.getInstance().onDeviceTextRecognizer

        recognizer.processImage(image)
            .addOnSuccessListener { firebaseVisionText ->
                // Task completed successfully
                processTextRecognitionResult(firebaseVisionText)
            }
            .addOnFailureListener { e ->
                // Task failed with an exception
                Log.e("TAG", "Text recognition failed: $e")
                Toast.makeText(this, "Text recognition failed", Toast.LENGTH_SHORT).show()
            }
    }

    private fun processTextRecognitionResult(texts: FirebaseVisionText) {
        val blocks = texts.textBlocks
        if (blocks.size == 0) {
            Toast.makeText(this, "No text found", Toast.LENGTH_SHORT).show()
            return
        }
        val stringBuilder = StringBuilder()
        for (block in blocks) {
            val lines = block.lines
            for (line in lines) {
                val elements = line.elements
                for (element in elements) {
                    stringBuilder.append(element.text).append(" ")
                }
                stringBuilder.append("\n")
            }
        }
        editText?.setText(stringBuilder.toString())
    }
}
