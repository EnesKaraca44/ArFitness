package com.example.arfitness.analysis

import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark
import kotlin.math.abs
import kotlin.math.atan2

class PullupAnalyzer : ExerciseAnalyzer {
    override val requiredLandmarks = listOf(
        PoseLandmark.RIGHT_SHOULDER, PoseLandmark.LEFT_SHOULDER,
        PoseLandmark.RIGHT_ELBOW, PoseLandmark.LEFT_ELBOW,
        PoseLandmark.RIGHT_WRIST, PoseLandmark.LEFT_WRIST,
        PoseLandmark.RIGHT_HIP, PoseLandmark.LEFT_HIP // Vücut salınımını kontrol etmek için
    )


    // Pull-up için durumlar: Kollar açık, aşağıda asılı (DOWN) ve yukarı çekilmiş (UP)
    private var state: ExerciseState = ExerciseState.DOWN
    private var repCount = 0

    // Hareketin tepe noktasını referans almak için başlangıçtaki bilek pozisyonu
    private var initialWristY: Float? = null

    override fun analyze(pose: Pose): Feedback {
        // Gerekli eklem noktaları
        val leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER)
        val rightShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER)
        val leftElbow = pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW)
        val rightElbow = pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW)
        val leftWrist = pose.getPoseLandmark(PoseLandmark.LEFT_WRIST)
        val rightWrist = pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST)
        val nose = pose.getPoseLandmark(PoseLandmark.NOSE) // Çenenin pozisyonunu tahmin etmek için

        var feedbackMessage: String? = "Harika Form!"
        var repCounted = false

        if (leftShoulder != null && rightShoulder != null && leftElbow != null && rightElbow != null &&
            leftWrist != null && rightWrist != null && nose != null) {

            // Başlangıçta, kullanıcının barı tuttuğu varsayılan bilek yüksekliğini kaydet
            if (initialWristY == null) {
                initialWristY = (leftWrist.position.y + rightWrist.position.y) / 2
            }

            // Mevcut vücut pozisyonları
            val shoulderY = (leftShoulder.position.y + rightShoulder.position.y) / 2
            val noseY = nose.position.y

            // --- FORM KONTROLÜ ---
            // KURAL 1: Tepe noktada çene barı geçti mi?
            if (state == ExerciseState.UP && noseY > (initialWristY ?: 0f)) {
                feedbackMessage = "Çeneni barın üzerine çıkar!"
            }
            // KURAL 2: Alt noktada kollar tam açıldı mı?
            else if (state == ExerciseState.DOWN) {
                val leftElbowAngle = getAngle(leftShoulder, leftElbow, leftWrist)
                if (leftElbowAngle < 160) {
                    feedbackMessage = "En altta kollarını tam aç!"
                }
            }

            // --- Tekrar Sayma Mantığı ---
            // Vücut yukarıda (UP durumu): Omuzlar, başlangıçtaki bilek yüksekliğine yakın veya üzerinde
            val isUp = shoulderY < (initialWristY ?: shoulderY) + 50 // 50 piksel tolerans

            // Vücut aşağıda (DOWN durumu): Kolların neredeyse tamamen açık olduğu durum
            val isDown = getAngle(leftShoulder, leftElbow, leftWrist) > 160

            if (isUp) {
                if (state == ExerciseState.DOWN) {
                    state = ExerciseState.UP
                }
            } else if (isDown) {
                if (state == ExerciseState.UP) {
                    state = ExerciseState.DOWN
                    repCounted = true
                    if (feedbackMessage == "Harika Form!") {
                        repCount++
                        feedbackMessage = "$repCount. tekrar tamamlandı!"
                    }
                }
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
