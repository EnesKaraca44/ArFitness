package com.example.arfitness.analysis

import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark

/**
 * Barfiks: sinyal = dirsek açısı.
 * Tekrar = asılı, kollar açık (>150°) → yukarı çekiş (<90°) → asılı.
 */
class PullupAnalyzer : RepExerciseAnalyzer() {

    override val counter = RepCounter(restThreshold = 150.0, peakThreshold = 90.0, minRepDurationMs = 700)

    override val readyMessage = "Bara asıl, kolların tam açık olsun"
    override val goMessage = "Göğsünü bara doğru çek"
    override val returnMessage = "Yavaşça in, kollarını tam aç"
    override val partialMessage = "Daha yukarı çek"

    override fun read(pose: Pose): Reading {
        val left = arrayOf(
            pose.lm(PoseLandmark.LEFT_SHOULDER), pose.lm(PoseLandmark.LEFT_ELBOW), pose.lm(PoseLandmark.LEFT_WRIST)
        )
        val right = arrayOf(
            pose.lm(PoseLandmark.RIGHT_SHOULDER), pose.lm(PoseLandmark.RIGHT_ELBOW), pose.lm(PoseLandmark.RIGHT_WRIST)
        )
        val side = pickSide("pullup", left, right)
        if (side == null) {
            clearSmoothing(KEY)
            return Reading.unavailable("Kolların ve başın görünmüyor, kameradan uzaklaş")
        }
        val (shoulder, elbow, wrist) = if (side == Side.LEFT) left else right
        val elbowAngle = smooth(KEY, AnalysisUtils.getAngle(shoulder!!, elbow!!, wrist!!))

        // Tepe noktada çene barı (bilek hizasını) geçmeli; görüntüde y aşağı doğru artar
        val nose = pose.lm(PoseLandmark.NOSE)
        val warning = if (elbowAngle < 90 && nose != null && isVisible(nose) &&
            nose.position.y > wrist!!.position.y
        ) {
            "Çeneni barın üstüne çıkar!"
        } else null

        return Reading.of(elbowAngle, warning)
    }

    private companion object {
        const val KEY = "pullupElbow"
    }
}
