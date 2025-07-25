package com.example.arfitness.analysis

import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark
import kotlin.math.abs
import kotlin.math.atan2

class ShoulderPressAnalyzer : ExerciseAnalyzer {
    override val requiredLandmarks = listOf(
        PoseLandmark.RIGHT_SHOULDER, PoseLandmark.LEFT_SHOULDER,
        PoseLandmark.RIGHT_ELBOW, PoseLandmark.LEFT_ELBOW,
        PoseLandmark.RIGHT_WRIST, PoseLandmark.LEFT_WRIST,
        PoseLandmark.RIGHT_HIP // Sırtın geriye yaslanmasını kontrol etmek için
    )

    private var state: ExerciseState = ExerciseState.DOWN
    private var repCount = 0

    override fun analyze(pose: Pose): Feedback {
        // Gerekli eklem noktaları
        val rightShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER)
        val rightElbow = pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW)
        val rightWrist = pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST)
        val leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER)
        val leftElbow = pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW)
        val leftWrist = pose.getPoseLandmark(PoseLandmark.LEFT_WRIST)
        val rightHip = pose.getPoseLandmark(PoseLandmark.RIGHT_HIP)

        var repCounted = false
        var feedbackMessage: String? = "Harika Form!"

        if (rightShoulder != null && rightElbow != null && rightWrist != null &&
            leftShoulder != null && leftElbow != null && leftWrist != null && rightHip != null) {

            // Pozisyonları ve açıları hesapla
            val rightWristY = rightWrist.position.y
            val rightShoulderY = rightShoulder.position.y
            val leftWristY = leftWrist.position.y
            val leftShoulderY = leftShoulder.position.y
            val rightElbowAngle = getAngle(rightShoulder, rightElbow, rightWrist)

            // --- HAREKETİN DURUMUNU BELİRLE ---
            // Kolların yukarıda kabul edilmesi için, bileklerin dirseklerden de yukarıda olması gerekir.
            // Bu, hareketin gerçekten tepe noktasına ulaştığını garantiler.
            val areHandsUp = rightWristY < rightElbow.position.y && leftWristY < leftElbow.position.y

            // Kolların aşağıda kabul edilmesi için, bileklerin omuz hizasına inmesi gerekir.
            val areHandsDown = rightWristY >= rightShoulderY && leftWristY >= leftShoulderY

            // --- DURUM GÜNCELLEME VE TEKRAR SAYMA ---
            if (areHandsUp) {
                if (state == ExerciseState.DOWN) {
                    state = ExerciseState.UP
                }
            } else if (areHandsDown) {
                if (state == ExerciseState.UP) {
                    state = ExerciseState.DOWN
                    repCounted = true // Tekrarın tamamlandığını işaretle
                }
            }

            // --- FORM KONTROLÜ (ÖNCELİKLİ) ---
            // Sırt açısını bu şekilde hesaplamak daha doğru olabilir: Omuz, Kalça ve Ayak Bileği.
            // Şimdilik daha basit bir yaklaşımla devam edelim.
            // KURAL 1: Yeterince aşağı indirildi mi? (En sık yapılan hata)
            if (state == ExerciseState.DOWN && rightWristY < rightShoulderY - 20) { // Omuz hizasından yukarıdaysa
                feedbackMessage = "Omuz hizasına kadar indir."
            }
            // KURAL 2: Kollar tepede tam kilitlendi mi?
            else if (state == ExerciseState.UP && rightElbowAngle < 160) {
                feedbackMessage = "Kollarını tepede tam düzleştir!"
            }

            // --- SON KARAR VE MESAJ ---
            // Eğer bir tekrar sayıldıysa VE o ana kadar hiçbir form hatası bulunmadıysa,
            // o zaman tekrar sayısını artır ve başarı mesajı göster.
            if (repCounted && feedbackMessage == "Harika Form!") {
                repCount++
                feedbackMessage = "$repCount. tekrar tamam!"
            }

        } else {
            feedbackMessage = "Vücudunun üst kısmı tam görünmüyor."
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