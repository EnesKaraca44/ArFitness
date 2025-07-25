package com.example.arfitness.analysis

import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.sqrt

class DumbbellFlyAnalyzer : ExerciseAnalyzer {

    override val requiredLandmarks = listOf(
        PoseLandmark.LEFT_SHOULDER, PoseLandmark.RIGHT_SHOULDER,
        PoseLandmark.LEFT_ELBOW, PoseLandmark.RIGHT_ELBOW,
        PoseLandmark.LEFT_WRIST, PoseLandmark.RIGHT_WRIST
    )


    // Dumbbell Fly için durumlar: Kollar açık (OPEN) ve kapalı (CLOSED)
    private var state: ExerciseState = ExerciseState.CLOSED
    private var repCount = 0


    override fun analyze(pose: Pose): Feedback {
        // Gerekli eklem noktaları
        val leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER)
        val rightShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER)
        val leftWrist = pose.getPoseLandmark(PoseLandmark.LEFT_WRIST)
        val rightWrist = pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST)
        val leftElbow = pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW)
        val rightElbow = pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW)

        var repCounted = false
        var feedbackMessage: String? = "Harika Form!"

        if (leftShoulder != null && rightShoulder != null && leftWrist != null && rightWrist != null && leftElbow != null && rightElbow != null) {

            // Hesaplamalar
            val wristDistance = getDistance(leftWrist, rightWrist)
            val shoulderWidth = getDistance(leftShoulder, rightShoulder)
            val leftElbowAngle = getAngle(leftShoulder, leftElbow, leftWrist)
            val rightElbowAngle = getAngle(rightShoulder, rightElbow, rightWrist)

            // Durum tespiti için daha hassas eşikler
            val areHandsClosed = wristDistance < (shoulderWidth * 0.7) // Eller omuz genişliğinden daha yakın
            val areHandsOpen = wristDistance > (shoulderWidth * 1.8)  // Eller omuz genişliğinin neredeyse 2 katı açık

            // --- DURUM GÜNCELLEME VE TEKRAR SAYMA ---
            if (areHandsOpen) {
                if (state == ExerciseState.CLOSED) {
                    state = ExerciseState.OPEN
                }
            } else if (areHandsClosed) {
                if (state == ExerciseState.OPEN) {
                    state = ExerciseState.CLOSED
                    // Tekrarın tamamlandığını işaretle
                    repCounted = true
                }
            }

            // --- FORM KONTROLÜ ---
            // KURAL 1: Dirsekler çok fazla bükülüyor mu?
            if (leftElbowAngle < 135 || rightElbowAngle < 135) {
                feedbackMessage = "Dirseklerini çok kırma!"
            }
            // KURAL 2: Kollar çok aşağıya düşüyor mu? (Sadece kollar açıkken kontrol et)
            else if (state == ExerciseState.OPEN && (leftWrist.position.y > leftShoulder.position.y + 50 || rightWrist.position.y > rightShoulder.position.y + 50)) {
                feedbackMessage = "Kollarını omuz hizasında tut!"
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


    // İki nokta arasındaki Öklid mesafesini hesaplayan yardımcı fonksiyon
    private fun getDistance(p1: PoseLandmark, p2: PoseLandmark): Float {
        val dx = p1.position.x - p2.position.x
        val dy = p1.position.y - p2.position.y
        return sqrt(dx * dx + dy * dy)
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