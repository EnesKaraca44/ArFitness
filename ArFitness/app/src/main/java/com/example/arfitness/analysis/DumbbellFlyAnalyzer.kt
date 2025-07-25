package com.example.arfitness.analysis

import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.sqrt

class DumbbellFlyAnalyzer : ExerciseAnalyzer {

    // Dumbbell Fly için durumlar: Kollar açık (OPEN) ve kapalı (CLOSED)
    private var state: ExerciseState = ExerciseState.CLOSED
    private var repCount = 0

    override fun analyze(pose: Pose): Feedback {
        // Gerekli eklem noktaları
        val leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER)
        val rightShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER)
        val leftWrist = pose.getPoseLandmark(PoseLandmark.LEFT_WRIST)
        val rightWrist = pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST)
        val leftElbow = pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW)
        val rightElbow = pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW)

        var feedbackMessage: String? = "Yatar pozisyonda başla!"
        var repCounted = false

        if (leftShoulder != null && rightShoulder != null && leftWrist != null && rightWrist != null && leftElbow != null && rightElbow != null) {

            // İki bilek arasındaki mesafeyi hesapla
            val wristDistance = getDistance(leftWrist, rightWrist)

            // Omuzların genişliğini referans alalım
            val shoulderWidth = getDistance(leftShoulder, rightShoulder)

            // --- Form Kontrolü ---
            // Dirseklerin çok fazla bükülmemesi gerekir
            val leftElbowAngle = getAngle(leftShoulder, leftElbow, leftWrist)
            val rightElbowAngle = getAngle(rightShoulder, rightElbow, rightWrist)
            if (leftElbowAngle < 130 || rightElbowAngle < 130) {
                feedbackMessage = "Dirseklerini çok kırma, kollarını daha düz tut!"
            } else {
                feedbackMessage = "Göğsünü sıkarak kapat!"
            }

            // --- Tekrar Sayma Mantığı (Bileklerin Mesafesine Göre) ---
            // Kollar kapalı (CLOSED durumu): İki bilek arasındaki mesafe, omuz genişliğinden az olmalı
            val areHandsClosed = wristDistance < shoulderWidth

            // Kollar açık (OPEN durumu): İki bilek arasındaki mesafe, omuz genişliğinin en az iki katı olmalı
            val areHandsOpen = wristDistance > (shoulderWidth * 2)

            if (areHandsOpen) {
                if (state == ExerciseState.CLOSED) {
                    state = ExerciseState.OPEN
                }
            } else if (areHandsClosed) {
                if (state == ExerciseState.OPEN) {
                    state = ExerciseState.CLOSED
                    repCount++
                    repCounted = true
                    feedbackMessage = "$repCount. tekrar harika!"
                }
            }

        } else {
            feedbackMessage = "Vücudunun üst kısmı tam görünmüyor."
        }

        return Feedback(state, feedbackMessage, repCounted, pose)
    }

    // İki nokta arasındaki Öklid mesafesini hesaplayan yardımcı fonksiyon
    private fun getDistance(p1: PoseLandmark, p2: PoseLandmark): Float {
        val dx = p1.position.x - p2.position.x
        val dy = p1.position.y - p2.position.y
        return sqrt(dx * dx + dy * dy)
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