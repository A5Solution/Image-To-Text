package com.example.image_to_text.ui

import android.app.Activity
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.image_to_text.R
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.material.button.MaterialButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.FirebaseApp
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.text.FirebaseVisionText
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private val REQUEST_CAMERA = 1
    private val REQUEST_GALLERY = 2

    private var imageViewCamera: MaterialButton? = null
    private var imageViewGallery: MaterialButton? = null
    private var imageViewPhoto: ImageView? = null
    private var menu: ImageView? = null
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var languageArrayList: ArrayList<ModelLanguage>
    private lateinit var destinationLanguageChooseBtn: MaterialButton
    private var destinationLanguageCode = "ur"
    private var destinationLanguageTitle = "Urdu"

    private var sourceLanguageCode = "en"
    private var sourceLanguageTitle = "English"
    private lateinit var translateBtn: MaterialButton
    private lateinit var sourceLanguage: EditText
    private lateinit var progressDialog: Dialog




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        setContentView(com.example.image_to_text.R.layout.activity_main)
        loadAvailableLanguages()
        destinationLanguageChooseBtn = findViewById<MaterialButton>(R.id.destinationLanguageChooseBtn)
        destinationLanguageChooseBtn.setOnClickListener {
            destinationLanguageChoose()
        }
        translateBtn = findViewById(R.id.translateBtn)
        translateBtn.setOnClickListener {
            validateData()
        }


        sourceLanguage = findViewById(R.id.sourceLanguage)

        menu = findViewById<ImageView>(R.id.menu)
        drawerLayout = findViewById<DrawerLayout>(R.id.drawerLayout)

        menu?.setOnClickListener {
            // Open the drawer when the menu ImageView is clicked
            drawerLayout.openDrawer(GravityCompat.START)
        }

        // Add ActionBarDrawerToggle to handle opening and closing the drawer
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        val adView: AdView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)


        imageViewCamera = findViewById<MaterialButton>(R.id.imageViewCamera)
        imageViewGallery = findViewById<MaterialButton>(R.id.imageViewGallery)
        imageViewPhoto = findViewById<ImageView>(R.id.imageViewPhoto)

        imageViewCamera?.setOnClickListener { openCamera() }
        imageViewGallery?.setOnClickListener { openGallery() }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            // Handle other action bar items here if needed
            else -> super.onOptionsItemSelected(item)
        }
    }
    private fun openCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (cameraIntent.resolveActivity(packageManager) != null) {
            startActivityForResult(cameraIntent, REQUEST_CAMERA)
        } else {
            Toast.makeText(this, "Camera not available", Toast.LENGTH_SHORT).show()
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
        if (blocks.isEmpty()) {
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
        sourceLanguage?.setText(stringBuilder.toString())
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

    private fun validateData() {
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
                withContext(Dispatchers.Main) {
                    progressDialog.dismiss()
                    Toast.makeText(this@MainActivity, ""+translatedText, Toast.LENGTH_SHORT).show()

                    // translatedText
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    progressDialog.dismiss()
                    Toast.makeText(this@MainActivity, "Failed! ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
