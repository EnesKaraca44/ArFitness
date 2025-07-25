package com.example.arfitness.analysis

import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark

class SquatAnalyzer : ExerciseAnalyzer {

    override val requiredLandmarks = listOf(
        PoseLandmark.LEFT_SHOULDER, PoseLandmark.RIGHT_SHOULDER,
        PoseLandmark.LEFT_HIP, PoseLandmark.RIGHT_HIP,
        PoseLandmark.LEFT_KNEE, PoseLandmark.RIGHT_KNEE,
        PoseLandmark.LEFT_ANKLE, PoseLandmark.RIGHT_ANKLE
    )

    private var state: ExerciseState = ExerciseState.UP // Başlangıç durumu UP olsun
    private var repCount = 0

    override fun analyze(pose: Pose): Feedback {
        // Gerekli tüm eklem noktalarını alıyoruz
        val leftHip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP)
        val leftKnee = pose.getPoseLandmark(PoseLandmark.LEFT_KNEE)
        val leftAnkle = pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE)
        val rightHip = pose.getPoseLandmark(PoseLandmark.RIGHT_HIP)
        val rightKnee = pose.getPoseLandmark(PoseLandmark.RIGHT_KNEE)
        val rightAnkle = pose.getPoseLandmark(PoseLandmark.RIGHT_ANKLE)
        val leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER)

        var repCounted = false
        var feedbackMessage: String? = "Harika Form!"

        if (leftHip != null && leftKnee != null && leftAnkle != null && rightHip != null &&
            rightKnee != null && rightAnkle != null && leftShoulder != null) {

            // Açıları hesapla
            val leftKneeAngle = AnalysisUtils.getAngle(leftHip, leftKnee, leftAnkle)
            val rightKneeAngle = AnalysisUtils.getAngle(rightHip, rightKnee, rightAnkle)
            val leftBackAngle = AnalysisUtils.getAngle(leftShoulder, leftHip, leftKnee)

            // --- HAREKETİN DURUMUNU BELİRLE ---
            // DOWN durumu: Her iki diz de 110 dereceden daha fazla bükülmüşse
            val isDown = leftKneeAngle < 110 && rightKneeAngle < 110

            // UP durumu: Her iki diz de 150 dereceden daha fazla açıksa (düzleşmişse)
            val isUp = leftKneeAngle > 150 && rightKneeAngle > 150


            // --- DURUM GÜNCELLEME VE TEKRAR SAYMA ---
            if (isDown) {
                if (state == ExerciseState.UP) {
                    state = ExerciseState.DOWN
                }
            } else if (isUp) {
                if (state == ExerciseState.DOWN) {
                    state = ExerciseState.UP
                    repCounted = true // Tekrarın tamamlandığını işaretle
                }
            }

            // --- FORM KONTROLÜ (Öncelikli) ---
            // KURAL 1: Sırt çok fazla öne eğildi mi?
            if (leftBackAngle < 150) {
                feedbackMessage = "Sırtını dik tut!"
            }
            // KURAL 2: Yeterince derine inildi mi? (Sadece aşağıdayken kontrol et)
            else if (state == ExerciseState.DOWN) {
                if (leftHip.position.y > leftKnee.position.y) {
                    feedbackMessage = "Biraz daha aşağı in!"
                }
            }


            // --- SON KARAR VE MESAJ ---
            if (repCounted && feedbackMessage == "Harika Form!") {
                repCount++
                feedbackMessage = "$repCount. tekrar tamam!"
            }
        } else {
            feedbackMessage = "Vücut tam olarak görünmüyor."
          }

        // --- Form Kontrolü ---


        // --- Raporu Oluşturma ---
        // Analiz sonucunda standart Feedback formunu doldurup geri gönderiyoruz.
        return Feedback(
            state = state,
            isRepCounted = repCounted,
            message = feedbackMessage,
            pose = pose
            // message = ... (form kontrolünden gelen mesaj)
        )
    }
}

