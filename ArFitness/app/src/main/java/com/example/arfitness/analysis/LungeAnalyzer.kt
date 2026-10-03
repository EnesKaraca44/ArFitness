package com.example.arfitness.analysis

import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark

class LungeAnalyzer : ExerciseAnalyzer {

    private var state: ExerciseState = ExerciseState.UP
    private var repCount = 0

    override fun analyze(pose: Pose): Feedback {
        val leftHip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP)
        val leftKnee = pose.getPoseLandmark(PoseLandmark.LEFT_KNEE)
        val leftAnkle = pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE)

        val rightHip = pose.getPoseLandmark(PoseLandmark.RIGHT_HIP)
        val rightKnee = pose.getPoseLandmark(PoseLandmark.RIGHT_KNEE)
        val rightAnkle = pose.getPoseLandmark(PoseLandmark.RIGHT_ANKLE)

        val isLeftVisible = leftHip != null && leftKnee != null && leftAnkle != null
        val isRightVisible = rightHip != null && rightKnee != null && rightAnkle != null

        if (!isLeftVisible || !isRightVisible) {
            return Feedback(state, "Bacaklarının tamamı görünmüyor.", false, pose)
        }

        val leftKneeAngle = AnalysisUtils.getAngle(leftHip!!, leftKnee!!, leftAnkle!!)
        val rightKneeAngle = AnalysisUtils.getAngle(rightHip!!, rightKnee!!, rightAnkle!!)

        var feedbackMessage = "Pozisyon al!"
        var repCounted = false

        // Lunge pozisyonu: En az bir diz 110 derecenin altına inmiş olmalı
        val isLungePosition = (leftKneeAngle < 110 && rightKneeAngle < 135) ||
                (rightKneeAngle < 110 && leftKneeAngle < 135)

        // Ayakta durma pozisyonu: Her iki diz de düz
        val isStandingPosition = leftKneeAngle > 155 && rightKneeAngle > 155

        if (isLungePosition) {
            if (state == ExerciseState.UP) {
                state = ExerciseState.DOWN
            }
            feedbackMessage = "Harika derinlik, şimdi yukarı kalk!"
        } else if (isStandingPosition) {
            if (state == ExerciseState.DOWN) {
                state = ExerciseState.UP
                repCount++
                repCounted = true
                feedbackMessage = "$repCount. tekrar tamamlandı!"
            } else {
                feedbackMessage = "Öne doğru adım at ve çömel"
            }
        }

        return Feedback(state, feedbackMessage, repCounted, pose)
    }
}