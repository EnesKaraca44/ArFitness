package com.example.arfitness.analysis



import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark
import kotlin.math.abs
import kotlin.math.atan2

/**
 * Analiz için kullanılacak genel yardımcı fonksiyonları içerir.
 */
object AnalysisUtils {

    //ç PoseLandmark nesnesi (ML Kit'ten alınan vücut noktaları).

    fun getAngle(firstPoint: PoseLandmark, midPoint: PoseLandmark, lastPoint: PoseLandmark): Double {
        // Noktaların pozisyonları null değilse devam et
        val p1 = firstPoint.position
        val p2 = midPoint.position
        val p3 = lastPoint.position

        var result = Math.toDegrees(
            (atan2(p3.y - p2.y, p3.x - p2.x) - atan2(p1.y - p2.y, p1.x - p2.x)).toDouble()
        )
        result = abs(result) // Açı her zaman pozitif olmalı
        if (result > 180) {
            result = 360.0 - result // 180 dereceden büyük olamaz
        }
        return result
    }
}