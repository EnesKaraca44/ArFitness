package com.example.arfitness.analysis

import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark
import kotlin.math.abs
import kotlin.math.atan2

class HammerCurlAnalyzer : ExerciseAnalyzer() {

    override val requiredLandmarks = listOf(
        PoseLandmark.RIGHT_SHOULDER, // veya hem sağ hem sol
        PoseLandmark.RIGHT_ELBOW,
        PoseLandmark.RIGHT_WRIST,
        PoseLandmark.RIGHT_HIP
    )

    private var state: ExerciseState = ExerciseState.DOWN
    private var repCount = 0
    private var initialElbowY: Float? = null

    override fun analyze(pose: Pose): Feedback {
        val shoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER)
        val elbow = pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW)
        val wrist = pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST)

        var repCounted = false
        var feedbackMessage: String? = "Harika Form!" // Varsayılan mesaj pozitif

        if (shoulder != null && elbow != null && wrist != null) {
            val elbowAngle = getAngle(shoulder, elbow, wrist)

            // Hareketin "yukarıda" (bükülü) ve "aşağıda" (düz) olma durumlarını belirleyelim
            val isUp = elbowAngle < 75   // Eşiği biraz daha esnek yapalım (70 yerine 75)
            val isDown = elbowAngle > 145 // Eşiği biraz daha esnek yapalım (150 yerine 145)

            // --- DURUM GÜNCELLEME VE TEKRAR SAYMA ---
            if (isUp) {
                if (state == ExerciseState.DOWN) {
                    state = ExerciseState.UP
                }
            } else if (isDown) {
                if (state == ExerciseState.UP) {
                    state = ExerciseState.DOWN
                    // Bu noktada bir tekrar tamamlandı, bayrağı işaretle.
                    // Henüz sayıyı artırma, önce formu kontrol edeceğiz.
                    repCounted = true
                }
            }

            // --- FORM KONTROLÜ (Öncelikli) ---
            // Form kontrollerini bir zincir halinde yapıyoruz. İlk bulunan hata gösterilir.
            if (initialElbowY == null) initialElbowY = elbow.position.y

            if (abs(elbow.position.y - (initialElbowY ?: elbow.position.y)) > 70) { // Toleransı artırabiliriz
                feedbackMessage = "Dirseğini sabit tut!"
            }
            // Diğer kontrolleri, sadece ilk kural doğruysa yap
            else if (state == ExerciseState.UP && elbowAngle > 60) {
                feedbackMessage = "Tepe noktada tam sıkıştır!"
            }
            else if (state == ExerciseState.DOWN && elbowAngle < 140) {
                feedbackMessage = "Kolunu aşağıda tam aç!"
            }

            // --- SON KARAR VE MESAJ ---
            // Eğer bu analizde bir tekrar sayıldıysa VE o ana kadar hiçbir form hatası bulunmadıysa,
            // o zaman tekrar sayısını artır ve başarı mesajı göster.
            if (repCounted) {
                repCount++
                feedbackMessage = if (feedbackMessage == "Harika Form!") {
                    "$repCount. tekrar tamam!"
                } else {
                    "$repCount. tekrar! ($feedbackMessage)"
                }
            }

        } else {
            feedbackMessage = "Kolun tam olarak görünmüyor."
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
