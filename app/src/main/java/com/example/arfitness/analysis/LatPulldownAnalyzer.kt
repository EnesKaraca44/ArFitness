package com.example.arfitness.analysis

import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark

/**
 * Lat Pulldown: sinyal = dirsek açısı.
 * Tekrar = kollar yukarıda açık (>150°) → göğse çekiş (<90°) → kollar açık.
 */
class LatPulldownAnalyzer : RepExerciseAnalyzer() {

    override val counter = RepCounter(restThreshold = 150.0, peakThreshold = 90.0, minRepDurationMs = 700)

    override val readyMessage = "Barı kolların yukarıda düz şekilde tut"
    override val goMessage = "Barı göğsüne doğru çek"
    override val returnMessage = "Kontrollü bırak, kollarını aç"
    override val partialMessage = "Barı göğsüne kadar çek"

    override fun read(pose: Pose): Reading {
        val left = arrayOf(
            pose.lm(PoseLandmark.LEFT_SHOULDER), pose.lm(PoseLandmark.LEFT_ELBOW), pose.lm(PoseLandmark.LEFT_WRIST)
        )
        val right = arrayOf(
            pose.lm(PoseLandmark.RIGHT_SHOULDER), pose.lm(PoseLandmark.RIGHT_ELBOW), pose.lm(PoseLandmark.RIGHT_WRIST)
        )
        val side = pickSide("latpulldown", left, right)
        if (side == null) {
            clearSmoothing(KEY)
            return Reading.unavailable("Kolların görünmüyor, kameradan uzaklaş")
        }
        val (shoulder, elbow, wrist) = if (side == Side.LEFT) left else right
        return Reading.of(smooth(KEY, AnalysisUtils.getAngle(shoulder!!, elbow!!, wrist!!)))
    }

    private companion object {
        const val KEY = "latElbow"
    }
}
