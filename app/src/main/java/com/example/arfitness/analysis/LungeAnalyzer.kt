package com.example.arfitness.analysis

import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark
import kotlin.math.abs
import kotlin.math.atan2

class LungeAnalyzer : ExerciseAnalyzer {
    override val requiredLandmarks = listOf(
        PoseLandmark.LEFT_SHOULDER, PoseLandmark.RIGHT_SHOULDER,
        PoseLandmark.LEFT_HIP, PoseLandmark.RIGHT_HIP,
        PoseLandmark.LEFT_KNEE, PoseLandmark.RIGHT_KNEE,
        PoseLandmark.LEFT_ANKLE, PoseLandmark.RIGHT_ANKLE
    )

    private var state: ExerciseState = ExerciseState.UP
    private var repCount = 0

    override fun analyze(pose: Pose): Feedback {
        // Gerekli tüm eklem noktaları
        val leftHip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP)
        val leftKnee = pose.getPoseLandmark(PoseLandmark.LEFT_KNEE)
        val leftAnkle = pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE)
        val rightHip = pose.getPoseLandmark(PoseLandmark.RIGHT_HIP)
        val rightKnee = pose.getPoseLandmark(PoseLandmark.RIGHT_KNEE)
        val rightAnkle = pose.getPoseLandmark(PoseLandmark.RIGHT_ANKLE)
        val leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER)

        var feedbackMessage: String? = "Harika Form!"
        var repCounted = false

        if (leftHip != null && leftKnee != null && leftAnkle != null &&
            rightHip != null && rightKnee != null && rightAnkle != null && leftShoulder != null) {

            // Açıları hesapla
            val leftKneeAngle = getAngle(leftHip, leftKnee, leftAnkle)
            val rightKneeAngle = getAngle(rightHip, rightKnee, rightAnkle)
            val backAngle = getAngle(leftShoulder, leftHip, leftKnee)

            // Hareketin durumunu belirle
            val isDown = leftKneeAngle < 120 && rightKneeAngle < 140
            val isUp = leftKneeAngle > 160 && rightKneeAngle > 160

            // Durum güncelleme ve tekrar sayma
            if (isDown) {
                if (state == ExerciseState.UP) {
                    state = ExerciseState.DOWN
                }
            } else if (isUp) {
                if (state == ExerciseState.DOWN) {
                    state = ExerciseState.UP
                    repCounted = true
                }
            }

            // Form kontrolü (Sadece Lunge pozisyonundayken)
            if (state == ExerciseState.DOWN) {
                // --- DEĞİŞİKLİK BURADA: position3D.z kullanıyoruz ---
                // Hangi bacağın önde olduğunu bulalım (kameraya daha yakın olan)
                val (frontKnee, frontAnkle) = if (leftAnkle.position3D.z < rightAnkle.position3D.z) {
                    Pair(leftKnee, leftAnkle)
                } else {
                    Pair(rightKnee, rightAnkle)
                }

                // KURAL 1: Ön diz, ayak parmağını geçiyor mu?
                if (abs(frontKnee.position.x - frontAnkle.position.x) > (frontAnkle.position.y - frontKnee.position.y) * 0.2) {
                    feedbackMessage = "Ön dizin ayak parmağını geçmesin!"
                }
                // KURAL 2: Gövde dik mi?
                else if (backAngle < 160) {
                    feedbackMessage = "Gövdeni dik tut!"
                }
            }

            // Son karar ve mesaj
            if (repCounted && feedbackMessage == "Harika Form!") {
                repCount++
                feedbackMessage = "$repCount. tekrar tamamlandı!"
            }

        } else {
            feedbackMessage = "Vücudun tam görünmüyor."
        }

        return Feedback(state, feedbackMessage, repCounted, pose)
    }
    // Bu yardımcı fonksiyonu AnalysisUtils gibi ortak bir dosyaya taşıyabilirsin.
    private fun getAngle(first: PoseLandmark, mid: PoseLandmark, last: PoseLandmark): Double {
        val angleRad = atan2(last.position.y - mid.position.y, last.position.x - mid.position.x) -
                atan2(first.position.y - mid.position.y, first.position.x - mid.position.x)
        var angleDeg = Math.toDegrees(abs(angleRad.toDouble()))
        if (angleDeg > 180) {
            angleDeg = 360 - angleDeg
        }
        return angleDeg
    }
}