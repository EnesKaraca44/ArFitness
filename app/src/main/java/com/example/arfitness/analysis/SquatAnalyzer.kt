package com.example.arfitness.analysis

import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark

/**
 * Squat hareketini analiz etmekten sorumlu sınıf.
 */
class SquatAnalyzer : ExerciseAnalyzer() {
    private var state: ExerciseState = ExerciseState.NEUTRAL
    private var repCount = 0

    // DTW için Profesyonel squatın zaman/açı grafiği (Ayakta -> Çömel -> Kalk)
    private val idealSquatTemplate = listOf(
        170.0, 160.0, 140.0, 120.0, 100.0, 85.0, 85.0, 100.0, 120.0, 140.0, 160.0, 170.0
    )

    override fun analyze(pose: Pose): Feedback {
        val leftHip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP)
        val leftKnee = pose.getPoseLandmark(PoseLandmark.LEFT_KNEE)
        val leftAnkle = pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE)

        val isLeftVisible = isVisible(leftHip, leftKnee, leftAnkle)
        val rightHip = pose.getPoseLandmark(PoseLandmark.RIGHT_HIP)
        val rightKnee = pose.getPoseLandmark(PoseLandmark.RIGHT_KNEE)
        val rightAnkle = pose.getPoseLandmark(PoseLandmark.RIGHT_ANKLE)

        val isRightVisible = isVisible(rightHip, rightKnee, rightAnkle)

        if (!isLeftVisible && !isRightVisible) {
            return Feedback(ExerciseState.UNKNOWN, "Bacakların net görünmüyor (Uzaklaş!).", false, pose)
        }

        val leftKneeRaw = if (isLeftVisible) AnalysisUtils.getAngle(leftHip!!, leftKnee!!, leftAnkle!!) else 180.0
        val rightKneeRaw = if (isRightVisible) AnalysisUtils.getAngle(rightHip!!, rightKnee!!, rightAnkle!!) else 180.0

        val leftKneeAngle = getSmoothedAngle("leftKnee", leftKneeRaw)
        val rightKneeAngle = getSmoothedAngle("rightKnee", rightKneeRaw)

        val averageKneeAngle = when {
            isLeftVisible && isRightVisible -> (leftKneeAngle + rightKneeAngle) / 2.0
            isLeftVisible -> leftKneeAngle
            else -> rightKneeAngle
        }

        var repCounted = false
        var feedbackMessage = "Pozisyonunu koru"

        // Yeni Merkezcil DTW Makine Öğrenmesi (Sayım işlemini kusursuz ve pürüzsüz yapar)
        if (matchPatternDTW("squat", averageKneeAngle, idealSquatTemplate, threshold = 20.0, bufferSize = 25, cooldownMs = 1500)) {
            repCount++
            repCounted = true
            feedbackMessage = "MÜKEMMEL FORM! $repCount. tekrar"
        } else {
            // Anlık form uyarıları (Kullanıcı bilgilendirmesi için)
            if (averageKneeAngle < 110) {
                state = ExerciseState.DOWN
                feedbackMessage = "Derinlik çok iyi, şimdi kalk!"
            } else if (averageKneeAngle > 160) {
                state = ExerciseState.UP
                feedbackMessage = "Harika kalkış!"
            }
        }

        return Feedback(
            state = state,
            message = feedbackMessage,
            isRepCounted = repCounted,
            pose = pose
        )
    }
}

