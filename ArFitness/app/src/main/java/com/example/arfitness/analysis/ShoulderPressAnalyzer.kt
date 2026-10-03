package com.example.arfitness.analysis

import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark

class ShoulderPressAnalyzer : ExerciseAnalyzer {

    private var state: ExerciseState = ExerciseState.DOWN
    private var repCount = 0

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
            return Feedback(state, "Kolların tam olarak görünmüyor.", false, pose)
        }

        val rightElbowAngle = if (isRightVisible) AnalysisUtils.getAngle(rightShoulder!!, rightElbow!!, rightWrist!!) else 0.0
        val leftElbowAngle = if (isLeftVisible) AnalysisUtils.getAngle(leftShoulder!!, leftElbow!!, leftWrist!!) else 0.0

        val avgElbowAngle = when {
            isRightVisible && isLeftVisible -> (rightElbowAngle + leftElbowAngle) / 2.0
            isRightVisible -> rightElbowAngle
            else -> leftElbowAngle
        }

        // Kolların omuz üstünde olup olmadığını kontrol et (Y koordinatı küçükse yukarıdadır)
        val areHandsUp = when {
            isRightVisible && isLeftVisible -> (rightWrist!!.position.y < rightShoulder!!.position.y) && (leftWrist!!.position.y < leftShoulder!!.position.y)
            isRightVisible -> rightWrist!!.position.y < rightShoulder!!.position.y
            else -> leftWrist!!.position.y < leftShoulder!!.position.y
        }

        var feedbackMessage = "Kontrollü ve yavaş ol!"
        var repCounted = false

        // Kollar tamamen yukarıda (UP durumu)
        if (avgElbowAngle > 145 && areHandsUp) {
            if (state == ExerciseState.DOWN) {
                state = ExerciseState.UP
            }
            feedbackMessage = "Tepede sıkıştır!"
        }
        // Kollar omuz hizasında aşağıda (DOWN durumu)
        else if (avgElbowAngle < 105) {
            if (state == ExerciseState.UP) {
                state = ExerciseState.DOWN
                repCount++
                repCounted = true
                feedbackMessage = "Harika! $repCount. tekrar!"
            } else {
                feedbackMessage = "Başın üzerine doğru it!"
            }
        }

        return Feedback(state, feedbackMessage, repCounted, pose)
    }
}