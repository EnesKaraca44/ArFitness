package com.example.arfitness.analysis

import com.google.mlkit.vision.pose.PoseLandmark
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.sqrt

/**
 * Analiz için kullanılacak genel yardımcı fonksiyonları içerir.
 */
object AnalysisUtils {

    /**
     * 3 nokta arasındaki açıyı (midPoint merkezli) derece cinsinden hesaplar.
     */
    fun getAngle(firstPoint: PoseLandmark, midPoint: PoseLandmark, lastPoint: PoseLandmark): Double {
        val p1 = firstPoint.position
        val p2 = midPoint.position
        val p3 = lastPoint.position

        val angleRad = atan2(p3.y - p2.y, p3.x - p2.x) - atan2(p1.y - p2.y, p1.x - p2.x)
        var angleDeg = Math.toDegrees(abs(angleRad.toDouble()))
        if (angleDeg > 180) {
            angleDeg = 360.0 - angleDeg
        }
        return angleDeg
    }

    /**
     * 2 nokta arasındaki Öklid mesafesini piksel cinsinden hesaplar.
     */
    fun getDistance(p1: PoseLandmark, p2: PoseLandmark): Float {
        val dx = p1.position.x - p2.position.x
        val dy = p1.position.y - p2.position.y
        return sqrt(dx * dx + dy * dy)
    }

    /**
     * Verilen noktanın kamerada yeterli güvenilirlikte görünüp görünmediğini kontrol eder.
     */
    fun isVisible(landmark: PoseLandmark?, minLikelihood: Float = 0.5f): Boolean {
        return landmark != null && landmark.inFrameLikelihood >= minLikelihood
    }
}