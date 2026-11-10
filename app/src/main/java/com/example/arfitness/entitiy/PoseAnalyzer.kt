package com.example.arfitness.entitiy

import android.annotation.SuppressLint
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseDetector
import com.google.mlkit.vision.pose.PoseDetectorOptionsBase
import com.google.mlkit.vision.pose.accurate.AccuratePoseDetectorOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * @param onPoseDetected (poz, görüntü genişliği, görüntü yüksekliği, karenin çekildiği an [ms])
 */
class PoseAnalyzer(private val onPoseDetected: (Pose, Int, Int, Long) -> Unit) : ImageAnalysis.Analyzer {

    private lateinit var detector: PoseDetector

    suspend fun hazirla() {
        withContext(Dispatchers.IO) {
            val builder = AccuratePoseDetectorOptions.Builder()
                .setDetectorMode(AccuratePoseDetectorOptions.STREAM_MODE)
            if (USE_GPU) {
                // Accurate model CPU'da orta seviye telefonlarda ~8–15 fps çalışıyor; düşük fps hızlı
                // tekrarlarda dip noktanın kaçmasına yol açar. CPU_GPU: GPU varsa onu kullan, yoksa CPU'ya dön.
                // Sorun çıkarsa (yanlış iskelet, çökme) USE_GPU = false yap.
                builder.setPreferredHardwareConfigs(PoseDetectorOptionsBase.CPU_GPU)
            }
            val options = builder.build()

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

            // Sayım, karenin işlendiği an değil çekildiği an üzerinden yapılır (ns → ms)
            val timestampMs = imageProxy.imageInfo.timestamp / 1_000_000L

            detector.process(image)
                .addOnSuccessListener { pose ->
                    onPoseDetected(pose, uprightWidth, uprightHeight, timestampMs)
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

    private companion object {
        const val USE_GPU = true
    }
}