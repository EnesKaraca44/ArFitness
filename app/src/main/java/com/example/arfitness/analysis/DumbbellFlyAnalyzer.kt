package com.example.arfitness.analysis

import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark

/**
 * Dumbbell Fly: hem önden hem yandan çekimi destekler. Sinyal, iki çekimde de aynı ölçeğe
 * çevrilmiş bir "açıklık" değeridir: 0 = kollar kapalı (göğsün üstünde), 1 = kollar açık.
 * Tekrar = kapalı (≤0) → açık (≥1) → kapalı.
 *
 * - Önden çekim (ayakta cable/band fly): bilekler arası mesafe / omuz genişliği (3D).
 *     kapalı ≤ 1.3, açık ≥ 2.0
 * - Yandan çekim (talimattaki gibi bench'te sırt üstü): yakın kolun omuz–bilek mesafesi / gövde boyu (2D).
 *     Kollar göğsün üstünde birleşikken kol görüntü düzleminde tam boyunu gösterir (~1.0);
 *     yana açılınca kameraya doğru/kameradan uzağa gittiği için kısalır (~0.3).
 *     kapalı ≥ 0.8, açık ≤ 0.4
 *
 * Çekim yönü omuz genişliğinin gövde boyuna oranından anlaşılır: yandan bakınca iki omuz üst üste biner.
 */
class DumbbellFlyAnalyzer : RepExerciseAnalyzer() {

    override val counter = RepCounter(restThreshold = 0.0, peakThreshold = 1.0, minRepDurationMs = 700)

    override val readyMessage = "Dumbbell'ları göğsünün üzerinde birleştir"
    override val goMessage = "Kollarını yanlara doğru kontrollü aç"
    override val returnMessage = "Göğsünü sıkarak kapat!"
    override val partialMessage = "Kollarını biraz daha aç"

    private var isSideView = false

    override fun read(pose: Pose): Reading {
        val lShoulder = pose.lm(PoseLandmark.LEFT_SHOULDER)
        val rShoulder = pose.lm(PoseLandmark.RIGHT_SHOULDER)
        val lHip = pose.lm(PoseLandmark.LEFT_HIP)
        val rHip = pose.lm(PoseLandmark.RIGHT_HIP)

        if (!isVisible(lShoulder, rShoulder, lHip, rHip)) {
            clearSmoothing(KEY)
            return Reading.unavailable("Gövden görünmüyor, kameradan biraz uzaklaş")
        }
        val torso = (AnalysisUtils.distance2D(lShoulder!!, lHip!!) + AnalysisUtils.distance2D(rShoulder!!, rHip!!)) / 2f
        if (torso < 1f) return Reading.unavailable("Gövden net görünmüyor")

        // Çekim yönü (histerezisli: iki eşik arasında önceki karar korunur)
        val shoulderSpan = AnalysisUtils.distance2D(lShoulder, rShoulder) / torso
        if (shoulderSpan < SIDE_VIEW_BELOW) isSideView = true
        else if (shoulderSpan > FRONT_VIEW_ABOVE) isSideView = false

        return if (isSideView) readSide(pose, torso) else readFront(pose, lShoulder, rShoulder)
    }

    private fun readFront(pose: Pose, lShoulder: PoseLandmark, rShoulder: PoseLandmark): Reading {
        val lElbow = pose.lm(PoseLandmark.LEFT_ELBOW)
        val rElbow = pose.lm(PoseLandmark.RIGHT_ELBOW)
        val lWrist = pose.lm(PoseLandmark.LEFT_WRIST)
        val rWrist = pose.lm(PoseLandmark.RIGHT_WRIST)
        if (!isVisible(lElbow, rElbow, lWrist, rWrist)) {
            clearSmoothing(KEY)
            return Reading.unavailable("İki kolun da görünmeli, kameradan biraz uzaklaş")
        }
        val shoulderWidth = AnalysisUtils.getDistance(lShoulder, rShoulder)
        if (shoulderWidth < 1f) return Reading.unavailable("Omuzların net görünmüyor")

        val ratio = AnalysisUtils.getDistance(lWrist!!, rWrist!!) / shoulderWidth
        val opening = smooth(KEY, normalize(ratio.toDouble(), closed = FRONT_CLOSED, open = FRONT_OPEN))

        val bentTooMuch = AnalysisUtils.getAngle(lShoulder, lElbow!!, lWrist) < 120 ||
            AnalysisUtils.getAngle(rShoulder, rElbow!!, rWrist) < 120
        return Reading.of(opening, if (bentTooMuch) BENT_WARNING else null)
    }

    private fun readSide(pose: Pose, torso: Float): Reading {
        val left = arrayOf(
            pose.lm(PoseLandmark.LEFT_SHOULDER), pose.lm(PoseLandmark.LEFT_ELBOW), pose.lm(PoseLandmark.LEFT_WRIST)
        )
        val right = arrayOf(
            pose.lm(PoseLandmark.RIGHT_SHOULDER), pose.lm(PoseLandmark.RIGHT_ELBOW), pose.lm(PoseLandmark.RIGHT_WRIST)
        )
        val side = pickSide("fly", left, right)
        if (side == null) {
            clearSmoothing(KEY)
            return Reading.unavailable("Kolun görünmüyor, telefonu bench'in yanına koy")
        }
        val (shoulder, elbow, wrist) = if (side == Side.LEFT) left else right
        val reach = AnalysisUtils.distance2D(shoulder!!, wrist!!) / torso
        val opening = smooth(KEY, normalize(reach.toDouble(), closed = SIDE_CLOSED, open = SIDE_OPEN))

        val bentTooMuch = AnalysisUtils.getAngle(shoulder, elbow!!, wrist) < 120
        return Reading.of(opening, if (bentTooMuch) BENT_WARNING else null)
    }

    /** [closed] değerini 0'a, [open] değerini 1'e eşler (iki yönde de çalışır). */
    private fun normalize(value: Double, closed: Double, open: Double): Double = (value - closed) / (open - closed)

    private companion object {
        const val KEY = "flyOpening"
        const val BENT_WARNING = "Dirseklerini çok kırma, hafif bükük tut"

        const val FRONT_CLOSED = 1.3
        const val FRONT_OPEN = 2.0
        const val SIDE_CLOSED = 0.8
        const val SIDE_OPEN = 0.4

        /** Omuz genişliği / gövde boyu bunun altındaysa yandan çekim. */
        const val SIDE_VIEW_BELOW = 0.35
        /** Bunun üstündeyse önden çekim. */
        const val FRONT_VIEW_ABOVE = 0.5
    }
}
