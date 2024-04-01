package com.example.image_to_text.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.speech.tts.TextToSpeech
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.image_to_text.R
import com.example.image_to_text.databinding.ActivityMainBinding
import com.example.image_to_text.ui.ApplicationClass.Companion.counter
import com.example.image_to_text.ui.SubscriptionManager.SubscriptionManager
import com.example.image_to_text.ui.Utils.Companion.logAnalytic
import com.example.image_to_text.ui.activities.CameraActivity
import com.example.image_to_text.ui.activities.MenuActivity
import com.example.image_to_text.ui.ads.AdmobInter
import com.example.image_to_text.ui.ads.AdmobInter.Companion.isClicked
import com.example.image_to_text.ui.history.HistoryActivity
import com.example.image_to_text.ui.inapp.InAppActivity
import com.example.image_to_text.ui.splashscreen.SplashActivity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.interstitial.InterstitialAd
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
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.Locale


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private val REQUEST_CAMERA = 1
    private val REQUEST_GALLERY = 2

    private lateinit var imageViewCamera: LinearLayout
    private lateinit var imageViewGallery: LinearLayout
    private lateinit var imageViewPhoto: ImageView
    private lateinit var history: LinearLayout
    private lateinit var menu: ImageView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var languageArrayList: ArrayList<ModelLanguage>
    private lateinit var destinationLanguageChooseBtn: Button
    private var destinationLanguageCode = "es"
    private var destinationLanguageTitle = "Spanish"
    private var mInterstitialAd: InterstitialAd? = null

    private var sourceLanguageCode = "en"
    public var count = 0
    private var sourceLanguageTitle = "English"
    private lateinit var translateBtn: Button
    private lateinit var sourceLanguage: EditText
    private lateinit var progressDialog: Dialog
    //private lateinit var navigationView: NavigationView
    private lateinit var copy: ImageView
    private lateinit var share: ImageView
    private lateinit var subscriptionManager: SubscriptionManager
    private var textToSpeech: TextToSpeech? = null

    companion object {
        private const val PERMISSION_REQUEST_CODE = 100
        var fuckingText: String = ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        subscriptionManager = SubscriptionManager(this)

        // Check subscription status
        val isMonthlySubscriptionActive = subscriptionManager.isMonthlySubscriptionActive()
        val isYearlySubscriptionActive = subscriptionManager.isYearlySubscriptionActive()
        val isLifetimeSubscriptionActive = subscriptionManager.isLifetimeSubscriptionActive()

        if (isMonthlySubscriptionActive || isYearlySubscriptionActive || isLifetimeSubscriptionActive) {
            // User is subscribed, hide ads
            hideAds()
        } else {
            // User is not subscribed, show ads
            SplashActivity.admobNative.showNative(this, binding.nativeAdContainer, SplashActivity.admobNativeId)
            showAds()
        }

        checkPermissions()

        val imageFilePath = intent.getStringExtra("imageFilePath")
        if (imageFilePath != null) {
            val bitmap = BitmapFactory.decodeFile(imageFilePath)
            binding.imageViewPhoto.setImageBitmap(bitmap)
            val string = intent.getStringExtra("string")
            binding.sourceLanguage.setText(string)
        }

        loadAvailableLanguages()
        destinationLanguageChooseBtn = binding.destinationLanguageChooseBtn
        destinationLanguageChooseBtn.setOnClickListener {
            destinationLanguageChoose()
        }
        translateBtn = binding.translateBtn
        translateBtn.setOnClickListener {
            val isLifetimeSubscriptionActive = subscriptionManager.isLifetimeSubscriptionActive()

            if (isMonthlySubscriptionActive || isYearlySubscriptionActive || isLifetimeSubscriptionActive) {
                validateData()
            } else {

                SplashActivity.admobInter.showInterAd(this) {
                    SplashActivity.admobInter.loadInterAd(
                        this,
                        SplashActivity.admobInterId
                    )
                    validateData()
                }
                if(isClicked){
                    validateData()
                }
                // Show Interstitial ad

            }

        }
        history = binding.history
        history.setOnClickListener {
            val isLifetimeSubscriptionActive = subscriptionManager.isLifetimeSubscriptionActive()

            if (isMonthlySubscriptionActive || isYearlySubscriptionActive || isLifetimeSubscriptionActive) {
                val intent = Intent(this, HistoryActivity::class.java)
                startActivity(intent)
            } else {

                SplashActivity.admobInter.showInterAd(this) {
                    SplashActivity.admobInter.loadInterAd(
                        this,
                        SplashActivity.admobInterId
                    )
                    val intent = Intent(this, HistoryActivity::class.java)
                    startActivity(intent)
                }
                if(isClicked){
                    val intent = Intent(this, HistoryActivity::class.java)
                    startActivity(intent)
                }
                // Show Interstitial ad

            }

        }
        sourceLanguage = binding.sourceLanguage
        val yourString = sourceLanguage.text.toString()
        copy = binding.copy
        share = binding.share
        copy.setOnClickListener {
            yourString?.let { it1 -> copyTextToClipboard(it1) }
        }
        share.setOnClickListener {
            yourString?.let { it1 -> shareImageAndText(it1) }
        }
        menu = binding.menu
        //drawerLayout = binding.drawerLayout

        menu.setOnClickListener {
            // Open the drawer when the menu ImageView is clicked
            val intent = Intent(this, MenuActivity::class.java)
            startActivity(intent)
            //drawerLayout.openDrawer(GravityCompat.START)
        }

        // Add ActionBarDrawerToggle to handle opening and closing the drawer
        /*val toggle = ActionBarDrawerToggle(
            this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
*/
        //navigationView = binding.navigationView
        /*
            navigationView.setNavigationItemSelectedListener { menu ->
                when (menu.itemId) {
                    R.id.menu_remove_ads -> {
                        val intent = Intent(this, InAppActivity::class.java)
                        startActivity(intent)
                        true
                    }
                    R.id.text_to_text -> {
                        val intent = Intent(this, TranslationsActivity::class.java)
                        startActivity(intent)
                        true
                    }
                   */
        /* R.id.menu_app_language -> {
                            // Handle click on the item
                            // For example, you can perform some action here
                            val builder = AlertDialog.Builder(this)
                            builder.setTitle("Coming Soon...")
                            builder.setMessage("This feature is currently under development. Stay tuned!")
                            builder.setPositiveButton("OK") { dialog, _ ->
                                dialog.dismiss()
                            }
                            val dialog = builder.create()
                            dialog.show()
                            true
                        }*//*

                        R.id.menu_share_app -> {
                            // Handle click on the item
                            // For example, you can perform some action here
                            val websiteUri = Uri.parse("https://play.google.com/store/apps/details?id=com.image.to.text.ocrscanner.textconverter.extract.text.translateapp") // Replace "https://example.com" with your website URL
                            val intent = Intent(Intent.ACTION_VIEW, websiteUri)
                            startActivity(intent)
                            true
                        }
                        R.id.menu_privacy_policy -> {
                            // Handle click on the item
                            // For example, you can perform some action here
                            val websiteUri = Uri.parse("https://sites.google.com/view/image-to-text-ocr-extract/home") // Replace "https://example.com" with your website URL
                            val intent = Intent(Intent.ACTION_VIEW, websiteUri)
                            startActivity(intent)
                            true
                        }
                        R.id.menu_rate_app -> {
                            // Handle click on the item
                            // For example, you can perform some action here
                            val websiteUri = Uri.parse("https://example.com") // Replace "https://example.com" with your website URL
                            val intent = Intent(Intent.ACTION_VIEW, websiteUri)
                            startActivity(intent)
                            true
                        }
                        R.id.menu_more_app -> {
                            // Handle click on the item
                            // For example, you can perform some action here
                            val websiteUri = Uri.parse("https://play.google.com/store/apps/developer?id=Sparx+Developer") // Replace "https://example.com" with your website URL
                            val intent = Intent(Intent.ACTION_VIEW, websiteUri)
                            startActivity(intent)
                            true
                        }
                        R.id.terms_and_condotions -> {
                            // Handle click on the item
                            // For example, you can perform some action here
                            val websiteUri = Uri.parse("https://sites.google.com/view/terms-and-conditions-image-to-/home") // Replace "https://example.com" with your website URL
                            val intent = Intent(Intent.ACTION_VIEW, websiteUri)
                            startActivity(intent)
                            true
                        }
                        else -> false
                    }
                }
        */

        imageViewCamera = binding.imageViewCamera
        imageViewGallery = binding.imageViewGallery
        imageViewPhoto = binding.imageViewPhoto

        imageViewCamera.setOnClickListener {
            binding.progressBar.visibility=View.VISIBLE
            count=1
            ad(count)
        }
        imageViewGallery.setOnClickListener {
            binding.progressBar.visibility=View.VISIBLE
            count = 2
            ad(count)
        }
        var isSpeaking = false

// Define a variable to store the text being spoken
        var spokenText: String? = null

// Initialize TextToSpeech in your activity
        textToSpeech = TextToSpeech(
            applicationContext
        ) { status ->
            if (status != TextToSpeech.ERROR) {
                // Set language to Arabic
                textToSpeech?.language = Locale("ar")
            } else {
                Toast.makeText(
                    this@MainActivity,
                    "Text-to-Speech initialization failed",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        val speaker: ImageView = findViewById(R.id.speaker)

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

        val eraseImageView = binding.erase
        eraseImageView.setOnClickListener(){
            binding.sourceLanguage.text.clear()
        }
        val editText = binding.sourceLanguage
        if(binding.sourceLanguage.text.isEmpty()){
            binding.translateBtn.setBackgroundResource(R.drawable.background_white)
            binding.translateBtn.isClickable=false
        }
// Metin değişikliklerini dinlemek için bir TextWatcher oluştur
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Metin değişmeden önceki durumu işlemek için burayı kullanabilirsiniz
                if (s.isNullOrBlank()) {
                    // Metin boşsa düğmenin rengini beyaz yap
                    binding.translateBtn.setBackgroundResource(R.drawable.background_white)
                    binding.translateBtn.isClickable=false
                }
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Metin değiştiğinde yapılacak işlemleri burada gerçekleştirin
                if (s.isNullOrBlank()) {
                    // Metin boşsa düğmenin rengini beyaz yap
                    binding.translateBtn.setBackgroundResource(R.drawable.background_white)
                    binding.translateBtn.isClickable=false
                }

            }
            override fun afterTextChanged(s: Editable?) {
                // Metin değiştikten sonra yapılacak işlemleri burada gerçekleştirin
                if (s.isNullOrBlank()) {
                    // Metin boşsa düğmenin arka planını beyaz yap
                    binding.translateBtn.setBackgroundResource(R.drawable.background_white)
                    binding.translateBtn.isClickable = false
                } else {
                    // Metin doluysa düğmenin arka planını drawable'a ayarla ve tıklanabilirliği etkinleştir
                    binding.translateBtn.setBackgroundResource(R.drawable.background_curve)
                    binding.translateBtn.isClickable = true
                }
            }
        }

// EditText'e TextWatcher'ı ekleyin
        editText.addTextChangedListener(textWatcher)
    }
    private fun speakText(text: String) {
        // Speak the text
        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    // Function to stop speaking
    private fun stopSpeaking() {
        textToSpeech?.stop()
    }
    private fun hideAds() {

    }

    private fun showAds() {

    }
    private fun ad(count: Int) {

        val isMonthlySubscriptionActive = subscriptionManager.isMonthlySubscriptionActive()
        val isYearlySubscriptionActive = subscriptionManager.isYearlySubscriptionActive()
        val isLifetimeSubscriptionActive = subscriptionManager.isLifetimeSubscriptionActive()

        if (isMonthlySubscriptionActive || isYearlySubscriptionActive || isLifetimeSubscriptionActive) {
            if (count == 1) {
                val intent = Intent(this, CameraActivity::class.java)
                startActivity(intent)
                binding.progressBar.visibility=View.GONE
                finish()
//                openCamera()
            } else if (count == 2) {
                binding.progressBar.visibility=View.GONE
                openGallery()
            }
        } else {
            SplashActivity.admobInter.showInterAd(this@MainActivity) {
                SplashActivity.admobInter.loadInterAd(
                    this,
                    SplashActivity.admobInterId
                )
                if(AdmobInter.isClicked)
                {
                    if (count == 1) {
                        if (ContextCompat.checkSelfPermission(
                                this,
                                Manifest.permission.CAMERA
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            // Permission already granted, open camera
                            val intent = Intent(this, CameraActivity::class.java)
                            startActivity(intent)
                            binding.progressBar.visibility=View.GONE
                            finish()
                        } else {
                            // Request permission if not granted
                            ActivityCompat.requestPermissions(
                                this,
                                arrayOf(Manifest.permission.CAMERA),
                                PERMISSION_REQUEST_CODE
                            )
                        }

                    } else if (count == 2) {
                        binding.progressBar.visibility=View.GONE
                        openGallery()
                    }
                }

            }
            if(isClicked)
            {
                if (count == 1) {
                    if (ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        // Permission already granted, open camera
                        val intent = Intent(this, CameraActivity::class.java)
                        startActivity(intent)
                        binding.progressBar.visibility=View.GONE
                        finish()
                    } else {
                        // Request permission if not granted
                        ActivityCompat.requestPermissions(
                            this,
                            arrayOf(Manifest.permission.CAMERA),
                            PERMISSION_REQUEST_CODE
                        )
                    }

                } else if (count == 2) {
                    binding.progressBar.visibility=View.GONE
                    openGallery()
                }
            }
        }


    }
    private fun copyTextToClipboard(text: String) {
        val clipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("text", text)
        clipboardManager.setPrimaryClip(clipData)
        //Toast.makeText(this, "Text copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, open camera
                //openCamera()
            } else {
                // Permission denied, inform user
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
            }
        }
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, initialize Text-to-Speech here or perform any other action
            } else {
                // Permission denied, inform the user or take appropriate action
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                PERMISSION_REQUEST_CODE)
        } else {
            // Permission is already granted
            // Initialize Text-to-Speech here or perform any other action
        }
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

    private fun openCamera() {
        val dialog = (this as? AppCompatActivity)?.supportFragmentManager?.findFragmentByTag("loading_dialog")
        if (dialog is AlertDialog) {
            dialog.dismiss()
        }
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (cameraIntent.resolveActivity(packageManager) != null) {

            startActivityForResult(cameraIntent, REQUEST_CAMERA)
        } else {
            //Toast.makeText(this, "Camera not available", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openGallery() {
        val dialog = (this as? AppCompatActivity)?.supportFragmentManager?.findFragmentByTag("loading_dialog")
        if (dialog is AlertDialog) {
            dialog.dismiss()
        }
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(galleryIntent, REQUEST_GALLERY)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
//                REQUEST_CAMERA -> {
//                    val extras: Bundle? = data?.extras
//                    val imageBitmap: Bitmap? = extras?.get("data") as? Bitmap
//                    imageViewPhoto?.setImageBitmap(imageBitmap)
//
//                    // Convert the captured bitmap to FirebaseVisionImage
//                    imageBitmap?.let { processImage(it) }
//                }
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
                //Toast.makeText(this, "Text recognition failed", Toast.LENGTH_SHORT).show()
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
        val countryFlags=TranslateLanguage.getAllLanguages()
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
                    Toast.makeText(this@MainActivity, "Failed! ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun navigateToViewTranslationActivity(filePath: String, translatedText: String) {
        val intent = Intent(applicationContext, ViewTranslationActivity::class.java)
        intent.putExtra("imageFilePath", filePath)
        intent.putExtra("yourString", translatedText)
        startActivity(intent)
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.drawer_menu, menu)

        // Inflate the header layout
        val headerView = layoutInflater.inflate(R.layout.menu_header, null)

        // Add the header to the menu
        val header = menu.add(Menu.NONE, Menu.NONE, Menu.NONE, "header")
        header.actionView = headerView
        header.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)

        return true
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
    @SuppressLint("MissingSuperCall")
        override fun onBackPressed() {
            val dialogView = layoutInflater.inflate(R.layout.dialog_exit, null)
            val dialogMessage = dialogView.findViewById<TextView>(R.id.dialog_message)
            val btnExit = dialogView.findViewById<TextView>(R.id.btn_exit)
            val btnCancel = dialogView.findViewById<Button>(R.id.btn_cancel)

            val dialogBuilder = AlertDialog.Builder(this, R.style.CustomAlertDialogStyle)
                .setView(dialogView)
                .setCancelable(false)

            val alertDialog = dialogBuilder.create()

            btnExit.setOnClickListener {
                // Exit the app
                finish()
                alertDialog.dismiss()
            }

            btnCancel.setOnClickListener {
                // Dismiss the dialog
                alertDialog.dismiss()
            }

            alertDialog.show()
        }

    override fun onResume() {
        super.onResume()
        counter++
    }
}
