package com.example.arfitness.analysis

import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark
import kotlin.math.abs
import kotlin.math.atan2

class BicepCurlAnalyzer : ExerciseAnalyzer {

    // Bicep curl için durumlar: Kol aşağıda (DOWN) ve kol yukarıda (UP)
    private var state: ExerciseState = ExerciseState.DOWN
    private var repCount = 0

    // Dirseğin pozisyonunu takip etmek için
    private var initialElbowY: Float? = null

    override fun analyze(pose: Pose): Feedback {
        // Bicep curl için sadece bir kolun eklemleri yeterli.
        // Genellikle sağ kolu takip ederiz ama sol da olabilir.
        val shoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER)
        val elbow = pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW)
        val wrist = pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST)

        var feedbackMessage: String? = "Pozisyon al!"
        var repCounted = false

        if (shoulder != null && elbow != null && wrist != null) {

            // Dirsek açısını hesapla
            val elbowAngle = getAngle(shoulder, elbow, wrist)

            // --- Form Kontrolü: Dirsek Sabit mi? ---
            // İlk karede dirseğin başlangıç pozisyonunu kaydediyoruz.
            if (initialElbowY == null) {
                initialElbowY = elbow.position.y
            }

            // Eğer dirsek başlangıç pozisyonundan çok fazla yukarı veya aşağı hareket ederse, bu bir hiledir.
            if (abs(elbow.position.y - (initialElbowY ?: elbow.position.y)) > 60) { // 60 piksellik bir tolerans
                feedbackMessage = "Dirseğini sabit tut, omuzdan güç alma!"
            } else {
                feedbackMessage = "Harika form!"
            }

            // --- Tekrar Sayma Mantığı ---
            // Kol yukarıda, bükülmüş durumda (UP)
            if (elbowAngle < 60) { // Dirsek açısı 60'tan küçükse
                if (state == ExerciseState.DOWN) {
                    state = ExerciseState.UP
                }
            }
            // Kol aşağıda, düz durumda (DOWN)
            else if (elbowAngle > 150) { // Dirsek açısı 150'den büyükse
                if (state == ExerciseState.UP) {
                    state = ExerciseState.DOWN
                    repCount++
                    repCounted = true
                    feedbackMessage = "$repCount. tekrar tamamlandı!"
                }
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