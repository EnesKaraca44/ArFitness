package com.example.arfitness.analysis

import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark
import kotlin.math.abs
import kotlin.math.atan2

class FrontRaiseAnalyzer : ExerciseAnalyzer {

    // --- YENİ EKLENEN KISIM ---
    override val requiredLandmarks = listOf(
        PoseLandmark.LEFT_SHOULDER, PoseLandmark.RIGHT_SHOULDER,
        PoseLandmark.LEFT_ELBOW, PoseLandmark.RIGHT_ELBOW,
        PoseLandmark.LEFT_WRIST, PoseLandmark.RIGHT_WRIST,
        PoseLandmark.LEFT_HIP, PoseLandmark.RIGHT_HIP
    )

    private var state: ExerciseState = ExerciseState.DOWN
    private var repCount = 0

    override fun analyze(pose: Pose): Feedback {
        // Gerekli eklem noktaları
        val leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER)
        val rightShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER)
        val leftElbow = pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW)
        val rightElbow = pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW)
        val leftWrist = pose.getPoseLandmark(PoseLandmark.LEFT_WRIST)
        val rightWrist = pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST)
        val leftHip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP)

        var feedbackMessage: String? = "Harika Form!"
        var repCounted = false

        if (leftShoulder != null && rightShoulder != null && leftElbow != null && rightElbow != null &&
            leftWrist != null && rightWrist != null && leftHip != null) {

            // Pozisyon ve açıları alalım
            val leftWristY = leftWrist.position.y
            val rightWristY = rightWrist.position.y
            val shoulderY = (leftShoulder.position.y + rightShoulder.position.y) / 2

            // Omuz-kalça-dirsek açısı sırtın duruşu hakkında bir fikir verir
            val backLeanAngle = getAngle(leftElbow, leftShoulder, leftHip)

            // --- HAREKET DURUMUNU BELİRLEME ---
            // UP durumu: Bilekler omuz hizasına gelmeli
            val areHandsUp = leftWristY <= shoulderY + 20 && rightWristY <= shoulderY + 20

            // DOWN durumu: Bilekler kalça hizasının altına inmeli
            val areHandsDown = leftWristY > leftHip.position.y && rightWristY > leftHip.position.y


            // --- DURUM GÜNCELLEME VE TEKRAR SAYMA ---
            if (areHandsUp) {
                if (state == ExerciseState.DOWN) {
                    state = ExerciseState.UP
                }
            } else if (areHandsDown) {
                if (state == ExerciseState.UP) {
                    state = ExerciseState.DOWN
                    repCounted = true
                }
            }

            // --- FORM KONTROLÜ (Öncelikli) ---
            // KURAL 1: Vücudu geriye yaslayarak momentum kullanılıyor mu?
            // Omuz-kalça-dirsek açısı 90 dereceye yakın olmalı. Çok artarsa, geriye yaslanma var demektir.
            if (backLeanAngle > 110) {
                feedbackMessage = "Momentum kullanma, sırtını geriye yaslama!"
            }
            // KURAL 2: Kollar omuz seviyesinden çok yukarı kalkıyor mu?
            else if (leftWristY < shoulderY - 50 || rightWristY < shoulderY - 50) {
                feedbackMessage = "Kollarını omuz hizasından yukarı kaldırma!"
            }


            // --- SON KARAR VE MESAJ ---
            if (repCounted && feedbackMessage == "Harika Form!") {
                repCount++
                feedbackMessage = "$repCount. tekrar tamam!"
            }
        } else {
            feedbackMessage = "Vücudun tam olarak görünmüyor."
        }

        return Feedback(state, feedbackMessage, repCounted, pose)
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