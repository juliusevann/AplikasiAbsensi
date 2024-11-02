package com.example.utsmaplab

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

class AbsensiActivity : AppCompatActivity() {

    private lateinit var previewView: PreviewView
    private lateinit var absenImage: ImageView
    private lateinit var captureButton: ImageView
    private lateinit var uploadButton: ImageView

    private lateinit var imageCapture: ImageCapture
    private var lastCapturedImageFile: File? = null

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                startCamera()
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_absensi)

        previewView = findViewById(R.id.preview_view)
        absenImage = findViewById(R.id.absenImage)
        captureButton = findViewById(R.id.captureButton)
        uploadButton = findViewById(R.id.uploadButton)

        // Periksa izin kamera
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }

        // Set Listener untuk tombol
        captureButton.setOnClickListener { takePicture() }
        uploadButton.setOnClickListener { uploadImage() }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            imageCapture = ImageCapture.Builder().build()
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            } catch (exc: Exception) {
                Toast.makeText(this, "Error starting camera: ${exc.message}", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePicture() {
        // Cek apakah ImageCapture sudah diinisialisasi
        if (!::imageCapture.isInitialized) {
            Toast.makeText(this, "Image capture is not initialized", Toast.LENGTH_SHORT).show()
            return
        }

        val photoFile = File(externalMediaDirs.first(), "${System.currentTimeMillis()}.jpg")
        lastCapturedImageFile = photoFile
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this), object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                Toast.makeText(this@AbsensiActivity, "Image saved: ${photoFile.absolutePath}", Toast.LENGTH_SHORT).show()
                displayImage(photoFile)
            }

            override fun onError(exception: ImageCaptureException) {
                Toast.makeText(this@AbsensiActivity, "Error capturing image: ${exception.message}", Toast.LENGTH_SHORT).show()
                exception.printStackTrace() // Tambahkan untuk men-debug kesalahan
            }
        })
    }

    private fun displayImage(file: File) {
        Glide.with(this)
            .load(file)
            .into(absenImage)
        absenImage.visibility = ImageView.VISIBLE
        previewView.visibility = PreviewView.GONE
    }

    private fun uploadImage() {
        val imageFile = lastCapturedImageFile
        if (imageFile == null || !imageFile.exists()) {
            Toast.makeText(this, "No image captured to upload", Toast.LENGTH_SHORT).show()
            return
        }

        val imageUri = Uri.fromFile(imageFile)
        val storageRef = FirebaseStorage.getInstance().reference.child("absensi/${imageFile.name}")

        storageRef.putFile(imageUri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    val db = FirebaseFirestore.getInstance()
                    val absensi = hashMapOf(
                        "imageUrl" to uri.toString(),
                        "timestamp" to FieldValue.serverTimestamp(),
                        "date" to SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(System.currentTimeMillis()),
                        "time" to SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(System.currentTimeMillis())
                    )

                    db.collection("absensi").add(absensi)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Image uploaded successfully!", Toast.LENGTH_SHORT).show()
                            absenImage.visibility = ImageView.GONE
                            previewView.visibility = PreviewView.VISIBLE
                        }
                        .addOnFailureListener { exception ->
                            Toast.makeText(this, "Error uploading data to Firestore: ${exception.message}", Toast.LENGTH_SHORT).show()
                        }
                }.addOnFailureListener {
                    Toast.makeText(this, "Failed to get download URL", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error uploading image to Storage: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
