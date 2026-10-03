package com.example.arfitness.analysis

import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark
import kotlin.math.abs

class BicepCurlAnalyzer : ExerciseAnalyzer {

    private var state: ExerciseState = ExerciseState.DOWN
    private var repCount = 0
    private var initialElbowY: Float? = null

    override fun analyze(pose: Pose): Feedback {
        val rightShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER)
        val rightElbow = pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW)
        val rightWrist = pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST)

        val leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER)
        val leftElbow = pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW)
        val leftWrist = pose.getPoseLandmark(PoseLandmark.LEFT_WRIST)

        val isRightVisible = rightShoulder != null && rightElbow != null && rightWrist != null
        val isLeftVisible = leftShoulder != null && leftElbow != null && leftWrist != null

        if (!isRightVisible && !isLeftVisible) {
            return Feedback(state, "Kolun tam olarak görünmüyor.", false, pose)
        }

        val shoulder = if (isRightVisible) rightShoulder!! else leftShoulder!!
        val elbow = if (isRightVisible) rightElbow!! else leftElbow!!
        val wrist = if (isRightVisible) rightWrist!! else leftWrist!!

        val elbowAngle = AnalysisUtils.getAngle(shoulder, elbow, wrist)

        var feedbackMessage = "Pozisyon al!"
        var repCounted = false

        // Form kontrolü: Dirsek stabil mi?
        if (initialElbowY == null) {
            initialElbowY = elbow.position.y
        }

        if (abs(elbow.position.y - (initialElbowY ?: elbow.position.y)) > 75) {
            feedbackMessage = "Dirseğini sabit tut, omuzdan savurma!"
        } else {
            feedbackMessage = "Harika form!"
        }

        // Tekrar sayma mantığı
        if (elbowAngle < 65) {
            if (state == ExerciseState.DOWN) {
                state = ExerciseState.UP
            }
        } else if (elbowAngle > 145) {
            if (state == ExerciseState.UP) {
                state = ExerciseState.DOWN
                repCount++
                repCounted = true
                feedbackMessage = "$repCount. tekrar tamamlandı!"
            }
        }

        return Feedback(state, feedbackMessage, repCounted, pose)
    }
}