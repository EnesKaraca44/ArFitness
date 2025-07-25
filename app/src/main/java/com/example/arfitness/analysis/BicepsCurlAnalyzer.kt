package com.example.arfitness.analysis

import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark
import kotlin.math.abs
import kotlin.math.atan2

class BicepCurlAnalyzer : ExerciseAnalyzer {

    override val requiredLandmarks = listOf(
        PoseLandmark.RIGHT_SHOULDER, // Veya hem sağ hem sol
        PoseLandmark.RIGHT_ELBOW,
        PoseLandmark.RIGHT_WRIST,
        PoseLandmark.RIGHT_HIP // Vücut salınımını kontrol etmek için
    )

    // Bicep curl için durumlar: Kol aşağıda (DOWN) ve kol yukarıda (UP)
    private var state: ExerciseState = ExerciseState.DOWN
    private var repCount = 0

    // Dirseğin pozisyonunu takip etmek için
    // --- YENİ: Önceki açıyı takip etmek için bir değişken ---
    private var previousAngle: Double = 180.0

    override fun analyze(pose: Pose): Feedback {
        val shoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER)
        val elbow = pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW)
        val wrist = pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST)

        var repCounted = false
        var feedbackMessage: String? = "Harika Form!"

        if (shoulder != null && elbow != null && wrist != null) {
            val elbowAngle = getAngle(shoulder, elbow, wrist)

            // --- YENİ VE DAHA SAĞLAM TEKRAR SAYMA MANTIĞI ---
            // Açı daralıyorsa (kol kapanıyorsa) ve daha önce aşağıdaysak, UP durumuna geç.
            if (elbowAngle < previousAngle - 2 && state == ExerciseState.DOWN) { // 2 derecelik bir eşik
                state = ExerciseState.UP
            }
            // Açı genişliyorsa (kol açılıyorsa) ve daha önce yukardaysak, DOWN durumuna geç ve tekrar say.
            else if (elbowAngle > previousAngle + 2 && state == ExerciseState.UP) {
                state = ExerciseState.DOWN
                repCounted = true
            }

            // Mevcut açıyı bir sonraki kare için kaydet
            previousAngle = elbowAngle

            // --- FORM KONTROLLERİ (Aynı kalabilir) ---
            // ... (Dirsek sabit mi? Yeterince sıkıştırıldı mı? vb.) ...
            // ...

            // --- SON KARAR VE MESAJ ---
            if (repCounted && feedbackMessage == "Harika Form!") {
                repCount++
                feedbackMessage = "$repCount. tekrar tamam!"
            }

        } else {
            feedbackMessage = "Kolun tam olarak görünmüyor."
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