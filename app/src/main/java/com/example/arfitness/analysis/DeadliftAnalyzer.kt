package com.example.arfitness.analysis

import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark
import kotlin.math.abs
import kotlin.math.atan2

class DeadliftAnalyzer : ExerciseAnalyzer() {


    override val requiredLandmarks = listOf(
        PoseLandmark.RIGHT_SHOULDER, PoseLandmark.LEFT_SHOULDER,
        PoseLandmark.RIGHT_HIP, PoseLandmark.LEFT_HIP,
        PoseLandmark.RIGHT_KNEE, PoseLandmark.LEFT_KNEE,
        PoseLandmark.RIGHT_ANKLE, PoseLandmark.LEFT_ANKLE,
        PoseLandmark.RIGHT_WRIST, PoseLandmark.LEFT_WRIST // Barın pozisyonunu tahmin etmek için
    )

    // Deadlift için durumlar: Ayakta dik (UP) ve öne eğilmiş (DOWN)
    private var state: ExerciseState = ExerciseState.UP
    private var repCount = 0

    override fun analyze(pose: Pose): Feedback {
        // Deadlift, tüm vücudu kullandığı için çok sayıda noktaya ihtiyacımız var.
        val leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER)
        val leftHip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP)
        val leftKnee = pose.getPoseLandmark(PoseLandmark.LEFT_KNEE)
        val leftAnkle = pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE)
        val leftWrist = pose.getPoseLandmark(PoseLandmark.LEFT_WRIST)

        var feedbackMessage: String? = "Sırtını Düz Tut!" // Varsayılan mesaj en önemli kural olsun
        var repCounted = false

        if (leftShoulder != null && leftHip != null && leftKnee != null && leftAnkle != null && leftWrist != null) {

            // Açıları hesapla
            val backAngle = getAngle(leftShoulder, leftHip, leftAnkle) // Sırtın yere ne kadar paralel olduğu
            val hipAngle = getAngle(leftShoulder, leftHip, leftKnee)  // Kalça bükülme açısı

            // --- FORM KONTROLÜ ---
            // KURAL 1: SIRTIN DÜZLÜĞÜ (EN ÖNEMLİ KURAL)
            // Kalça, omuz ve ayak bileği arasındaki açı, sırtın düzlüğünü gösterir.
            // 150 dereceden küçükse, sırt yuvarlaklaşıyor demektir.
            if (backAngle < 150) {
                feedbackMessage = "SIRTINI YUVARLAMA! Düz tut!"
            }
            // KURAL 2: KALÇA POZİSYONU
            // Aşağıdayken kalça, omuzlardan aşağıda ama dizlerden yukarıda olmalı.
            else if (state == ExerciseState.DOWN) {
                if (leftHip.position.y < leftShoulder.position.y) {
                    feedbackMessage = "Kalçanı çok yukarıda başlatıyorsun!"
                } else if (leftHip.position.y > leftKnee.position.y) {
                    feedbackMessage = "Çömelme, kalçanı kaldır! Bu bir squat değil."
                }
            }


            // --- Tekrar Sayma Mantığı ---
            // Ayakta dik pozisyon (UP durumu)
            // Kalça açısı neredeyse tamamen açık olmalı
            val isUp = hipAngle > 160

            // Eğilmiş pozisyon (DOWN durumu)
            // Bilekler, diz hizasında veya daha aşağıda olmalı
            val isDown = leftWrist.position.y > leftKnee.position.y

            if (isDown) {
                if (state == ExerciseState.UP) {
                    state = ExerciseState.DOWN
                }
            } else if (isUp) {
                if (state == ExerciseState.DOWN) {
                    state = ExerciseState.UP
                    repCounted = true
                    repCount++
                    feedbackMessage = if (feedbackMessage == "Sırtını Düz Tut!") {
                        "$repCount. tekrar tamam!"
                    } else {
                        "$repCount. tekrar! ($feedbackMessage)"
                    }
                }
            }
        } else {
            feedbackMessage = "Vücudun tam olarak görünmüyor."
        }

        return Feedback(state, feedbackMessage, repCounted, pose)
    }

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
