package com.example.arfitness.analysis

import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark

class PushupAnalyzer : ExerciseAnalyzer() {
    private var state: ExerciseState = ExerciseState.UP
    private var repCount = 0

    // Şınav (Push-up) için ideal dirsek açısı şablonu (Yukarıda kilitli -> Aşağı in -> Yukarı Çık)
    private val idealPushupTemplate = listOf(
        170.0, 150.0, 120.0, 90.0, 75.0, 90.0, 120.0, 150.0, 170.0
    )

    override fun analyze(pose: Pose): Feedback {
        val leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER)
        val leftElbow = pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW)
        val leftWrist = pose.getPoseLandmark(PoseLandmark.LEFT_WRIST)
        val leftHip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP)
        val leftAnkle = pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE)

        val rightShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER)
        val rightElbow = pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW)
        val rightWrist = pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST)
        val rightHip = pose.getPoseLandmark(PoseLandmark.RIGHT_HIP)
        val rightAnkle = pose.getPoseLandmark(PoseLandmark.RIGHT_ANKLE)

        val isLeftVisible = isVisible(leftShoulder, leftElbow, leftWrist, leftHip, leftAnkle)
        val isRightVisible = isVisible(rightShoulder, rightElbow, rightWrist, rightHip, rightAnkle)

        if (!isLeftVisible && !isRightVisible) {
            return Feedback(state, "Vücudun tam görünmüyor (Kameraya uzaklaş!)", false, pose)
        }

        val shoulder = if (isLeftVisible) leftShoulder!! else rightShoulder!!
        val elbow = if (isLeftVisible) leftElbow!! else rightElbow!!
        val wrist = if (isLeftVisible) leftWrist!! else rightWrist!!
        val hip = if (isLeftVisible) leftHip!! else rightHip!!
        val ankle = if (isLeftVisible) leftAnkle!! else rightAnkle!!

        // Açıları hesapla (3D Matematik + Low Pass Smoothing)
        val rawElbowAngle = AnalysisUtils.getAngle(shoulder, elbow, wrist)
        val rawBodyAngle = AnalysisUtils.getAngle(shoulder, hip, ankle)

        val elbowAngle = getSmoothedAngle("pushupElbow", rawElbowAngle)
        val bodyAngle = getSmoothedAngle("pushupBody", rawBodyAngle)

        var feedbackMessage = ""
        var repCounted = false

        // Form kontrolü: Şınavda sırt/kalça düz olmalı
        if (bodyAngle < 145) {
            feedbackMessage = "Kalçanı düşürme, düz tut!"
        }

        // DTW ile Şınav Sayımı
        if (matchPatternDTW("pushup", elbowAngle, idealPushupTemplate, threshold = 22.0, bufferSize = 25, cooldownMs = 1200)) {
            repCount++
            repCounted = true
            feedbackMessage = "MÜKEMMEL FORM! $repCount. tekrar"
        } else {
            // Sadece görsel anlık yönlendirme (Sayımı etkilemez)
            if (elbowAngle < 90) {
                state = ExerciseState.DOWN
            } else if (elbowAngle > 150) {
                state = ExerciseState.UP
            }
        }

        if (feedbackMessage.isEmpty()) {
            feedbackMessage = if (state == ExerciseState.DOWN) "Şimdi yukarı it!" else "Formun harika, inmeye başla!"
        }

        return Feedback(state, feedbackMessage, repCounted, pose)
    }
}
