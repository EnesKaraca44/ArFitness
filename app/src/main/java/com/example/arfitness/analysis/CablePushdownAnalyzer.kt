package com.example.arfitness.analysis

import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark

/**
 * Cable Pushdown: sinyal = dirsek açısı.
 * Tekrar = dirsek bükülü (<95°) → kollar düz (>155°) → dirsek bükülü.
 */
class CablePushdownAnalyzer : RepExerciseAnalyzer() {

    override val counter = RepCounter(restThreshold = 95.0, peakThreshold = 155.0, minRepDurationMs = 500)

    override val readyMessage = "Dirseklerin bükülü, eller göğüs hizasında başla"
    override val goMessage = "Kollarını tamamen düzelene kadar aşağı it"
    override val returnMessage = "Kontrollü şekilde yukarı bırak"
    override val partialMessage = "Kollarını sonuna kadar düzelt"

    override fun read(pose: Pose): Reading {
        val left = arrayOf(
            pose.lm(PoseLandmark.LEFT_SHOULDER), pose.lm(PoseLandmark.LEFT_ELBOW), pose.lm(PoseLandmark.LEFT_WRIST)
        )
        val right = arrayOf(
            pose.lm(PoseLandmark.RIGHT_SHOULDER), pose.lm(PoseLandmark.RIGHT_ELBOW), pose.lm(PoseLandmark.RIGHT_WRIST)
        )
        val side = pickSide("pushdown", left, right)
        if (side == null) {
            clearSmoothing(KEY)
            return Reading.unavailable("Kolun görünmüyor, kameradan biraz uzaklaş")
        }
        val (shoulder, elbow, wrist) = if (side == Side.LEFT) left else right
        val elbowAngle = smooth(KEY, AnalysisUtils.getAngle(shoulder!!, elbow!!, wrist!!))

        // Üst kol gövdeye paralel kalmalı: kalça–omuz–dirsek açısı küçük olmalı
        val hip = pose.lm(if (side == Side.LEFT) PoseLandmark.LEFT_HIP else PoseLandmark.RIGHT_HIP)
        val warning = if (hip != null && isVisible(hip) && AnalysisUtils.getAngle(hip, shoulder, elbow) > 40) {
            "Dirseğini gövdene sabitle"
        } else null

        return Reading.of(elbowAngle, warning)
    }

    private companion object {
        const val KEY = "pushdownElbow"
    }
}
