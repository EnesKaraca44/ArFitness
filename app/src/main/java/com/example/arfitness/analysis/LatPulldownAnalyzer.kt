package com.example.arfitness.analysis

import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark
import kotlin.math.abs
import kotlin.math.atan2

class LatPulldownAnalyzer : ExerciseAnalyzer() {
    override val requiredLandmarks = listOf(
        PoseLandmark.RIGHT_SHOULDER, PoseLandmark.LEFT_SHOULDER,
        PoseLandmark.RIGHT_ELBOW, PoseLandmark.LEFT_ELBOW,
        PoseLandmark.RIGHT_WRIST, PoseLandmark.LEFT_WRIST,
        PoseLandmark.RIGHT_HIP, PoseLandmark.LEFT_HIP // Sırtın pozisyonunu kontrol etmek için
    )

    private var state: ExerciseState = ExerciseState.UP // Başlangıç durumu: Kollar yukarıda
    private var repCount = 0

    // İdeal Lat Pulldown Şablonu (Kollar açık yukarıda -> Göğse çekiş -> Yukarı bırakış)
    private val idealLatPulldownTemplate = listOf(
        170.0, 140.0, 100.0, 70.0, 50.0, 70.0, 100.0, 140.0, 170.0
    )

    override fun analyze(pose: Pose): Feedback {
        val leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER)
        val rightShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER)
        val leftElbow = pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW)
        val rightElbow = pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW)
        val leftWrist = pose.getPoseLandmark(PoseLandmark.LEFT_WRIST)
        val rightWrist = pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST)
        val leftHip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP)
        val rightHip = pose.getPoseLandmark(PoseLandmark.RIGHT_HIP)

        val isLeftVisible = isVisible(leftShoulder, leftElbow, leftWrist, leftHip)
        val isRightVisible = isVisible(rightShoulder, rightElbow, rightWrist, rightHip)

        if (!isLeftVisible && !isRightVisible) {
            return Feedback(state, "Vücudunun üst kısmı net görünmüyor.", false, pose)
        }

        // Hangi taraf net görünüyorsa onu kullan (Yan profil desteği)
        val shoulder = if (isLeftVisible) leftShoulder!! else rightShoulder!!
        val elbow = if (isLeftVisible) leftElbow!! else rightElbow!!
        val wrist = if (isLeftVisible) leftWrist!! else rightWrist!!
        val hip = if (isLeftVisible) leftHip!! else rightHip!!

        // 3D Matematik + EMA Titreme Filtresi
        val rawElbowAngle = AnalysisUtils.getAngle(shoulder, elbow, wrist)
        val elbowAngle = getSmoothedAngle("latElbow", rawElbowAngle)

        // Gövde (bel) açısını bacaklarla bulmak için diz gerekli ama elimizde hip var.
        // Lat pulldown yaparken omuz, kalça ve dikey eksen arası eğilme hesabı yerine doğrudan y koordinatına bakabiliriz
        var feedbackMessage = "Harika Form!"
        var repCounted = false

        // Form Kontrolü (Bar göğse kadar inmiş mi?)
        if (state == ExerciseState.DOWN) {
            // Bileklerin y koordinatı omuzların hemen üstünde olmalı (göğüs hizası)
            if (wrist.position3D.y < shoulder.position3D.y - 150) {
                feedbackMessage = "Barı göğsüne kadar çek!"
            }
        }

        // DTW Makine Öğrenmesi ile Sayım
        if (matchPatternDTW("latpulldown", elbowAngle, idealLatPulldownTemplate, threshold = 22.0, bufferSize = 25, cooldownMs = 1500)) {
            repCount++
            repCounted = true
            feedbackMessage = "MÜKEMMEL FORM! $repCount. tekrar"
        } else {
            // Anlık görsel yönlendirme
            if (elbowAngle < 80) {
                state = ExerciseState.DOWN
            } else if (elbowAngle > 150) {
                state = ExerciseState.UP
            }
        }

        return Feedback(state, feedbackMessage, repCounted, pose)
    }
}
