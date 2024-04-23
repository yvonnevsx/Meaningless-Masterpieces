package com.example.myapplication.ui.dashboard

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentDashboardBinding
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var capturedImageView: ImageView
    private lateinit var acceptButton: ImageButton
    private lateinit var deleteButton: ImageButton

    private var capturedImageUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cameraExecutor = Executors.newSingleThreadExecutor()

// Request camera permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissions()
        }

        capturedImageView = view.findViewById(R.id.capturedImageView)

        acceptButton = view.findViewById(R.id.acceptButton)
        deleteButton = view.findViewById(R.id.deleteButton)

        acceptButton.setOnClickListener { acceptPhoto() }
        deleteButton.setOnClickListener { deletePhoto() }

        binding.imageCaptureButton.setOnClickListener { takePhoto() }


        cameraExecutor = Executors.newSingleThreadExecutor()

    }

    private fun savePhoto(uri: Uri): Uri? {
        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
            .format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/MeaninglessMasterpieces")
            }
        }

        val contentResolver = requireContext().contentResolver
        return contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            ?.also { newUri ->
                try {
                    contentResolver.openInputStream(uri)?.use { inputStream ->
                        contentResolver.openOutputStream(newUri)?.use { outputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }

                    Toast.makeText(requireContext(), "Image saved successfully", Toast.LENGTH_SHORT)
                        .show()
                    return newUri
                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(requireContext(), "Failed to save image", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        return null
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(
                requireContext().contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, "MeaninglessMasterpiece")
                    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                        put(
                            MediaStore.Images.Media.RELATIVE_PATH,
                            "Pictures/MeaninglessMasterpieces"
                        )
                    }
                }
            )
            .build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val msg = "Art Captured! Delete or Save?"
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                    Log.d(TAG, msg)

                    capturedImageUri = output.savedUri

                    capturedImageView.setImageURI(output.savedUri)
                    capturedImageView.visibility = View.VISIBLE

                    acceptButton.visibility = View.VISIBLE
                    deleteButton.visibility = View.VISIBLE

                    binding.viewFinder.visibility = View.GONE
                }
            }
        )
    }

    private fun acceptPhoto() {
        capturedImageUri?.let { uri ->
            val savedUri = savePhoto(uri)
            if (savedUri != null) {
                // Navigate to the new activity and pass the Uri
                val intent = Intent(requireContext(), PhotoDisplayActivity::class.java).apply {
                    putExtra("imageUri", savedUri.toString())
                }
                startActivity(intent)
            }
        }

        capturedImageView.visibility = View.GONE

        // Show PreviewView
        binding.viewFinder.visibility = View.VISIBLE

        // Hide accept and delete buttons
        acceptButton.visibility = View.GONE
        deleteButton.visibility = View.GONE
    }

    private fun deletePhoto() {
        // Ensure capturedImageUri is not null
        capturedImageUri?.let { uri ->
            // Use content resolver to delete the image file
            try {
                requireContext().contentResolver.delete(uri, null, null)
                Toast.makeText(requireContext(), "Image deleted", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Failed to delete image", Toast.LENGTH_SHORT)
                    .show()
            }

            // Clear the capturedImageUri
            capturedImageUri = null
        }

        // Clear the capturedImageView
        capturedImageView.setImageURI(null)

        // Hide capturedImageView
        capturedImageView.visibility = View.GONE

        // Show PreviewView
        binding.viewFinder.visibility = View.VISIBLE

        // Hide accept and delete buttons
        acceptButton.visibility = View.GONE
        deleteButton.visibility = View.GONE
    }


    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this.requireContext())

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }
            imageCapture = ImageCapture.Builder().build()

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )

            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this.requireContext()))
    }

    private fun requestPermissions() {
        activityResultLauncher.launch(REQUIRED_PERMISSIONS)
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            requireContext(), it
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    private val activityResultLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        )
        { permissions ->
            // Handle Permission granted/rejected
            var permissionGranted = true
            permissions.entries.forEach {
                if (it.key in REQUIRED_PERMISSIONS && it.value == false)
                    permissionGranted = false
            }
            if (!permissionGranted) {
                Toast.makeText(
                    requireContext(),
                    "Permission request denied",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                startCamera()
            }
        }

    companion object {
        private const val TAG = "CameraXApp"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private val REQUIRED_PERMISSIONS =
            mutableListOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }
}