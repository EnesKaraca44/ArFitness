package com.example.arfitness.entitiy

import android.annotation.SuppressLint
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseDetector
import com.google.mlkit.vision.pose.accurate.AccuratePoseDetectorOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PoseAnalyzer(private val onPoseDetected: (Pose, Int, Int) -> Unit) : ImageAnalysis.Analyzer {

    private lateinit var detector: PoseDetector

    suspend fun hazirla() {
        withContext(Dispatchers.IO) {
            val options = AccuratePoseDetectorOptions.Builder()
                .setDetectorMode(AccuratePoseDetectorOptions.STREAM_MODE)
                .build()

            detector = PoseDetection.getClient(options)
        }
    }

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        if (!::detector.isInitialized) {
            imageProxy.close()
            return
        }

        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val rotationDegrees = imageProxy.imageInfo.rotationDegrees
            val image = InputImage.fromMediaImage(mediaImage, rotationDegrees)

            // ML Kit döndürülmüş (upright) koordinat sistemi kullanır.
            val isRotated = rotationDegrees == 90 || rotationDegrees == 270
            val uprightWidth = if (isRotated) imageProxy.height else imageProxy.width
            val uprightHeight = if (isRotated) imageProxy.width else imageProxy.height

            detector.process(image)
                .addOnSuccessListener { pose ->
                    onPoseDetected(pose, uprightWidth, uprightHeight)
                }
                .addOnFailureListener { e ->
                    Log.e("PoseAnalyzer", "Poz işlenemedi.", e)
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }
}