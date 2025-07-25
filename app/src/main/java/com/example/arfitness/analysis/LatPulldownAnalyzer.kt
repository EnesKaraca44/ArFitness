package com.example.arfitness.analysis

import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark
import kotlin.math.abs
import kotlin.math.atan2

class LatPulldownAnalyzer : ExerciseAnalyzer {

    override val requiredLandmarks = listOf(
        PoseLandmark.RIGHT_SHOULDER, PoseLandmark.LEFT_SHOULDER,
        PoseLandmark.RIGHT_ELBOW, PoseLandmark.LEFT_ELBOW,
        PoseLandmark.RIGHT_WRIST, PoseLandmark.LEFT_WRIST,
        PoseLandmark.RIGHT_HIP, PoseLandmark.LEFT_HIP // Sırtın pozisyonunu kontrol etmek için
    )

    private var state: ExerciseState = ExerciseState.UP // Başlangıç durumu: Kollar yukarıda
    private var repCount = 0

    override fun analyze(pose: Pose): Feedback {
        // Gerekli eklem noktaları
        val leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER)
        val rightShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER)
        val leftElbow = pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW)
        val rightElbow = pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW)
        val leftWrist = pose.getPoseLandmark(PoseLandmark.LEFT_WRIST)
        val rightWrist = pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST)

        // --- YENİ: Sırt pozisyonu için kalça noktaları ---
        val leftHip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP)
        val rightHip = pose.getPoseLandmark(PoseLandmark.RIGHT_HIP)

        var feedbackMessage: String? = "Harika Form!"
        var repCounted = false

        if (leftShoulder != null && rightShoulder != null && leftElbow != null && rightElbow != null &&
            leftWrist != null && rightWrist != null && leftHip != null && rightHip != null) {

            // Ortalama bilek, omuz ve kalça yüksekliğini/pozisyonunu alalım
            val wristsY = (leftWrist.position.y + rightWrist.position.y) / 2
            val shouldersY = (leftShoulder.position.y + rightShoulder.position.y) / 2
            val hipsY = (leftHip.position.y + rightHip.position.y) / 2

            val leftElbowAngle = getAngle(leftShoulder, leftElbow, leftWrist)
            val rightElbowAngle = getAngle(rightShoulder, rightElbow, rightWrist)

            // --- YENİ: Gövde açısı için ---
            val bodyAngle = getAngle(leftShoulder, leftHip, rightHip) // Omuzlar ve kalça arasındaki açı

            // --- TEKRAR SAYMA MANTIĞI ---
            // Bar aşağıda, göğse yakın (DOWN durumu): Bilekler, omuz hizasında veya biraz altında
            // Dirseklerin de yeterince büküldüğünü kontrol edelim (örneğin 90 dereceden az)
            val isDownPosition = wristsY >= (shouldersY + 50) && leftElbowAngle < 90 && rightElbowAngle < 90 // 50 piksel tolerans

            // Bar yukarıda (UP durumu): Bilekler, omuzların belirgin şekilde üzerinde ve kollar neredeyse düz.
            val isUpPosition = leftElbowAngle > 165 && rightElbowAngle > 165 // Kollar tam düz

            if (isDownPosition) {
                if (state == ExerciseState.UP) {
                    state = ExerciseState.DOWN
                }
            } else if (isUpPosition) {
                if (state == ExerciseState.DOWN) {
                    state = ExerciseState.UP
                    repCounted = true // Tekrar tamamlandı olarak işaretle
                }
            }

            // --- FORM KONTROLÜ (Tekrar sayıldıktan sonra, en önemli hata öncelikli) ---
            if (bodyAngle < 165) { // Gövde çok fazla geriye yaslanıyorsa
                feedbackMessage = "Sırtını geriye yaslama, dik dur!"
            }
            else if (state == ExerciseState.DOWN && (wristsY > (shouldersY + 50) || wristsY < (shouldersY - 20))) { // Bar yeterince aşağıda/yukarıda değilse
                feedbackMessage = "Barı göğsünün üstüne doğru çek!"
            }
            else if (state == ExerciseState.UP && (leftElbowAngle < 170 || rightElbowAngle < 170)) { // Kollar tepede tam açılmadıysa
                feedbackMessage = "Kollarını tepede tam aç!"
            }

            // --- SON KARAR VE MESAJ ---
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