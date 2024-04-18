package com.example.image_to_text.ui.activities

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import com.example.image_to_text.databinding.ActivityCameraBinding
import com.example.image_to_text.ui.ViewModel.GraphicOverlay
import com.example.image_to_text.ui.ViewModel.SharedViewModel
import com.example.image_to_text.ui.ViewModel.TextGraphic
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.yalantis.ucrop.UCrop
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.ExecutorService
import android.media.Image
import com.example.image_to_text.R
import com.example.image_to_text.ui.ViewModel.SubscriptionManager.SubscriptionManager
import com.example.image_to_text.ui.utils.Utils
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import java.io.InputStream

class CameraActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCameraBinding // Declare binding variable
    private var imageCapture: ImageCapture? = null
    private lateinit var viewModel: SharedViewModel
    private var isImageView = false
    private var cameraExecutor: ExecutorService? = null
    private var isFlashOn = false
    private val REQUEST_CODE_PICK_IMAGE = 100
    private lateinit var subscriptionManager: SubscriptionManager
    private lateinit var progressDialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater) // Inflate the binding layout
        setContentView(binding.root) // Use the root of the binding layout as the content view
        Utils.logAnalytic("Camera Activity")
        viewModel = ViewModelProvider(this)[SharedViewModel::class.java]
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this as Activity, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }
        subscriptionManager = SubscriptionManager(this)

        val adView: AdView = binding.adView
        val isMonthlySubscriptionActive = subscriptionManager.isMonthlySubscriptionActive()
        val isYearlySubscriptionActive = subscriptionManager.isYearlySubscriptionActive()
        val isLifetimeSubscriptionActive = subscriptionManager.isLifetimeSubscriptionActive()

        if (isMonthlySubscriptionActive || isYearlySubscriptionActive || isLifetimeSubscriptionActive) {
            // User is subscribed, hide ads
            adView?.visibility= View.GONE
            //Toast.makeText(this, "Thank you for subscribing!", Toast.LENGTH_SHORT).show()
        } else {
            val adRequest = AdRequest.Builder().build()
            adView.loadAd(adRequest)
            Utils.logAnalytic("Camera Activity banner ad showed")
        }
        // Your activity initialization code here
        binding.imageCaptureBtn.setOnClickListener {
            Utils.logAnalytic("Camera Activity capture image")
            takePhoto()
            Toast.makeText(this, "please wait ...", Toast.LENGTH_LONG).show()
        }
        binding.save.setOnClickListener(){
            Utils.logAnalytic("Camera Activity done button clicked")
            progressDialog = Dialog(this)
            progressDialog.setCanceledOnTouchOutside(false)
            progressDialog.setContentView(R.layout.custom_dialog)
            progressDialog.show()
            viewModel = ViewModelProvider(this).get(SharedViewModel::class.java)
            viewModel.textBlocks.observe(this) { textBlocks ->
                // Check if textBlocks has data
                if (textBlocks.isNullOrEmpty()) {
                    // If no data, handle accordingly (e.g., show a message)
                    Toast.makeText(this, "No text detected", Toast.LENGTH_SHORT).show()
                } else {
                    // If data is available, update the TextView with the retrieved text
                    val stringBuilder = StringBuilder()
                    for (block in textBlocks) {
                        stringBuilder.append(block.text).append("\n")
                    }
                    //stringSaver.saveString(stringBuilder.toString())
                    /*viewModel1.setString(stringBuilder.toString())

                    fuckingText = stringBuilder.toString()
                    */
                    val drawable = binding.imageView.drawable

// Convert the drawable to a Bitmap
                    val bitmap = (drawable as BitmapDrawable).bitmap
                    val file = File(cacheDir, "image.jpg") // Create a file to save the bitmap
                    try {
                        val outputStream = FileOutputStream(file)
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream) // Compress bitmap and save to file
                        outputStream.flush()
                        outputStream.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                    val intent = Intent(this, TextExtractionActivity::class.java).apply {
                        putExtra("imageFilePath", file.absolutePath)
                        putExtra("string", stringBuilder.toString())
                        finish()
                    }
                    progressDialog.dismiss()
                    startActivity(intent)
                    //Toast.makeText(this, ""+stringBuilder, Toast.LENGTH_SHORT).show()
                    //binding.sourceLanguage.text = stringBuilder.toString().trim().toEditable()
                }
            }

        }
        binding.close.setOnClickListener(){
            Utils.logAnalytic("Camera Activity close button clicked")
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        binding.flash.setOnClickListener(){
            Utils.logAnalytic("Camera Activity flash button clicked")
            toggleFlashlight()
        }
        binding.gallery.setOnClickListener {
            Utils.logAnalytic("Camera Activity gallery button clicked")
            openGallery()
        }
    }
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE)
    }
    private fun takePhoto() {
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return

        // Set up image capture listener, which is triggered after photo has
        // been taken
        imageCapture.takePicture(
            ContextCompat.getMainExecutor(this),
            @ExperimentalGetImage object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    super.onCaptureSuccess(image)
                    val m = image.image
                    if (m != null) {
                        runTextRecognition(m)
                    }
                }
                override fun onError(exception: ImageCaptureException) {
                    super.onError(exception)
                    Log.d(TAG, "Image not captured")
                }
            }
        )
    }

    private fun runTextRecognition(bitmap: Bitmap) {
        val image = InputImage.fromBitmap(bitmap, 0)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        recognizer.process(image)
            .addOnSuccessListener { texts ->
                processTextRecognitionResult(texts)
            }
            .addOnFailureListener { e -> // Task failed with an exception
                e.printStackTrace()
            }
    }

    private fun runTextRecognition(mSelectedImage: Image) {
        val image = InputImage.fromMediaImage(mSelectedImage, 0)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        recognizer.process(image)
            .addOnSuccessListener { texts ->
                toggleImageView(image, true)
                processTextRecognitionResult(texts)
            }
            .addOnFailureListener { e -> // Task failed with an exception
                e.printStackTrace()
            }
    }
    private fun toggleImageView(image: InputImage?, imageView: Boolean) {
        isImageView = imageView
        if (imageView) {
            binding.preview.visibility = View.GONE
            binding.imageCaptureBtn.visibility = View.GONE
            binding.imageView.visibility=View.VISIBLE
            binding.imageView.setImageBitmap(image!!.bitmapInternal)
            image!!.bitmapInternal?.let { startCrop(it) }
            //image!!.bitmapInternal?.let { viewModel1.setBitmap(it) }
            binding.cameraLayout.visibility=View.GONE
            binding.save.isClickable=true
        } else {
            binding.imageGroup.visibility = View.GONE
            binding.preview.visibility = View.VISIBLE
            binding.imageCaptureBtn.visibility = View.VISIBLE
        }

        ActivityCompat.invalidateOptionsMenu(this as Activity)
    }
    fun startCrop(imageBitmap: Bitmap) {
        try {
            // Create a temporary file to store the bitmap
            val tempFile = File.createTempFile("temp_image", ".jpg", applicationContext.cacheDir)
            val outputStream = FileOutputStream(tempFile)

            // Compress the bitmap and write it to the file
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.flush()
            outputStream.close()

            // Get the Uri of the temporary file
            val sourceUri = FileProvider.getUriForFile(
                this@CameraActivity,
                "com.example.image_to_text.fileprovider",
                tempFile
            )

            // Define the destination Uri where the cropped image will be saved
            val destinationUri = Uri.fromFile(File(applicationContext.cacheDir, "cropped_image.jpg"))
            binding.save.visibility=View.VISIBLE
            // Start the cropping activity using UCrop
            UCrop.of(sourceUri, destinationUri)
                .withOptions(getCropOptions()) // Customize cropping options if needed
                .start(this@CameraActivity) // Start the cropping activity
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        viewModel.clearData()
        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                // Start cropping activity for the selected image
                startCrop(uri)
            }
        } else if (requestCode == UCrop.REQUEST_CROP && resultCode == RESULT_OK) {
            viewModel.clearData()
            val resultUri = UCrop.getOutput(data!!)
            // Load the cropped image into your ImageView
            resultUri?.let { uri ->
                binding.imageView.setImageURI(uri)
                binding.imageView.visibility = View.VISIBLE
                binding.save.visibility = View.VISIBLE
                binding.cameraLayout.visibility=View.GONE
                binding.preview.visibility=View.GONE
                val bitmap: Bitmap? = getBitmapFromUri(uri)
                bitmap?.let {
                    // Perform text recognition on the cropped image
                    runTextRecognition(it)
                }
            }
        } else if (resultCode == UCrop.RESULT_ERROR) {
            val error = UCrop.getError(data!!)
            error?.printStackTrace()

            Toast.makeText(this, "Error cropping image", Toast.LENGTH_SHORT).show()
        }
    }
    private fun startCrop(uri: Uri) {
        val destinationUri = Uri.fromFile(File(applicationContext.cacheDir, "cropped_image.jpg"))
        UCrop.of(uri, destinationUri)
            .withOptions(getCropOptions())
            .start(this)
    }
    private fun getBitmapFromUri(uri: Uri): Bitmap? {
        val inputStream: InputStream? = contentResolver.openInputStream(uri)
        inputStream?.use { stream ->
            return BitmapFactory.decodeStream(stream)
        }
        return null
    }



    fun getCropOptions(): UCrop.Options {
        val options = UCrop.Options()
        // Customize cropping options here if needed
        return options
    }
    private fun processTextRecognitionResult(texts: Text) {
        val blocks: List<Text.TextBlock> = texts.textBlocks
        if (blocks.isEmpty()) {
            Log.d(TAG, "No text found")

            Toast.makeText(this, "No text found", Toast.LENGTH_LONG).show()
            return
        }
        binding.graphicOverlay.clear()
        viewModel.setTextBlocks(blocks)
        for (i in blocks.indices) {
            val lines: List<Text.Line> = blocks[i].lines
            for (j in lines.indices) {
                val elements: List<Text.Element> = lines[j].elements
                for (k in elements.indices) {
                    val textGraphic: GraphicOverlay.Graphic = TextGraphic(binding.graphicOverlay, elements[k])
                    binding.graphicOverlay.add(textGraphic)
                    Log.e("graphics",textGraphic.toString())
                }
            }
        }

    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            this, it) == PackageManager.PERMISSION_GRANTED
    }
    companion object {
        private const val TAG = "MainFragment"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS =
            mutableListOf (
                Manifest.permission.CAMERA,
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
        var capturedBitmap: Bitmap? = null

    }
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.preview.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder()
                .build()

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture)

            } catch(exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }
    private fun toggleFlashlight() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val camera = cameraProvider.bindToLifecycle(
                this, CameraSelector.DEFAULT_BACK_CAMERA
            )

            val cameraControl = camera.cameraControl
            val cameraInfo = camera.cameraInfo

            if (isFlashOn) {
                // Turn off flashlight
                cameraControl.enableTorch(false)
                isFlashOn = false
            } else {
                // Turn on flashlight
                cameraControl.enableTorch(true)
                isFlashOn = true
            }
        }, ContextCompat.getMainExecutor(this))
    }

    override fun onDestroy() {
        super.onDestroy()
        // Shut down the cameraExecutor
        cameraExecutor?.shutdown()
    }
    override fun onBackPressed() {
        super.onBackPressed()
        Utils.logAnalytic("Camera Activity back button clicked")
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish() // Finish the current activity
    }

}
