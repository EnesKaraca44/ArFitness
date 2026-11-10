package com.example.arfitness.analysis

import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark

/**
 * Bicep Curl ve Hammer Curl'ün ortak sayım mantığı.
 * Sinyal = her kolun dirsek açısı, kollar ayrı sayaçlarla izlenir ([PairedRepCounter]):
 * iki kol birlikte → 1 tekrar, sırayla (alternatif) → kol başına 1 tekrar, tek kol → o kol.
 * Tekrar (kol başına) = kol aşağıda düz (>145°) → yukarı (<70°) → aşağı.
 */
abstract class CurlAnalyzer(private val key: String) : RepExerciseAnalyzer() {

    override val counter = PairedRepCounter(
        left = RepCounter(restThreshold = 145.0, peakThreshold = 70.0, minRepDurationMs = 600),
        right = RepCounter(restThreshold = 145.0, peakThreshold = 70.0, minRepDurationMs = 600),
    )

    override val readyMessage = "Kollarını aşağıda düz sal"
    override val goMessage = "Dirseğini sabit tutarak yukarı kaldır"
    override val returnMessage = "Şimdi yavaşça indir"
    override val partialMessage = "Daha yukarı kaldır"

    private class Arm(val angle: Double, val elbowDrifting: Boolean)

    override fun read(pose: Pose): Reading {
        val left = readArm(pose, "${key}L", PoseLandmark.LEFT_SHOULDER, PoseLandmark.LEFT_ELBOW, PoseLandmark.LEFT_WRIST, PoseLandmark.LEFT_HIP)
        val right = readArm(pose, "${key}R", PoseLandmark.RIGHT_SHOULDER, PoseLandmark.RIGHT_ELBOW, PoseLandmark.RIGHT_WRIST, PoseLandmark.RIGHT_HIP)
        if (left == null && right == null) {
            return Reading.unavailable("Kolun görünmüyor, kameradan biraz uzaklaş")
        }
        val warning = if (left?.elbowDrifting == true || right?.elbowDrifting == true) {
            "Dirseğini sabit tut, omuzdan savurma"
        } else null
        return Reading.pair(left?.angle, right?.angle, warning)
    }

    private fun readArm(pose: Pose, smoothKey: String, s: Int, e: Int, w: Int, h: Int): Arm? {
        val shoulder = pose.lm(s)
        val elbow = pose.lm(e)
        val wrist = pose.lm(w)
        if (!isVisible(shoulder, elbow, wrist)) {
            clearSmoothing(smoothKey)
            return null
        }
        val angle = smooth(smoothKey, AnalysisUtils.getAngle(shoulder!!, elbow!!, wrist!!))

        // Dirsek gövdenin yanında sabit kalmalı: kalça–omuz–dirsek açısı büyürse omuzdan savuruyordur.
        // Yalnızca kol bükülüyken bakılır; kol aşağıdayken bu açı zaten küçüktür.
        val hip = pose.lm(h)
        val drifting = angle < 120 && hip != null && isVisible(hip) &&
            AnalysisUtils.getAngle(hip, shoulder, elbow) > 40
        return Arm(angle, drifting)
    }
}
