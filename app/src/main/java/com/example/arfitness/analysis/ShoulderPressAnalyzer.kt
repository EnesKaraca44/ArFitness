package com.example.arfitness.analysis

import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark

/**
 * Shoulder Press: sinyal = her kolun dirsek açısı, kollar ayrı sayaçlarla izlenir
 * ([PairedRepCounter]): iki kol birlikte → 1 tekrar, sırayla → kol başına 1 tekrar.
 * Tekrar (kol başına) = dumbbell omuz hizasında (<115°) → baş üstünde kol düz (>150°) → omuz hizası.
 *
 * Kol yanda sarkıyorsa (bilek omuzun belirgin şekilde altında) o kolun sayacı dondurulur. Aksi halde
 * kolu aşağı sarkıtmak (dirsek ~170°) "tepe noktası" sanılıp sahte tekrar üretirdi.
 */
class ShoulderPressAnalyzer : RepExerciseAnalyzer() {

    override val counter = PairedRepCounter(
        left = RepCounter(restThreshold = 115.0, peakThreshold = 150.0, minRepDurationMs = 600),
        right = RepCounter(restThreshold = 115.0, peakThreshold = 150.0, minRepDurationMs = 600),
    )

    override val readyMessage = "Dumbbell'ları omuz hizasında tut"
    override val goMessage = "Başının üzerine doğru it"
    override val returnMessage = "Kontrollü şekilde omuz hizasına indir"
    override val partialMessage = "Kollarını tam yukarı uzat"

    private sealed interface ArmReading {
        object NotVisible : ArmReading
        object Hanging : ArmReading
        class Angle(val value: Double) : ArmReading
    }

    override fun read(pose: Pose): Reading {
        val left = readArm(pose, LEFT_KEY, PoseLandmark.LEFT_SHOULDER, PoseLandmark.LEFT_ELBOW, PoseLandmark.LEFT_WRIST)
        val right = readArm(pose, RIGHT_KEY, PoseLandmark.RIGHT_SHOULDER, PoseLandmark.RIGHT_ELBOW, PoseLandmark.RIGHT_WRIST)

        val leftAngle = (left as? ArmReading.Angle)?.value
        val rightAngle = (right as? ArmReading.Angle)?.value
        if (leftAngle == null && rightAngle == null) {
            val anyHanging = left is ArmReading.Hanging || right is ArmReading.Hanging
            return Reading.unavailable(
                if (anyHanging) "Dumbbell'ları omuz hizasına kaldır"
                else "Kolların görünmüyor, kameradan biraz uzaklaş"
            )
        }
        return Reading.pair(leftAngle, rightAngle)
    }

    private fun readArm(pose: Pose, key: String, s: Int, e: Int, w: Int): ArmReading {
        val shoulder = pose.lm(s)
        val elbow = pose.lm(e)
        val wrist = pose.lm(w)
        if (!isVisible(shoulder, elbow, wrist)) {
            clearSmoothing(key)
            return ArmReading.NotVisible
        }
        // Görüntüde y aşağı doğru artar. Bilek omuzun, üst kol boyunun yarısından fazla
        // altındaysa kol yanda sarkıyordur (alt noktada bilek omuz hizasında ya da biraz üstündedir).
        val upperArm = AnalysisUtils.distance2D(shoulder!!, elbow!!)
        if (wrist!!.position.y - shoulder.position.y > 0.5f * upperArm) {
            clearSmoothing(key)
            return ArmReading.Hanging
        }
        return ArmReading.Angle(smooth(key, AnalysisUtils.getAngle(shoulder, elbow, wrist)))
    }

    private companion object {
        const val LEFT_KEY = "pressLeftElbow"
        const val RIGHT_KEY = "pressRightElbow"
    }
}
