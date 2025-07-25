package com.example.arfitness.analysis

import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark
import kotlin.math.abs
import kotlin.math.atan2

class CablePushdownAnalyzer : ExerciseAnalyzer {

    override val requiredLandmarks = listOf(
        PoseLandmark.RIGHT_SHOULDER,
        PoseLandmark.RIGHT_ELBOW,
        PoseLandmark.RIGHT_WRIST,
        PoseLandmark.RIGHT_HIP
    )

    // Cable Pushdown için durumlar: Kollar bükülü (UP) ve aşağıda kilitli (DOWN)
    private var state: ExerciseState = ExerciseState.UP
    private var repCount = 0

    override fun analyze(pose: Pose): Feedback {
        // Gerekli eklem noktaları (sağ kolu takip ediyoruz)
        val shoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER)
        val elbow = pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW)
        val wrist = pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST)

        var feedbackMessage: String? = "Harika Form!"
        var repCounted = false

        if (shoulder != null && elbow != null && wrist != null) {

            // Dirsek açısını hesapla
            val elbowAngle = getAngle(shoulder, elbow, wrist)

            // --- FORM KONTROLÜ ---
            // KURAL 1: Dirsek vücuttan çok fazla ayrılıyor mu?
            // Dirseğin X pozisyonu, omuzun X pozisyonuna çok yakın olmalı.
            if (abs(elbow.position.x - shoulder.position.x) > 100) { // 100 piksel tolerans
                feedbackMessage = "Dirseğini vücuduna yakın tut!"
            }
            // KURAL 2: Hareketin en alt noktasında kol tam kilitlendi mi?
            else if (state == ExerciseState.DOWN && elbowAngle < 160) {
                feedbackMessage = "Aşağıda kolunu tam kilitle!"
            }
            // KURAL 3: Hareketin en üst noktasında yeterince yukarı salındı mı?
            else if (state == ExerciseState.UP && elbowAngle > 90) {
                feedbackMessage = "Daha yukarı sal, triceps'i hisset!"
            }

            // --- Tekrar Sayma Mantığı ---
            // Kollar aşağıda, kilitli (DOWN durumu)
            if (elbowAngle > 160) { // Kol neredeyse düz
                if (state == ExerciseState.UP) {
                    state = ExerciseState.DOWN
                }
            }
            // Kollar yukarıda, bükülü (UP durumu)
            else if (elbowAngle < 90) { // Dirsek 90 dereceden daha dar
                if (state == ExerciseState.DOWN) {
                    state = ExerciseState.UP
                    repCounted = true
                    if (feedbackMessage == "Harika Form!") {
                        repCount++
                        feedbackMessage = "$repCount. tekrar tamam!"
                    }
                }
            }
        } else {
            feedbackMessage = "Kolun tam olarak görünmüyor."
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