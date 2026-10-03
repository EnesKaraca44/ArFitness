package com.example.arfitness.analysis

import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark

class PushupAnalyzer : ExerciseAnalyzer {

    private var state: ExerciseState = ExerciseState.UP
    private var repCount = 0

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

        // Hangi taraf daha görünürse o tarafı seç
        val isLeftVisible = leftShoulder != null && leftElbow != null && leftWrist != null && leftHip != null && leftAnkle != null
        val isRightVisible = rightShoulder != null && rightElbow != null && rightWrist != null && rightHip != null && rightAnkle != null

        if (!isLeftVisible && !isRightVisible) {
            return Feedback(state, "Vücudun tam görünmüyor. Yan profil al!", false, pose)
        }

        val shoulder = if (isLeftVisible) leftShoulder!! else rightShoulder!!
        val elbow = if (isLeftVisible) leftElbow!! else rightElbow!!
        val wrist = if (isLeftVisible) leftWrist!! else rightWrist!!
        val hip = if (isLeftVisible) leftHip!! else rightHip!!
        val ankle = if (isLeftVisible) leftAnkle!! else rightAnkle!!

        // Açıları hesapla
        val elbowAngle = AnalysisUtils.getAngle(shoulder, elbow, wrist)
        val bodyAngle = AnalysisUtils.getAngle(shoulder, hip, ankle)

        var feedbackMessage: String
        var repCounted = false

        // Form kontrolü
        if (bodyAngle < 145) {
            feedbackMessage = "Kalçanı düşürme, vücudunu düz tut!"
        } else {
            feedbackMessage = "Formun harika!"
        }

        // Tekrar sayma mantığı
        if (elbowAngle < 95) {
            if (state == ExerciseState.UP) {
                state = ExerciseState.DOWN
            }
        } else if (elbowAngle > 155) {
            if (state == ExerciseState.DOWN) {
                state = ExerciseState.UP
                repCount++
                repCounted = true
                feedbackMessage = "İşte bu! $repCount. tekrar"
            }
        }

        return Feedback(state, feedbackMessage, repCounted, pose)
    }
}