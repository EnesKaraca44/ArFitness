package com.example.arfitness.analysis

import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark

/**
 * Squat: sinyal = diz açısı (görünen bacakların ortalaması).
 * Tekrar = dik (>160°) → çömelme (<110°) → dik.
 */
class SquatAnalyzer : RepExerciseAnalyzer() {

    override val counter = RepCounter(restThreshold = 160.0, peakThreshold = 110.0, minRepDurationMs = 700)

    override val readyMessage = "Dik dur, tüm bacakların kamerada görünsün"
    override val goMessage = "Kalçanı geriye iterek çömel"
    override val returnMessage = "Derinlik iyi, şimdi kalk!"
    override val partialMessage = "Biraz daha derine in"

    override fun read(pose: Pose): Reading {
        val lHip = pose.lm(PoseLandmark.LEFT_HIP)
        val lKnee = pose.lm(PoseLandmark.LEFT_KNEE)
        val lAnkle = pose.lm(PoseLandmark.LEFT_ANKLE)
        val rHip = pose.lm(PoseLandmark.RIGHT_HIP)
        val rKnee = pose.lm(PoseLandmark.RIGHT_KNEE)
        val rAnkle = pose.lm(PoseLandmark.RIGHT_ANKLE)

        val leftOk = isVisible(lHip, lKnee, lAnkle)
        val rightOk = isVisible(rHip, rKnee, rAnkle)

        // Görünmeyen bacağın eski değeri, tekrar göründüğünde ortalamayı bozmasın
        if (!leftOk) clearSmoothing(LEFT_KEY)
        if (!rightOk) clearSmoothing(RIGHT_KEY)

        if (!leftOk && !rightOk) {
            return Reading.unavailable("Bacakların görünmüyor, kameradan biraz uzaklaş")
        }

        val angles = listOfNotNull(
            if (leftOk) smooth(LEFT_KEY, AnalysisUtils.getAngle(lHip!!, lKnee!!, lAnkle!!)) else null,
            if (rightOk) smooth(RIGHT_KEY, AnalysisUtils.getAngle(rHip!!, rKnee!!, rAnkle!!)) else null,
        )
        return Reading.of(angles.average())
    }

    private companion object {
        const val LEFT_KEY = "squatLeftKnee"
        const val RIGHT_KEY = "squatRightKnee"
    }
}
