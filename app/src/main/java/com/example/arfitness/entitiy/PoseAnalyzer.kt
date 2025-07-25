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
 // <-- ÖNEMLİ IMPORT
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await // await() kullanmak için bu import gerekli
import kotlinx.coroutines.withContext

class PoseAnalyzer(private val onPoseDetected: (Pose, Int, Int) -> Unit) : ImageAnalysis.Analyzer {

    // Detector'ı hemen oluşturmuyoruz, 'lateinit' ile daha sonra oluşturacağımızı belirtiyoruz.
    private lateinit var detector: PoseDetector

    /**
     * Bu fonksiyon, ağır olan detector başlatma işini yapar.
     * 'suspend' olduğu için bir coroutine içinde çağrılmalıdır.
     */
    suspend fun hazirla() {
        // withContext(Dispatchers.IO), bu bloğun arka plan(ıo) thread'inde çalışmasını sağlar.
        withContext(Dispatchers.IO) {

            val options = AccuratePoseDetectorOptions.Builder()
                .setDetectorMode(AccuratePoseDetectorOptions.STREAM_MODE)
                .build()

            detector = PoseDetection.getClient(options)
        }
    }

    @SuppressLint("UnsafeOptInUsageError") //Bu uyarıyı anlıyorum ve risklerinin farkındayım, lütfen bu satırda veya blokta bu uyarıyı görmezden gel."
    override fun analyze(imageProxy: ImageProxy) {
        // Eğer detector henüz hazırlanmadıysa, analizi atla.

        if (!::detector.isInitialized) {
            imageProxy.close()
            return
        }

        val mediaImage = imageProxy.image
        if (mediaImage != null) {

            Log.d("ImageInfo", "Görüntü Rotasyonu: ${imageProxy.imageInfo.rotationDegrees}, Boyutlar: ${imageProxy.width}x${imageProxy.height}")

            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            // ML Kit'in process fonksiyonu bir Google Task döndürür.

            detector.process(image)
                .addOnSuccessListener { pose ->
                    // Başarıyla bir iskelet bulunursa, sonucu geri gönder.
                    onPoseDetected(pose,imageProxy.height,imageProxy.width)
                }
                .addOnFailureListener { e ->
                    // Hata durumunda loglama yapabiliriz.
                    // Log.e("PoseAnalyzer", "Poz işlenemedi.", e)
                }
                .addOnCompleteListener {
                    // Analiz bittiğinde kaynağı serbest bırak, bu çok önemli!
                    imageProxy.close()
                }
        }
    }
}