package com.example.arfitness.analysis

import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark
import kotlin.math.abs

/**
 * Plank (yandan çekim): tekrar sayılmaz, doğru pozisyonda geçen süre ölçülür.
 *
 * Kalçanın omuz–ayak bileği çizgisinden sapması işaretli olarak ölçülür (vücut boyuna oranla):
 *  > 0 → kalça çizginin üstünde (çok yüksek), < 0 → altında (sarkıyor).
 * Eski kod işaretsiz açı kullandığı için kalça sarktığında da "kalçanı indir" diyordu.
 */
class PlankAnalyzer : ExerciseAnalyzer() {

    override val isTimed = true

    private var lastDeviation: Double? = null
    private var lastState = "NOT_READY"

    // Form puanı: plank pozisyonundayken form uyarısı olmayan karelerin oranı
    private var inPositionFrames = 0
    private var goodFormFrames = 0

    override val debugState: DebugState
        get() = DebugState(listOf(lastDeviation), lastState)

    override val sessionFormScore: Int?
        get() = if (inPositionFrames == 0) null else (100L * goodFormFrames / inPositionFrames).toInt()

    override fun analyze(pose: Pose, timestampMs: Long): Feedback {
        val feedback = evaluate(pose)
        lastState = when {
            lastDeviation == null -> "UNAVAILABLE"
            feedback.isHolding -> "HOLDING"
            else -> "FORM_BROKEN"
        }
        if (lastDeviation != null) {
            inPositionFrames++
            if (!feedback.isWarning) goodFormFrames++
        }
        return feedback
    }

    private fun evaluate(pose: Pose): Feedback {
        lastDeviation = null
        val left = arrayOf(
            pose.lm(PoseLandmark.LEFT_SHOULDER), pose.lm(PoseLandmark.LEFT_HIP), pose.lm(PoseLandmark.LEFT_ANKLE)
        )
        val right = arrayOf(
            pose.lm(PoseLandmark.RIGHT_SHOULDER), pose.lm(PoseLandmark.RIGHT_HIP), pose.lm(PoseLandmark.RIGHT_ANKLE)
        )
        val side = pickSide("plank", left, right)
        if (side == null) {
            clearSmoothing(KEY)
            return Feedback("Vücudun tam görünmüyor, telefonu yanına koyup uzaklaş", isWarning = true)
        }
        val (shoulder, hip, ankle) = if (side == Side.LEFT) left else right

        if (AnalysisUtils.inclinationDeg(shoulder!!, ankle!!) > MAX_BODY_INCLINATION) {
            clearSmoothing(KEY)
            return Feedback("Plank pozisyonuna geç (telefon yanında olsun)", isWarning = true)
        }

        val bodyLength = AnalysisUtils.distance2D(shoulder, ankle)
        if (bodyLength < 1f) return Feedback("Vücudun net görünmüyor", isWarning = true)

        // Omuz–ayak bileği çizgisinin kalçanın x konumundaki y değeri
        val dx = ankle.position.x - shoulder.position.x
        val t = if (abs(dx) < 1f) 0.5f else ((hip!!.position.x - shoulder.position.x) / dx).coerceIn(0f, 1f)
        val lineY = shoulder.position.y + t * (ankle.position.y - shoulder.position.y)
        // Görüntüde y aşağı doğru artar: kalça çizginin üstündeyse sapma pozitif
        val deviation = smooth(KEY, ((lineY - hip!!.position.y) / bodyLength).toDouble())
        lastDeviation = deviation

        val holding = abs(deviation) <= MAX_DEVIATION_FOR_TIMER
        val message = when {
            deviation > WARN_DEVIATION -> "Kalçanı biraz indir, vücudun düz olmalı"
            deviation < -WARN_DEVIATION -> "Kalçanı düşürme, karnını sık"
            else -> null
        }
        if (message != null) {
            return Feedback(message, isWarning = true, isHolding = holding)
        }

        val elbow = pose.lm(if (side == Side.LEFT) PoseLandmark.LEFT_ELBOW else PoseLandmark.RIGHT_ELBOW)
        if (elbow != null && isVisible(elbow) &&
            abs(shoulder.position.x - elbow.position.x) > 0.15f * bodyLength
        ) {
            return Feedback("Omuzlarını dirseklerinin üstüne getir", isWarning = true, isHolding = holding)
        }

        return Feedback("Harika, pozisyonu koru!", isHolding = holding)
    }

    private companion object {
        const val KEY = "plankHipDeviation"
        const val MAX_BODY_INCLINATION = 35.0
        /** Bu sapmanın üstünde uyarı gösterilir. */
        const val WARN_DEVIATION = 0.07
        /** Bu sapmanın üstünde süre durur (form çok bozuk). */
        const val MAX_DEVIATION_FOR_TIMER = 0.15
    }
}
