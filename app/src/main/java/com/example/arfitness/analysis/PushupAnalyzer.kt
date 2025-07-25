package com.example.arfitness.analysis

import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark
import kotlin.math.abs
import kotlin.math.atan2

class PushupAnalyzer : ExerciseAnalyzer {
    override val requiredLandmarks = listOf(
        PoseLandmark.LEFT_SHOULDER, PoseLandmark.RIGHT_SHOULDER,
        PoseLandmark.LEFT_ELBOW, PoseLandmark.RIGHT_ELBOW,
        PoseLandmark.LEFT_WRIST, PoseLandmark.RIGHT_WRIST,
        PoseLandmark.LEFT_HIP, PoseLandmark.RIGHT_HIP,
        PoseLandmark.LEFT_ANKLE, PoseLandmark.RIGHT_ANKLE
    )

    private var state: ExerciseState = ExerciseState.UP // Şınavda başlangıç durumu 'UP'
    private var repCount = 0

    override fun analyze(pose: Pose): Feedback {
        // Şınav için gerekli eklem noktaları
        val leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER)
        val leftElbow = pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW)
        val leftWrist = pose.getPoseLandmark(PoseLandmark.LEFT_WRIST)
        val leftHip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP)
        val leftAnkle = pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE)

        var feedbackMessage: String? = "Pozisyon al!"
        var repCounted = false

        if (leftShoulder != null && leftElbow != null && leftWrist != null && leftHip != null && leftAnkle != null) {

            // Dirsek açısını hesapla
            val elbowAngle = getAngle(leftShoulder, leftElbow, leftWrist)

            // Vücut düzlüğünü kontrol etmek için omuz-kalça-ayak bileği açısını hesapla
            val bodyAngle = getAngle(leftShoulder, leftHip, leftAnkle)

            // --- YENİ: Dirsek Pozisyonu Kontrolü ---
            //Bu, dirseklerin ne kadar dışa açıldığını anlamamıza yardımcı olur.
            val elbowFlareRatio = abs(leftElbow.position.x - leftShoulder.position.x) /
                    abs(leftShoulder.position.y - leftHip.position.y) // Gövde uzunluğuna oranla





            // --- FORM KONTROLÜ ---
            // KURAL 1: Kalça düştü mü?
            if (bodyAngle < 155) { // Eşiği biraz daha hassas hale getirelim
                feedbackMessage = "Kalçanı düşürme, vücudunu düz tut!"
            }
            // KURAL 2: Dirsekler çok fazla dışa mı açılıyor?
            else if (elbowFlareRatio > 0.8) { // Bu oran deneme yanılma ile ayarlanabilir
                feedbackMessage = "Dirseklerini vücuduna biraz daha yakın tut!"
            }
            // KURAL 3: Yeterince derine inildi mi? (Sadece aşağıdayken kontrol et)
            else if (state == ExerciseState.DOWN) {
                // Omuzun dikey pozisyonu, dirseğin dikey pozisyonundan daha aşağıda olmalı.
                if (leftShoulder.position.y > leftElbow.position.y) {
                    feedbackMessage = "Göğsünü yere daha çok yaklaştır!"
                }
            }



            // --- Tekrar Sayma Mantığı ---
            // Eğer dirsekler yeterince büküldüyse (DOWN durumu)
            if (elbowAngle < 90) { // 90 dereceden küçükse aşağıda kabul edelim
                if (state == ExerciseState.UP) {
                    state = ExerciseState.DOWN
                }
            }
            // Eğer dirsekler tekrar düzleştiyse (UP durumu)
            else if (elbowAngle > 160) { // 160 dereceden büyükse yukarıda kabul edelim
                if (state == ExerciseState.DOWN) {
                    state = ExerciseState.UP
                    repCount++
                    repCounted = true
                    feedbackMessage = "İşte bu! $repCount. tekrar"
                }
            }
        } else {
            feedbackMessage = "Vücudun tam görünmüyor."
        }

        return Feedback(state, feedbackMessage, repCounted, pose)
    }

    // Bu yardımcı fonksiyonu AnalysisUtils gibi ortak bir yere taşıyabiliriz.
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