package com.example.arfitness.analysis

import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark

/**
 * Lateral Raise ve Front Raise'in ortak sayım mantığı (önden çekim).
 *
 * Sinyal = bileğin omuzun ne kadar altında olduğu, gövde boyuna (omuz–kalça) bölünerek:
 *   ~1.0 → kol aşağıda sarkıyor, ~0 → bilek omuz hizasında, < 0 → omuzun üstünde.
 * Gövde boyuna bölmek, eşiklerin kişinin kameraya uzaklığından ve çözünürlükten
 * bağımsız olmasını sağlar (eski kod sabit 20/50 piksel kullanıyordu).
 *
 * Kollar ayrı sayaçlarla izlenir ([PairedRepCounter]): iki kol birlikte → 1 tekrar,
 * sırayla → kol başına 1 tekrar. Tekrar (kol başına) = aşağıda (>0.75) → omuz hizası (<0.25) → aşağı.
 */
abstract class RaiseAnalyzer(private val key: String) : RepExerciseAnalyzer() {

    override val counter = PairedRepCounter(
        left = RepCounter(restThreshold = 0.75, peakThreshold = 0.25, minRepDurationMs = 600),
        right = RepCounter(restThreshold = 0.75, peakThreshold = 0.25, minRepDurationMs = 600),
    )

    override val readyMessage = "Kollarını aşağıda sal"
    override val returnMessage = "Kontrollü şekilde indir"
    override val partialMessage = "Omuz hizasına kadar kaldır"

    override fun read(pose: Pose): Reading {
        val left = readArm(pose, "${key}L", PoseLandmark.LEFT_SHOULDER, PoseLandmark.LEFT_WRIST, PoseLandmark.LEFT_HIP)
        val right = readArm(pose, "${key}R", PoseLandmark.RIGHT_SHOULDER, PoseLandmark.RIGHT_WRIST, PoseLandmark.RIGHT_HIP)
        if (left == null && right == null) {
            return Reading.unavailable("Kolların ve gövden görünmüyor, kameradan uzaklaş")
        }
        val tooHigh = (left != null && left < TOO_HIGH) || (right != null && right < TOO_HIGH)
        val warning = if (tooHigh) "Kollarını omuz hizasından yukarı kaldırma" else null
        return Reading.pair(left, right, warning)
    }

    private fun readArm(pose: Pose, smoothKey: String, s: Int, w: Int, h: Int): Double? {
        val shoulder = pose.lm(s)
        val wrist = pose.lm(w)
        val hip = pose.lm(h)
        if (!isVisible(shoulder, wrist, hip)) {
            clearSmoothing(smoothKey)
            return null
        }
        val torso = AnalysisUtils.distance2D(shoulder!!, hip!!)
        if (torso < 1f) return null
        val raw = (wrist!!.position.y - shoulder.position.y) / torso
        return smooth(smoothKey, raw.toDouble())
    }

    private companion object {
        /** Bilek omuzun bu kadar (gövde boyu oranı) üstüne çıkarsa uyarı ver. */
        const val TOO_HIGH = -0.35
    }
}
