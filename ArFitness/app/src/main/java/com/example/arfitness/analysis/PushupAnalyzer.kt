package com.example.arfitness.analysis

import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark
import kotlin.math.abs
import kotlin.math.atan2

class PushupAnalyzer : ExerciseAnalyzer {

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

            // --- Form Kontrolü ---
            if (bodyAngle < 150) { // Eğer vücut 150 dereceden fazla bükülürse (kalça düşerse)
                feedbackMessage = "Kalçanı düşürme, vücudunu düz tut!"
            } else {
                feedbackMessage = "Formun harika!"
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