package com.example.arfitness.analysis

import android.util.Log
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark
import kotlin.math.abs
import kotlin.math.atan2

class PlankAnalyzer : ExerciseAnalyzer() {

    override val requiredLandmarks = listOf(
        PoseLandmark.LEFT_SHOULDER, PoseLandmark.RIGHT_SHOULDER,
        PoseLandmark.LEFT_ELBOW, PoseLandmark.RIGHT_ELBOW,
        PoseLandmark.LEFT_HIP, PoseLandmark.RIGHT_HIP,
        PoseLandmark.LEFT_ANKLE, PoseLandmark.RIGHT_ANKLE
    )


    // Plank, durağan bir egzersiz olduğu için durum ve tekrar sayacı kullanmıyoruz.
    override fun analyze(pose: Pose): Feedback {
        // Gerekli eklem noktaları
        val leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER)
        val leftElbow = pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW) // Dirsek pozisyonu için eklendi
        val leftHip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP)
        val leftAnkle = pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE)

        var feedbackMessage: String? = "Pozisyonu Koru, Harika!" // Varsayılan mesaj pozitif

        if (leftShoulder != null && leftElbow != null && leftHip != null && leftAnkle != null) {

            // Vücut düzlüğünü kontrol etmek için omuz-kalça-ayak bileği açısı
            val bodyAngle = getAngle(leftShoulder, leftHip, leftAnkle)
            val shoulderElbowDistanceX = abs(leftShoulder.position.x - leftElbow.position.x)

            Log.d(
                "Plank_Debug",
                "Vücut Açısı: ${bodyAngle.toInt()}, Omuz-Dirsek Farkı: ${shoulderElbowDistanceX.toInt()}"
            )
            // --- GELİŞTİRİLMİŞ FORM KONTROLÜ ---
            // KURAL 1: Kalça çok mu YÜKSEKTE? (Açı çok daralır)
            if (bodyAngle < 160) {
                feedbackMessage = "Kalçanı biraz indir, vücudun düz olmalı!"
            }
            // KURAL 2: Kalça çok mu DÜŞÜK? (Açı 180'i çok geçer, sırt çöker)
            // Not: getAngle 180'den büyük değerleri de 180'e yakın döndürür, bu yüzden pozisyon kontrolü daha iyi.
            // Kalçanın dikey pozisyonu, omuz ve ayak bileğini birleştiren hayali bir çizginin üzerinde mi?
            else if (leftHip.position.y > (leftShoulder.position.y + leftAnkle.position.y) / 2 + 30) { // 30 piksel tolerans
                feedbackMessage = "Kalçanı düşürme, sırtını sık!"
            }
            // KURAL 3: Omuzlar dirseklerin üzerinde mi? (Forearm Plank için)
            else if (abs(leftShoulder.position.x - leftElbow.position.x) > 50) { // Yatayda çok farklılarsa
                feedbackMessage = "Omuzlarını dirseklerinle aynı hizaya getir."
            }


            // Plank'ta tekrar sayma olmadığı için 'isRepCounted' hep false olacak.
            // Durum olarak da NEUTRAL kullanabiliriz, çünkü sabit bir duruş.
            return Feedback(ExerciseState.NEUTRAL, feedbackMessage, false, pose)

        } else {
            return Feedback(ExerciseState.UNKNOWN, "Vücudun tam görünmüyor.", false, pose)
        }
    }

    private fun getAngle(first: PoseLandmark, mid: PoseLandmark, last: PoseLandmark): Double {
        val angleRad = atan2(last.position.y - mid.position.y, last.position.x - mid.position.x) -
                atan2(first.position.y - mid.position.y, first.position.x - mid.position.x)
        var angleDeg = Math.toDegrees(abs(angleRad.toDouble()))
        if (angleDeg > 180) {
            angleDeg = 360 - angleDeg
        }
        return angleDeg
    }
}

