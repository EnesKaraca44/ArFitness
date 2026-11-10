package com.example.arfitness.analysis

import com.google.mlkit.vision.pose.PoseLandmark
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.sqrt

/**
 * Analiz için kullanılacak genel yardımcı fonksiyonları içerir.
 */
object AnalysisUtils {

    /**
     * 3 nokta arasındaki açıyı 3 BOYUTLU (X, Y, Z) vektörler kullanarak derece cinsinden hesaplar.
     * Z ekseni sayesinde açı kameranın bakış yönünden daha az etkilenir (önden ya da yandan çekim).
     */
    fun getAngle(firstPoint: PoseLandmark, midPoint: PoseLandmark, lastPoint: PoseLandmark): Double {
        val p1 = firstPoint.position3D
        val p2 = midPoint.position3D
        val p3 = lastPoint.position3D

        val v1 = floatArrayOf(p1.x - p2.x, p1.y - p2.y, p1.z - p2.z)
        val v2 = floatArrayOf(p3.x - p2.x, p3.y - p2.y, p3.z - p2.z)

        val dotProduct = (v1[0] * v2[0]) + (v1[1] * v2[1]) + (v1[2] * v2[2])
        val mag1 = sqrt((v1[0] * v1[0]) + (v1[1] * v1[1]) + (v1[2] * v1[2]))
        val mag2 = sqrt((v2[0] * v2[0]) + (v2[1] * v2[1]) + (v2[2] * v2[2]))

        if (mag1 * mag2 == 0f) return 0.0

        val cosine = dotProduct / (mag1 * mag2)
        val angleRad = acos(cosine.coerceIn(-1.0f, 1.0f))
        return Math.toDegrees(angleRad.toDouble())
    }

    /** 2 nokta arasındaki 3D (Z dahil) mesafe. */
    fun getDistance(p1: PoseLandmark, p2: PoseLandmark): Float {
        val dx = p1.position3D.x - p2.position3D.x
        val dy = p1.position3D.y - p2.position3D.y
        val dz = p1.position3D.z - p2.position3D.z
        return sqrt((dx * dx) + (dy * dy) + (dz * dz))
    }

    /** 2 nokta arasındaki görüntü düzlemindeki (2D) mesafe. */
    fun distance2D(p1: PoseLandmark, p2: PoseLandmark): Float =
        hypot(p1.position.x - p2.position.x, p1.position.y - p2.position.y)

    /**
     * a → b doğrusunun yatayla yaptığı açı (2D, 0° = yatay, 90° = dikey).
     * Şınav/plank'ta vücudun yere paralel olup olmadığını anlamak için kullanılır.
     */
    fun inclinationDeg(a: PoseLandmark, b: PoseLandmark): Double {
        val dx = abs(b.position.x - a.position.x)
        val dy = abs(b.position.y - a.position.y)
        return Math.toDegrees(atan2(dy, dx).toDouble())
    }
}
