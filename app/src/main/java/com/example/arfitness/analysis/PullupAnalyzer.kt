package com.example.arfitness.analysis

import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark
import kotlin.math.abs
import kotlin.math.atan2

class PullupAnalyzer : ExerciseAnalyzer() {
    override val requiredLandmarks = listOf(
        PoseLandmark.RIGHT_SHOULDER, PoseLandmark.LEFT_SHOULDER,
        PoseLandmark.RIGHT_ELBOW, PoseLandmark.LEFT_ELBOW,
        PoseLandmark.RIGHT_WRIST, PoseLandmark.LEFT_WRIST,
        PoseLandmark.NOSE
    )

    private var state: ExerciseState = ExerciseState.DOWN
    private var repCount = 0

    // İdeal Barfiks Zaman Serisi Şablonu (Kollar açık -> Yukarı Çekiş -> İniş)
    private val idealPullupTemplate = listOf(
        170.0, 140.0, 100.0, 70.0, 50.0, 70.0, 100.0, 140.0, 170.0
    )

    override fun analyze(pose: Pose): Feedback {
        val leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER)
        val rightShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER)
        val leftElbow = pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW)
        val rightElbow = pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW)
        val leftWrist = pose.getPoseLandmark(PoseLandmark.LEFT_WRIST)
        val rightWrist = pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST)
        val nose = pose.getPoseLandmark(PoseLandmark.NOSE)

        val isLeftVisible = isVisible(leftShoulder, leftElbow, leftWrist)
        val isRightVisible = isVisible(rightShoulder, rightElbow, rightWrist)

        if (!isLeftVisible && !isRightVisible) {
            return Feedback(state, "Vücudunun üst kısmı net görünmüyor.", false, pose)
        }

        val shoulder = if (isLeftVisible) leftShoulder!! else rightShoulder!!
        val elbow = if (isLeftVisible) leftElbow!! else rightElbow!!
        val wrist = if (isLeftVisible) leftWrist!! else rightWrist!!

        // 3 Boyutlu Açı ve Smoothing (Titreme Önleyici)
        val rawElbowAngle = AnalysisUtils.getAngle(shoulder, elbow, wrist)
        val elbowAngle = getSmoothedAngle("pullupElbow", rawElbowAngle)

        var feedbackMessage = "Pozisyon al!"
        var repCounted = false

        // DTW ile Barfiks Sayımı
        if (matchPatternDTW("pullup", elbowAngle, idealPullupTemplate, threshold = 25.0, bufferSize = 25, cooldownMs = 1500)) {
            repCount++
            repCounted = true
            feedbackMessage = "MÜKEMMEL FORM! $repCount. tekrar"
        } else {
            // Görsel / Anlık rehberlik ve form kontrolü
            if (elbowAngle < 80) {
                state = ExerciseState.UP
                // Barfiks tepe noktasındayken çene bileği geçmeli (y koordinatı daha küçük olmalı)
                if (nose != null && nose.position3D.y > wrist.position3D.y) {
                    feedbackMessage = "Çeneni barın üstüne çıkar!"
                } else {
                    feedbackMessage = "Harika çekiş, yavaşça in!"
                }
            } else if (elbowAngle > 150) {
                state = ExerciseState.DOWN
                feedbackMessage = "Kolların tam açıldı, şimdi yukarı çek!"
            }
        }

        return Feedback(state, feedbackMessage, repCounted, pose)
    }
}

