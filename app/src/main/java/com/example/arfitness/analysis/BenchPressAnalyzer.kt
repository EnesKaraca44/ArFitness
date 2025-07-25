package com.example.arfitness.analysis

import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark
import kotlin.math.abs
import kotlin.math.atan2

class BenchPressAnalyzer : ExerciseAnalyzer {

    override val requiredLandmarks = listOf(
        PoseLandmark.RIGHT_SHOULDER, PoseLandmark.LEFT_SHOULDER,
        PoseLandmark.RIGHT_ELBOW, PoseLandmark.LEFT_ELBOW,
        PoseLandmark.RIGHT_WRIST, PoseLandmark.LEFT_WRIST
    )

    // Bench Press için durumlar: Kollar yukarıda, kilitli (UP) ve göğse inmiş (DOWN)
    private var state: ExerciseState = ExerciseState.UP
    private var repCount = 0

    override fun analyze(pose: Pose): Feedback {
        // Gerekli eklem noktaları
        val leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER)
        val leftElbow = pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW)
        val leftWrist = pose.getPoseLandmark(PoseLandmark.LEFT_WRIST)
        val rightShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER)
        val rightElbow = pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW)
        val rightWrist = pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST)

        var feedbackMessage: String? = "Harika Form!"
        var repCounted = false

        if (leftShoulder != null && leftElbow != null && leftWrist != null &&
            rightShoulder != null && rightElbow != null && rightWrist != null) {

            // Dirsek açılarını hesapla
            val leftElbowAngle = getAngle(leftShoulder, leftElbow, leftWrist)
            val rightElbowAngle = getAngle(rightShoulder, rightElbow, rightWrist)

            // --- FORM KONTROLÜ ---
            // KURAL 1: İniş derinliği yeterli mi? (Sadece aşağıdayken kontrol et)
            // Bileklerin dikey pozisyonu, dirseklerin altına inmeli.
            if (state == ExerciseState.DOWN) {
                if (leftWrist.position.y < leftElbow.position.y || rightWrist.position.y < rightElbow.position.y) {
                    feedbackMessage = "Barı/Dumbbell'ları göğsüne daha çok yaklaştır!"
                }
            }
            // KURAL 2: Kollar tepede tam kilitlendi mi?
            else if (state == ExerciseState.UP) {
                if (leftElbowAngle < 160 || rightElbowAngle < 160) {
                    feedbackMessage = "Kollarını tepede tam kilitle!"
                }
            }

            // --- Tekrar Sayma Mantığı ---
            // Kollar aşağıda, göğse yakın (DOWN durumu)
            if (leftElbowAngle < 95 && rightElbowAngle < 95) {
                if (state == ExerciseState.UP) {
                    state = ExerciseState.DOWN
                }
            }
            // Kollar yukarıda, kilitli (UP durumu)
            else if (leftElbowAngle > 160 && rightElbowAngle > 160) {
                if (state == ExerciseState.DOWN) {
                    state = ExerciseState.UP
                    repCounted = true
                    // Form hatası yoksa pozitif mesaj ver
                    if (feedbackMessage == "Harika Form!") {
                        repCount++
                        feedbackMessage = "$repCount. tekrar tamamlandı!"
                    }
                }
            }
        } else {
            feedbackMessage = "Vücudunun üst kısmı tam görünmüyor."
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