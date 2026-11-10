package com.example.arfitness.analysis

import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark

/**
 * Bench Press (yandan çekim): sinyal = kameraya yakın kolun dirsek açısı.
 * Tekrar = kollar kilitli (>150°) → göğse iniş (<95°) → kollar kilitli.
 * Kullanıcı yatmıyorsa (gövde yatay değilse) sayım yapılmaz.
 */
class BenchPressAnalyzer : RepExerciseAnalyzer() {

    override val counter = RepCounter(restThreshold = 150.0, peakThreshold = 95.0, minRepDurationMs = 600)

    override val readyMessage = "Ağırlığı kolların düz şekilde yukarıda tut"
    override val goMessage = "Göğsüne doğru kontrollü indir"
    override val returnMessage = "Şimdi yukarı it!"
    override val partialMessage = "Göğsüne kadar indir"

    override fun read(pose: Pose): Reading {
        val left = arrayOf(
            pose.lm(PoseLandmark.LEFT_SHOULDER), pose.lm(PoseLandmark.LEFT_ELBOW), pose.lm(PoseLandmark.LEFT_WRIST)
        )
        val right = arrayOf(
            pose.lm(PoseLandmark.RIGHT_SHOULDER), pose.lm(PoseLandmark.RIGHT_ELBOW), pose.lm(PoseLandmark.RIGHT_WRIST)
        )
        val side = pickSide("bench", left, right)
        if (side == null) {
            clearSmoothing(KEY)
            return Reading.unavailable("Kolların görünmüyor, telefonu yanına koy")
        }
        val (shoulder, elbow, wrist) = if (side == Side.LEFT) left else right

        // Gövde yatay değilse (ayakta ağırlık ayarlıyor, bench'e oturuyor) sayaç dondurulur
        val hip = pose.lm(if (side == Side.LEFT) PoseLandmark.LEFT_HIP else PoseLandmark.RIGHT_HIP)
        if (hip != null && isVisible(hip) && AnalysisUtils.inclinationDeg(shoulder!!, hip) > MAX_TORSO_INCLINATION) {
            clearSmoothing(KEY)
            return Reading.unavailable("Bench'e sırt üstü uzan (telefon yanında olsun)")
        }

        return Reading.of(smooth(KEY, AnalysisUtils.getAngle(shoulder!!, elbow!!, wrist!!)))
    }

    private companion object {
        const val KEY = "benchElbow"
        /** Omuz–kalça çizgisinin yatayla yapabileceği en büyük açı (incline bench için pay bırakıldı). */
        const val MAX_TORSO_INCLINATION = 45.0
    }
}
