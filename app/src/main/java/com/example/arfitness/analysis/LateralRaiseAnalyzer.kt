package com.example.arfitness.analysis

import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark
import kotlin.math.abs
import kotlin.math.atan2

class LateralRaiseAnalyzer : ExerciseAnalyzer() {

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
        val rightHip = pose.getPoseLandmark(PoseLandmark.RIGHT_HIP)

        var feedbackMessage: String? = "Harika Form!"
        var repCounted = false

        if (leftShoulder != null && rightShoulder != null && leftElbow != null && rightElbow != null &&
            leftWrist != null && rightWrist != null && leftHip != null && rightHip != null) {

            // Y koordinatları (daha küçük Y = daha yukarıda)
            val leftWristY = leftWrist.position.y
            val rightWristY = rightWrist.position.y
            // Omuzların ortalama yüksekliğini referans alalım
            val shoulderY = (leftShoulder.position.y + rightShoulder.position.y) / 2

            // Dirsek açıları
            val leftElbowAngle = getAngle(leftShoulder, leftElbow, leftWrist)
            val rightElbowAngle = getAngle(rightShoulder, rightElbow, rightWrist)

            // --- HAREKET DURUMUNU BELİRLEME ---
            // UP durumu: Bilekler, omuz hizasına gelmiş veya çok az geçmiş olmalı.
            // (Y koordinatı, omuz Y'sinden küçük veya biraz büyük olmalı)
            val areHandsAtShoulderLevel = leftWristY <= shoulderY + 20 && rightWristY <= shoulderY + 20

            // DOWN durumu: Bilekler, kalça hizasında veya daha aşağıda olmalı.
            val areHandsDown = leftWristY > leftHip.position.y && rightWristY > rightHip.position.y


            // --- DURUM GÜNCELLEME VE TEKRAR SAYMA ---
            if (areHandsAtShoulderLevel) {
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
            // KURAL 1: Kollar omuz seviyesinden çok yukarı kalkıyor mu?
            if (leftWristY < shoulderY - 50 || rightWristY < shoulderY - 50) {
                feedbackMessage = "Kollarını omuz hizasından yukarı kaldırma!"
            }
            // KURAL 2: Dirsekler çok fazla bükülüyor mu?
            else if (leftElbowAngle < 140 || rightElbowAngle < 140) {
                feedbackMessage = "Dirseklerini çok bükme, kollarını daha düz tut."
            }


            // --- SON KARAR VE MESAJ ---
            if (repCounted) {
                repCount++
                feedbackMessage = if (feedbackMessage == "Harika Form!") {
                    "$repCount. tekrar tamam!"
                } else {
                    "$repCount. tekrar! ($feedbackMessage)"
                }
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
