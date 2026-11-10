package com.example.arfitness.analysis

import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark

/**
 * Lunge: sinyal = daha çok bükülen dizin açısı (aktif diz).
 * Tekrar = dik (>155°) → alçalma (<110°) → dik. Her bacak değişimi ayrı tekrar sayılır.
 */
class LungeAnalyzer : RepExerciseAnalyzer() {

    override val counter = RepCounter(restThreshold = 155.0, peakThreshold = 110.0, minRepDurationMs = 700)

    override val readyMessage = "Dik dur, iki bacağın da kamerada görünsün"
    override val goMessage = "Öne adım at ve iki dizini bükerek alçal"
    override val returnMessage = "Harika derinlik, öndeki ayağınla itip kalk!"
    override val partialMessage = "Biraz daha alçal, diz 90°'ye yaklaşsın"

    override fun read(pose: Pose): Reading {
        val lHip = pose.lm(PoseLandmark.LEFT_HIP)
        val lKnee = pose.lm(PoseLandmark.LEFT_KNEE)
        val lAnkle = pose.lm(PoseLandmark.LEFT_ANKLE)
        val rHip = pose.lm(PoseLandmark.RIGHT_HIP)
        val rKnee = pose.lm(PoseLandmark.RIGHT_KNEE)
        val rAnkle = pose.lm(PoseLandmark.RIGHT_ANKLE)

        val leftOk = isVisible(lHip, lKnee, lAnkle)
        val rightOk = isVisible(rHip, rKnee, rAnkle)
        if (!leftOk) clearSmoothing(LEFT_KEY)
        if (!rightOk) clearSmoothing(RIGHT_KEY)

        if (!leftOk && !rightOk) {
            return Reading.unavailable("Bacakların görünmüyor, kameradan biraz uzaklaş")
        }

        val angles = listOfNotNull(
            if (leftOk) smooth(LEFT_KEY, AnalysisUtils.getAngle(lHip!!, lKnee!!, lAnkle!!)) else null,
            if (rightOk) smooth(RIGHT_KEY, AnalysisUtils.getAngle(rHip!!, rKnee!!, rAnkle!!)) else null,
        )
        return Reading.of(angles.min())
    }

    private companion object {
        const val LEFT_KEY = "lungeLeftKnee"
        const val RIGHT_KEY = "lungeRightKnee"
    }
}
