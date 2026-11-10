package com.example.arfitness.analysis

import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark

/**
 * Deadlift (yandan çekim): sinyal = kalça açısı (omuz–kalça–diz), kameraya yakın taraf.
 * Tekrar = dik (>160°) → öne eğilmiş (<120°) → dik.
 *
 * Not: Eski "sırtını yuvarlama" kontrolü (omuz–kalça–ayak bileği < 150°) normal kalça
 * menteşesinde her tekrarda tetikleniyordu; kaldırıldı. Sırt kontrolü Faz 2'de ele alınacak.
 */
class DeadliftAnalyzer : RepExerciseAnalyzer() {

    override val counter = RepCounter(restThreshold = 160.0, peakThreshold = 120.0, minRepDurationMs = 800)

    override val readyMessage = "Telefonu yanına koy ve dik dur"
    override val goMessage = "Kalçanı geriye iterek eğil"
    override val returnMessage = "Kalçanı öne iterek dikleş!"
    override val partialMessage = "Biraz daha eğil, bar dizlerinin altına insin"

    override fun read(pose: Pose): Reading {
        val left = arrayOf(
            pose.lm(PoseLandmark.LEFT_SHOULDER), pose.lm(PoseLandmark.LEFT_HIP), pose.lm(PoseLandmark.LEFT_KNEE)
        )
        val right = arrayOf(
            pose.lm(PoseLandmark.RIGHT_SHOULDER), pose.lm(PoseLandmark.RIGHT_HIP), pose.lm(PoseLandmark.RIGHT_KNEE)
        )
        val side = pickSide("deadlift", left, right)
        if (side == null) {
            clearSmoothing(KEY)
            return Reading.unavailable("Vücudun tam görünmüyor, telefonu yanına koyup uzaklaş")
        }
        val (shoulder, hip, knee) = if (side == Side.LEFT) left else right
        val ankle = pose.lm(if (side == Side.LEFT) PoseLandmark.LEFT_ANKLE else PoseLandmark.RIGHT_ANKLE)

        val hipAngle = smooth(KEY, AnalysisUtils.getAngle(shoulder!!, hip!!, knee!!))

        // Deadlift bir kalça hareketidir; diz çok bükülüyorsa squat'a dönüşmüştür
        val warning = if (ankle != null && isVisible(ankle) &&
            hipAngle < 130 && AnalysisUtils.getAngle(hip, knee, ankle) < 95
        ) {
            "Çömelme, hareketi kalçadan yap"
        } else null

        return Reading.of(hipAngle, warning)
    }

    private companion object {
        const val KEY = "deadliftHip"
    }
}
