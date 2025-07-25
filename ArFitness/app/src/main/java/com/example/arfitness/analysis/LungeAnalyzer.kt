package com.example.arfitness.analysis

import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark
import kotlin.math.abs
import kotlin.math.atan2

class LungeAnalyzer : ExerciseAnalyzer {

    // Lunge için de UP (ayakta) ve DOWN (hamle pozisyonu) durumları var.
    private var state: ExerciseState = ExerciseState.UP
    private var repCount = 0

    override fun analyze(pose: Pose): Feedback {
        // Lunge için tüm bacak eklemleri gerekli.
        val leftHip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP)
        val leftKnee = pose.getPoseLandmark(PoseLandmark.LEFT_KNEE)
        val leftAnkle = pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE)
        val rightHip = pose.getPoseLandmark(PoseLandmark.RIGHT_HIP)
        val rightKnee = pose.getPoseLandmark(PoseLandmark.RIGHT_KNEE)
        val rightAnkle = pose.getPoseLandmark(PoseLandmark.RIGHT_ANKLE)

        var feedbackMessage: String? = "Pozisyon al!"
        var repCounted = false

        if (leftHip != null && leftKnee != null && leftAnkle != null &&
            rightHip != null && rightKnee != null && rightAnkle != null) {

            // Hem sağ hem de sol dizin açılarını hesaplıyoruz.
            val leftKneeAngle = getAngle(leftHip, leftKnee, leftAnkle)
            val rightKneeAngle = getAngle(rightHip, rightKnee, rightAnkle)

            // --- Form Kontrolü ---
            // Basit bir kontrol: Kullanıcı çok derine inip dengesini bozuyor mu?
            // Ayak bileklerinin y koordinatını karşılaştırabiliriz.
            if (abs(leftAnkle.position.y - rightAnkle.position.y) < 50) { // Ayaklar çok yakınsa
                feedbackMessage = "Adımını daha geniş at!"
            } else {
                feedbackMessage = "Harika gidiyorsun!"
            }

            // --- Tekrar Sayma Mantığı ---
            // Lunge pozisyonu: Dizlerden biri çok bükük (örn: < 110 derece)
            // diğeri de bükük olmalı (örn: < 130 derece).
            val isLungePosition = (leftKneeAngle < 110 && rightKneeAngle < 130) ||
                    (rightKneeAngle < 110 && leftKneeAngle < 130)

            // Ayakta durma pozisyonu: Her iki diz de neredeyse düz.
            val isStandingPosition = leftKneeAngle > 160 && rightKneeAngle > 160

            if (isLungePosition) {
                if (state == ExerciseState.UP) {
                    state = ExerciseState.DOWN
                }
            } else if (isStandingPosition) {
                if (state == ExerciseState.DOWN) {
                    state = ExerciseState.UP
                    repCount++
                    repCounted = true
                    feedbackMessage = "$repCount. tekrar tamamlandı!"
                }
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