package com.example.arfitness.analysis

import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark

abstract class ExerciseAnalyzer {
    var lastRepTime: Long = 0
    private var smoothedAngles = mutableMapOf<String, Double>()
    
    open val requiredLandmarks: List<Int>
        get() = emptyList() // Default empty list for backward compatibility

    // Uzuvların %60 üzerinde net görünme şartı
    fun isVisible(vararg landmarks: PoseLandmark?): Boolean {
        return landmarks.all { it != null && it.inFrameLikelihood > 0.6f }
    }

    // Titreme engelleyici Low-Pass (Hareketli Ortalama) filtresi
    fun getSmoothedAngle(angleName: String, currentRawAngle: Double, alpha: Double = 0.3): Double {
        val previousAngle = smoothedAngles[angleName]
        if (previousAngle == null) {
            smoothedAngles[angleName] = currentRawAngle
            return currentRawAngle
        }
        val newSmoothed = (alpha * currentRawAngle) + ((1.0 - alpha) * previousAngle)
        smoothedAngles[angleName] = newSmoothed
        return newSmoothed
    }

    private var angleBuffers = mutableMapOf<String, MutableList<Double>>()

    /**
     * DTW Zaman Serisi Eşleştirme Motoru
     * Canlı kamera açılarını, ideal şablon grafiği ile karşılaştırır.
     */
    fun matchPatternDTW(
        bufferKey: String,
        currentAngle: Double,
        template: List<Double>,
        threshold: Double = 22.0,
        bufferSize: Int = 25,
        cooldownMs: Long = 1000
    ): Boolean {
        val buffer = angleBuffers.getOrPut(bufferKey) { mutableListOf() }
        
        buffer.add(currentAngle)
        if (buffer.size > bufferSize) {
            buffer.removeAt(0) // En eski veriyi sil (Sliding Window)
        }

        // Buffer en az yarı yarıya doluysa ve cooldown dolduysa analiz yap
        if (buffer.size >= (bufferSize / 2) && canCountRep(cooldownMs)) {
            val distance = DTWUtils.computeDTWDistance(buffer, template)
            if (distance < threshold) {
                buffer.clear() // Eşleşme olduysa buffer'ı temizle (Çift saymayı engeller)
                return true
            }
        }
        return false
    }

    // İnsani hız sınırı (Varsayılan 1 saniye bekleme)
    fun canCountRep(cooldownMs: Long = 1000): Boolean {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastRepTime > cooldownMs) {
            lastRepTime = currentTime
            return true
        }
        return false
    }

    abstract fun analyze(pose: Pose): Feedback
}