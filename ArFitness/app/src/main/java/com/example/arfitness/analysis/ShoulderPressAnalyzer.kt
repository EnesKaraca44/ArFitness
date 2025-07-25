package com.example.arfitness.analysis

import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark
import kotlin.math.abs
import kotlin.math.atan2

class ShoulderPressAnalyzer : ExerciseAnalyzer {

    // Shoulder Press için durumlar: Kollar aşağıda (DOWN) ve yukarıda (UP)
    private var state: ExerciseState = ExerciseState.DOWN
    private var repCount = 0

    override fun analyze(pose: Pose): Feedback {
        // Gerekli eklem noktaları
        val rightShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER)
        val rightElbow = pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW)
        val rightWrist = pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST)

        val leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER)
        val leftElbow = pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW)
        val leftWrist = pose.getPoseLandmark(PoseLandmark.LEFT_WRIST)

        var feedbackMessage: String? = "Omuz hizasında başla!"
        var repCounted = false

        // Her iki kolun da görünür olduğundan emin olalım
        if (rightShoulder != null && rightElbow != null && rightWrist != null &&
            leftShoulder != null && leftElbow != null && leftWrist != null) {

            // Pozisyonları alıyoruz (Y koordinatı ne kadar küçükse, o kadar yukarıdadır)
            val rightWristY = rightWrist.position.y
            val rightShoulderY = rightShoulder.position.y
            val leftWristY = leftWrist.position.y
            val leftShoulderY = leftShoulder.position.y

            // --- Form Kontrolü ---
            // Kolların ne kadar yukarı kalktığını kontrol edelim
            val rightElbowAngle = getAngle(rightShoulder, rightElbow, rightWrist)
            if (state == ExerciseState.UP && rightElbowAngle < 150) {
                feedbackMessage = "Kollarını tepede tam kilitle!"
            } else {
                feedbackMessage = "Kontrollü ve yavaş ol!"
            }

            // --- Tekrar Sayma Mantığı (Dikey Pozisyona Göre) ---
            // Kollar yukarıda (UP durumu): Bilekler, omuzların belirgin şekilde üzerinde olmalı
            val areHandsUp = rightWristY < rightShoulderY && leftWristY < leftShoulderY

            // Kollar aşağıda (DOWN durumu): Bilekler, omuz hizasında veya biraz altında olmalı
            // Dirseklerin de omuzdan çok aşağıda olmaması gerekir.
            val areHandsDown = rightWristY >= rightShoulderY && leftWristY >= leftShoulderY &&
                    rightElbow.position.y > rightShoulderY - 30 // Dirseklerin çok düşmesini engelle

            if (areHandsUp) {
                if (state == ExerciseState.DOWN) {
                    state = ExerciseState.UP
                }
            } else if (areHandsDown) {
                if (state == ExerciseState.UP) {
                    state = ExerciseState.DOWN
                    repCount++
                    repCounted = true
                    feedbackMessage = "Harika! $repCount. tekrar!"
                }
            }

        } else {
            feedbackMessage = "Kolların tam olarak görünmüyor."
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