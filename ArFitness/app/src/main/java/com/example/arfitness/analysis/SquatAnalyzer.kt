package com.example.arfitness.analysis

import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark

/**
 * Squat hareketini analiz etmekten sorumlu sınıf.
 */
class SquatAnalyzer : ExerciseAnalyzer {

    private var state: ExerciseState = ExerciseState.NEUTRAL
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

        if (!isLeftVisible && !isRightVisible) {
            return Feedback(ExerciseState.UNKNOWN, "Bacakların tam olarak görünmüyor.", false, pose)
        }

        val leftKneeAngle = if (isLeftVisible) AnalysisUtils.getAngle(leftHip!!, leftKnee!!, leftAnkle!!) else 180.0
        val rightKneeAngle = if (isRightVisible) AnalysisUtils.getAngle(rightHip!!, rightKnee!!, rightAnkle!!) else 180.0

        val averageKneeAngle = when {
            isLeftVisible && isRightVisible -> (leftKneeAngle + rightKneeAngle) / 2.0
            isLeftVisible -> leftKneeAngle
            else -> rightKneeAngle
        }

        var repCounted = false
        var feedbackMessage = "Pozisyonunu koru"

        if (averageKneeAngle < 105) {
            if (state == ExerciseState.NEUTRAL || state == ExerciseState.UP) {
                state = ExerciseState.DOWN
            }
            feedbackMessage = "Harika derinlik, şimdi yukarı it!"
        } else if (averageKneeAngle > 155) {
            if (state == ExerciseState.DOWN) {
                state = ExerciseState.UP
                repCount++
                repCounted = true
                feedbackMessage = "Tebrikler! $repCount. tekrar"
            } else {
                state = ExerciseState.NEUTRAL
                feedbackMessage = "Squat için çömelmeye başla"
            }
        } else if (state == ExerciseState.DOWN) {
            feedbackMessage = "Tam yukarı kalk!"
        }

        return Feedback(
            state = state,
            message = feedbackMessage,
            isRepCounted = repCounted,
            pose = pose
        )
    }
}
