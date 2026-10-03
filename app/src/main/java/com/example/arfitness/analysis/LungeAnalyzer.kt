package com.example.arfitness.analysis

import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark

import kotlin.math.min

class LungeAnalyzer : ExerciseAnalyzer() {

    private var state: ExerciseState = ExerciseState.UP
    private var repCount = 0

    // İdeal lunge (adım atıp çömelme) aktif diz grafiği
    private val idealLungeTemplate = listOf(
        170.0, 150.0, 120.0, 95.0, 80.0, 80.0, 95.0, 120.0, 150.0, 170.0
    )

    override fun analyze(pose: Pose): Feedback {
        val leftHip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP)
        val leftKnee = pose.getPoseLandmark(PoseLandmark.LEFT_KNEE)
        val leftAnkle = pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE)

        val rightHip = pose.getPoseLandmark(PoseLandmark.RIGHT_HIP)
        val rightKnee = pose.getPoseLandmark(PoseLandmark.RIGHT_KNEE)
        val rightAnkle = pose.getPoseLandmark(PoseLandmark.RIGHT_ANKLE)

        val isLeftVisible = isVisible(leftHip, leftKnee, leftAnkle)
        val isRightVisible = isVisible(rightHip, rightKnee, rightAnkle)

        if (!isLeftVisible || !isRightVisible) {
            return Feedback(state, "Bacaklarının tamamı net görünmüyor.", false, pose)
        }

        val rawLeftKneeAngle = AnalysisUtils.getAngle(leftHip!!, leftKnee!!, leftAnkle!!)
        val rawRightKneeAngle = AnalysisUtils.getAngle(rightHip!!, rightKnee!!, rightAnkle!!)

        val leftKneeAngle = getSmoothedAngle("lungeLeft", rawLeftKneeAngle)
        val rightKneeAngle = getSmoothedAngle("lungeRight", rawRightKneeAngle)

        // Lunge yaparken her zaman bir diz daha çok bükülür (Aktif diz)
        val activeKneeAngle = min(leftKneeAngle, rightKneeAngle)

        var feedbackMessage = "Pozisyon al!"
        var repCounted = false

        // DTW Makine Öğrenmesi ile Lunge Sayımı
        if (matchPatternDTW("lunge", activeKneeAngle, idealLungeTemplate, threshold = 22.0, bufferSize = 25, cooldownMs = 1500)) {
            repCount++
            repCounted = true
            feedbackMessage = "MÜKEMMEL FORM! $repCount. tekrar"
        } else {
            // Görsel / Anlık rehberlik
            if (activeKneeAngle < 100) {
                state = ExerciseState.DOWN
                feedbackMessage = "Harika derinlik, şimdi yukarı kalk!"
            } else if (activeKneeAngle > 155) {
                state = ExerciseState.UP
                feedbackMessage = "Öne doğru adım at ve çömel"
            }
        }

        return Feedback(state, feedbackMessage, repCounted, pose)
    }
}
