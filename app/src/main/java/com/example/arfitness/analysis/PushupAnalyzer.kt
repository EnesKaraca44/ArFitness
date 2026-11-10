package com.example.arfitness.analysis

import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark

/**
 * Şınav (yandan çekim): sinyal = kameraya yakın kolun dirsek açısı.
 * Tekrar = kollar düz (>150°) → aşağı (<100°) → kollar düz.
 * Gövde yere paralel değilse (ayakta, dizüstü vs.) sayaç dondurulur; ayakta kol oynatmak tekrar saymaz.
 */
class PushupAnalyzer : RepExerciseAnalyzer() {

    override val counter = RepCounter(restThreshold = 150.0, peakThreshold = 100.0, minRepDurationMs = 500)

    override val readyMessage = "Şınav pozisyonunda kollarını düz tut"
    override val goMessage = "Göğsünü yere doğru indir"
    override val returnMessage = "Şimdi yukarı it!"
    override val partialMessage = "Biraz daha aşağı in"

    override fun read(pose: Pose): Reading {
        val left = arrayOf(
            pose.lm(PoseLandmark.LEFT_SHOULDER), pose.lm(PoseLandmark.LEFT_ELBOW), pose.lm(PoseLandmark.LEFT_WRIST),
            pose.lm(PoseLandmark.LEFT_HIP), pose.lm(PoseLandmark.LEFT_ANKLE)
        )
        val right = arrayOf(
            pose.lm(PoseLandmark.RIGHT_SHOULDER), pose.lm(PoseLandmark.RIGHT_ELBOW), pose.lm(PoseLandmark.RIGHT_WRIST),
            pose.lm(PoseLandmark.RIGHT_HIP), pose.lm(PoseLandmark.RIGHT_ANKLE)
        )
        val side = pickSide("pushup", left, right)
        if (side == null) {
            clearSmoothing(KEY)
            return Reading.unavailable("Vücudun tam görünmüyor, telefonu yanına koyup uzaklaş")
        }
        val (shoulder, elbow, wrist, hip, ankle) = if (side == Side.LEFT) left else right

        if (AnalysisUtils.inclinationDeg(shoulder!!, ankle!!) > MAX_BODY_INCLINATION) {
            clearSmoothing(KEY)
            return Reading.unavailable("Şınav pozisyonuna geç (telefon yanında olsun)")
        }

        val elbowAngle = smooth(KEY, AnalysisUtils.getAngle(shoulder, elbow!!, wrist!!))
        val bodyAngle = AnalysisUtils.getAngle(shoulder, hip!!, ankle)
        val warning = if (bodyAngle < 145) "Kalçanı düz tut, vücudun tek çizgi olsun" else null

        return Reading.of(elbowAngle, warning)
    }

    private companion object {
        const val KEY = "pushupElbow"
        /** Omuz–ayak bileği çizgisinin yatayla yapabileceği en büyük açı. */
        const val MAX_BODY_INCLINATION = 40.0
    }
}
