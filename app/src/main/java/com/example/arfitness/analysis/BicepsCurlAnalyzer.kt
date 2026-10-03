package com.example.arfitness.analysis

import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark
import kotlin.math.abs

class BicepCurlAnalyzer : ExerciseAnalyzer() {

    private var state: ExerciseState = ExerciseState.DOWN
    private var repCount = 0
    private var initialElbowY: Float? = null

    // İdeal Biceps Curl Zaman Serisi Şablonu (Kollar aşağıda -> Yukarı curl -> Aşağı bırakış)
    private val idealBicepsTemplate = listOf(
        170.0, 140.0, 100.0, 70.0, 50.0, 70.0, 100.0, 140.0, 170.0
    )

    override fun analyze(pose: Pose): Feedback {
        val rightShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER)
        val rightElbow = pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW)
        val rightWrist = pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST)

        val leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER)
        val leftElbow = pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW)
        val leftWrist = pose.getPoseLandmark(PoseLandmark.LEFT_WRIST)

        val isRightVisible = isVisible(rightShoulder, rightElbow, rightWrist)
        val isLeftVisible = isVisible(leftShoulder, leftElbow, leftWrist)

        if (!isRightVisible && !isLeftVisible) {
            return Feedback(state, "Kolun net olarak görünmüyor (Kameraya uzaklaş).", false, pose)
        }

        // Hangi taraf görünüyorsa onu kullan (Yan profil desteği)
        val shoulder = if (isRightVisible) rightShoulder!! else leftShoulder!!
        val elbow = if (isRightVisible) rightElbow!! else leftElbow!!
        val wrist = if (isRightVisible) rightWrist!! else leftWrist!!

        // 3D Vektör açısı ve Smoothing Filtresi
        val rawElbowAngle = AnalysisUtils.getAngle(shoulder, elbow, wrist)
        val elbowAngle = getSmoothedAngle("bicepElbow", rawElbowAngle)

        var feedbackMessage = "Pozisyon al!"
        var repCounted = false

        // Form kontrolü: Dirsek stabil mi? Omuzdan savuruyor mu? (Hile kontrolü)
        if (initialElbowY == null) {
            initialElbowY = elbow.position3D.y
        }

        if (abs(elbow.position3D.y - (initialElbowY ?: elbow.position3D.y)) > 75) {
            feedbackMessage = "Dirseğini sabit tut, omuzdan savurma (Hile yapma)!"
        } else {
            feedbackMessage = "Harika form!"
        }

        // DTW Makine Öğrenmesi ile Sayım
        if (matchPatternDTW("biceps", elbowAngle, idealBicepsTemplate, threshold = 22.0, bufferSize = 25, cooldownMs = 1200)) {
            repCount++
            repCounted = true
            feedbackMessage = "MÜKEMMEL FORM! $repCount. tekrar"
        } else {
            // Anlık görsel geri bildirim
            if (elbowAngle < 65) {
                state = ExerciseState.UP
            } else if (elbowAngle > 145) {
                state = ExerciseState.DOWN
            }
        }

        return Feedback(state, feedbackMessage, repCounted, pose)
    }
}
