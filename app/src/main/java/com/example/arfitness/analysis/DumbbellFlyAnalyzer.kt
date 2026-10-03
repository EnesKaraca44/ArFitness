package com.example.arfitness.analysis

import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark

class DumbbellFlyAnalyzer : ExerciseAnalyzer() {

    private var state: ExerciseState = ExerciseState.CLOSED
    private var repCount = 0

    override fun analyze(pose: Pose): Feedback {
        val leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER)
        val rightShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER)
        val leftWrist = pose.getPoseLandmark(PoseLandmark.LEFT_WRIST)
        val rightWrist = pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST)
        val leftElbow = pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW)
        val rightElbow = pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW)

        if (leftShoulder == null || rightShoulder == null ||
            leftWrist == null || rightWrist == null ||
            leftElbow == null || rightElbow == null) {
            return Feedback(state, "Üst vücudun tam görünmüyor.", false, pose)
        }

        val wristDistance = AnalysisUtils.getDistance(leftWrist, rightWrist)
        val shoulderWidth = AnalysisUtils.getDistance(leftShoulder, rightShoulder)

        val leftElbowAngle = AnalysisUtils.getAngle(leftShoulder, leftElbow, leftWrist)
        val rightElbowAngle = AnalysisUtils.getAngle(rightShoulder, rightElbow, rightWrist)

        var feedbackMessage = "Kontrollü aç ve kapat!"
        var repCounted = false

        // Form kontrolü
        if (leftElbowAngle < 120 || rightElbowAngle < 120) {
            feedbackMessage = "Dirseklerini çok kırma, hafif bükük tut!"
        }

        // Kollar kapalı: Bilekler omuz genişliğine yakın
        val areHandsClosed = wristDistance < (shoulderWidth * 1.2f)

        // Kollar açık: Bilekler omuz genişliğinin en az 2 katı açık
        val areHandsOpen = wristDistance > (shoulderWidth * 2.0f)

        if (areHandsOpen) {
            if (state == ExerciseState.CLOSED) {
                state = ExerciseState.OPEN
            }
            feedbackMessage = "Göğsünü sıkarak kapat!"
        } else if (areHandsClosed) {
            if (state == ExerciseState.OPEN) {
                state = ExerciseState.CLOSED
                repCount++
                repCounted = true
                feedbackMessage = "$repCount. tekrar harika!"
            }
        }

        return Feedback(state, feedbackMessage, repCounted, pose)
    }
}
