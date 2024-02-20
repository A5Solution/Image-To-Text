package com.example.image_to_text.ui

import android.app.Activity
import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.opengl.Visibility
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.image_to_text.R
import com.example.image_to_text.ui.SubscriptionManager.SubscriptionManager
import com.example.image_to_text.ui.history.HistoryActivity
import com.example.image_to_text.ui.inapp.InAppActivity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
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
    private val REQUEST_CAMERA = 1
    private val REQUEST_GALLERY = 2

    private var imageViewCamera: MaterialButton? = null
    private var imageViewGallery: MaterialButton? = null
    private var imageViewPhoto: ImageView? = null
    private var history: ImageView? = null
    private var menu: ImageView? = null
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var languageArrayList: ArrayList<ModelLanguage>
    private lateinit var destinationLanguageChooseBtn: MaterialButton
    private var destinationLanguageCode = "es"
    private var destinationLanguageTitle = "Spanish"
    private var mInterstitialAd: InterstitialAd? = null

    private var sourceLanguageCode = "en"
    public var count = 0
    private var sourceLanguageTitle = "English"
    private lateinit var translateBtn: MaterialButton
    private lateinit var sourceLanguage: EditText
    private lateinit var progressDialog: Dialog
    private lateinit var navigationView: NavigationView
    private lateinit var copy: ImageView
    private lateinit var share: ImageView
    private lateinit var subscriptionManager: SubscriptionManager
    private lateinit var adView: AdView



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        setContentView(com.example.image_to_text.R.layout.activity_main)
        adView=findViewById(R.id.adView)
        subscriptionManager = SubscriptionManager(this)

        // Check subscription status
        val isMonthlySubscriptionActive = subscriptionManager.isMonthlySubscriptionActive()
        val isYearlySubscriptionActive = subscriptionManager.isYearlySubscriptionActive()
        val isLifetimeSubscriptionActive = subscriptionManager.isLifetimeSubscriptionActive()

        if (isMonthlySubscriptionActive || isYearlySubscriptionActive || isLifetimeSubscriptionActive) {
            // User is subscribed, hide ads
            hideAds()
            //Toast.makeText(this, "Thank you for subscribing!", Toast.LENGTH_SHORT).show()
        } else {
            // User is not subscribed, show ads
            showAds()
        }
        loadAvailableLanguages()
        destinationLanguageChooseBtn = findViewById<MaterialButton>(R.id.destinationLanguageChooseBtn)
        destinationLanguageChooseBtn.setOnClickListener {
            destinationLanguageChoose()
        }
        translateBtn = findViewById(R.id.translateBtn)
        translateBtn.setOnClickListener {
            validateData()
        }
        history = findViewById(R.id.history)
        history?.setOnClickListener {
            val intent = Intent(this, HistoryActivity::class.java)
            startActivity(intent)
        }
        sourceLanguage = findViewById(R.id.sourceLanguage)
        val yourString=sourceLanguage.text.toString()
        copy = findViewById(R.id.copy)
        share = findViewById(R.id.share)
        copy.setOnClickListener {
            yourString?.let { it1 -> copyTextToClipboard(it1) }
        }
        share.setOnClickListener {
            yourString?.let { it1 -> shareImageAndText(it1) }
        }
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


        navigationView=findViewById(R.id.navigationView)
        navigationView.setNavigationItemSelectedListener { menu->
            when (menu.itemId){
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
                R.id.menu_app_language -> {
                    // Handle click on the item
                    // For example, you can perform some action here
                    true
                }
                R.id.menu_share_app -> {
                    // Handle click on the item
                    // For example, you can perform some action here
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


        imageViewCamera = findViewById<MaterialButton>(R.id.imageViewCamera)
        imageViewGallery = findViewById<MaterialButton>(R.id.imageViewGallery)
        imageViewPhoto = findViewById<ImageView>(R.id.imageViewPhoto)

        imageViewCamera?.setOnClickListener {
            count=1
            ad(count)
        }
        imageViewGallery?.setOnClickListener {

            count=2
            ad(count)
        }
    }
    private fun hideAds() {
        adView.visibility = View.GONE
    }

    private fun showAds() {
        adView.visibility = View.VISIBLE
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
    }
    private fun ad(count: Int) {

        val isMonthlySubscriptionActive = subscriptionManager.isMonthlySubscriptionActive()
        val isYearlySubscriptionActive = subscriptionManager.isYearlySubscriptionActive()
        val isLifetimeSubscriptionActive = subscriptionManager.isLifetimeSubscriptionActive()

        if (isMonthlySubscriptionActive || isYearlySubscriptionActive || isLifetimeSubscriptionActive) {
            if (count == 1) {
                openCamera()
            } else if (count == 2) {
                openGallery()
            }
        } else {
            val loadingDialog = AlertDialog.Builder(this)
                .setMessage("Loading...")
                .setCancelable(false)
                .create()
            loadingDialog.show()

            val adRequest = AdRequest.Builder().build()

            InterstitialAd.load(this, "ca-app-pub-7055337155394452/3471919069", adRequest, object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    Log.d(ContentValues.TAG, "Ad was loaded.")
                    mInterstitialAd = interstitialAd
                    mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            super.onAdDismissedFullScreenContent()
                            // Dismiss the loading dialog when ad is dismissed
                            loadingDialog.dismiss()
                        }
                    }
                    if (count == 1) {
                        openCamera()
                    } else if (count == 2) {
                        openGallery()
                    }
                    // Show the ad
                    mInterstitialAd?.show(this@MainActivity)
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    loadingDialog.dismiss()
                    Log.e(ContentValues.TAG, "Ad failed to load: $adError")
                }
            })
        }

    }
    private fun copyTextToClipboard(text: String) {
        val clipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("text", text)
        clipboardManager.setPrimaryClip(clipData)
        //Toast.makeText(this, "Text copied to clipboard", Toast.LENGTH_SHORT).show()
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
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.drawer_menu, menu) // Replace "your_menu_file_name" with the name of your menu XML file

        // Set click listener for menu items
        menu?.apply {
            findItem(R.id.menu_remove_ads)?.setOnMenuItemClickListener {
                // Handle Remove Ads click
                true
            }
            findItem(R.id.menu_app_language)?.setOnMenuItemClickListener {
                // Handle App Language click
                true
            }
            findItem(R.id.menu_share_app)?.setOnMenuItemClickListener {
                // Handle Share App click
                val websiteUri = Uri.parse("https://example.com") // Replace "https://example.com" with your website URL
                val intent = Intent(Intent.ACTION_VIEW, websiteUri)
                startActivity(intent)
                true
            }
            findItem(R.id.menu_privacy_policy)?.setOnMenuItemClickListener {
                // Handle Privacy Policy click
                val websiteUri = Uri.parse("https://sites.google.com/view/image-to-text-ocr-extract/home") // Replace "https://example.com" with your website URL
                val intent = Intent(Intent.ACTION_VIEW, websiteUri)
                startActivity(intent)
                true
            }
            findItem(R.id.menu_rate_app)?.setOnMenuItemClickListener {
                // Handle Rate App click
                val websiteUri = Uri.parse("https://example.com") // Replace "https://example.com" with your website URL
                val intent = Intent(Intent.ACTION_VIEW, websiteUri)
                startActivity(intent)
                true
            }
            findItem(R.id.menu_more_app)?.setOnMenuItemClickListener {
                // Handle More App click
                val websiteUri = Uri.parse("https://play.google.com/store/apps/developer?id=Sparx+Developer") // Replace "https://example.com" with your website URL
                val intent = Intent(Intent.ACTION_VIEW, websiteUri)
                startActivity(intent)
                true
            }
            findItem(R.id.terms_and_condotions)?.setOnMenuItemClickListener {
                // Handle Terms and Conditions click
                val websiteUri = Uri.parse("https://sites.google.com/view/terms-and-conditions-image-to-/home") // Replace "https://example.com" with your website URL
                val intent = Intent(Intent.ACTION_VIEW, websiteUri)
                startActivity(intent)
                true
            }
        }

        return true
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
                    val imageViewPhoto = findViewById<ImageView>(R.id.imageViewPhoto)
                    val bitmap = (imageViewPhoto.drawable as BitmapDrawable).bitmap

                    val file = saveBitmapToFile(bitmap)
                    val filePath = file.absolutePath
                    val intent = Intent(applicationContext, ViewTranslationActivity::class.java)
                    intent.putExtra("imageFilePath", filePath)
                    intent.putExtra("yourString", translatedText.toString())
                    startActivity(intent)


                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    progressDialog.dismiss()
                    Toast.makeText(this@MainActivity, "Failed! ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    fun saveBitmapToFile(bitmap: Bitmap): File {
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
}
