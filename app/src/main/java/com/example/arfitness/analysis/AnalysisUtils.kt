package com.example.arfitness.analysis

import com.google.mlkit.vision.pose.PoseLandmark
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.sqrt

/**
 * Analiz için kullanılacak genel yardımcı fonksiyonları içerir.
 */
object AnalysisUtils {

    /**
     * 3 nokta arasındaki açıyı 3 BOYUTLU (X, Y, Z) vektörler kullanarak derece cinsinden hesaplar.
     */
    fun getAngle(firstPoint: PoseLandmark, midPoint: PoseLandmark, lastPoint: PoseLandmark): Double {
        val p1 = firstPoint.position3D
        val p2 = midPoint.position3D
        val p3 = lastPoint.position3D

        // Vektör 1: midPoint -> firstPoint
        val v1 = floatArrayOf(p1.x - p2.x, p1.y - p2.y, p1.z - p2.z)
        // Vektör 2: midPoint -> lastPoint
        val v2 = floatArrayOf(p3.x - p2.x, p3.y - p2.y, p3.z - p2.z)

        // Nokta Çarpımı (Dot Product)
        val dotProduct = (v1[0] * v2[0]) + (v1[1] * v2[1]) + (v1[2] * v2[2])

        // Vektörlerin Büyüklükleri (Magnitudes)
        val mag1 = sqrt((v1[0] * v1[0]) + (v1[1] * v1[1]) + (v1[2] * v1[2]))
        val mag2 = sqrt((v2[0] * v2[0]) + (v2[1] * v2[1]) + (v2[2] * v2[2]))

        if (mag1 * mag2 == 0f) return 0.0

        val cosine = dotProduct / (mag1 * mag2)
        // Cosine değeri -1.0 ile 1.0 arasında sınırlandırılır (Float hassasiyeti hatalarından kaçınmak için)
        val angleRad = acos(cosine.coerceIn(-1.0f, 1.0f))
        
        return Math.toDegrees(angleRad.toDouble())
    }

    /**
     * 2 nokta arasındaki gerçek 3D (Z ekseni dahil) mesafeyi hesaplar.
     */
    fun getDistance(p1: PoseLandmark, p2: PoseLandmark): Float {
        val dx = p1.position3D.x - p2.position3D.x
        val dy = p1.position3D.y - p2.position3D.y
        val dz = p1.position3D.z - p2.position3D.z
        return sqrt((dx * dx) + (dy * dy) + (dz * dz))
    }

    /**
     * Verilen noktanın kamerada yeterli güvenilirlikte (likelihood) görünüp görünmediğini kontrol eder.
     */
    fun isVisible(landmark: PoseLandmark?, minLikelihood: Float = 0.6f): Boolean {
        return landmark != null && landmark.inFrameLikelihood >= minLikelihood
    }
}