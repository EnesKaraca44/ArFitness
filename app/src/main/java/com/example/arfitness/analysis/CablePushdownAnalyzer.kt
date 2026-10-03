package com.example.arfitness.analysis

import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark
import kotlin.math.abs
import kotlin.math.atan2

class CablePushdownAnalyzer : ExerciseAnalyzer() {

    override val requiredLandmarks = listOf(
        PoseLandmark.RIGHT_SHOULDER, PoseLandmark.LEFT_SHOULDER,
        PoseLandmark.RIGHT_ELBOW, PoseLandmark.LEFT_ELBOW,
        PoseLandmark.RIGHT_WRIST, PoseLandmark.LEFT_WRIST
    )

    private var state: ExerciseState = ExerciseState.UP
    private var repCount = 0

    // İdeal Cable Pushdown Şablonu (Kollar bükülü yukarıda -> Aşağı itiş -> Yukarı bırakış)
    private val idealPushdownTemplate = listOf(
        70.0, 100.0, 140.0, 170.0, 140.0, 100.0, 70.0
    )

    override fun analyze(pose: Pose): Feedback {
        val leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER)
        val leftElbow = pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW)
        val leftWrist = pose.getPoseLandmark(PoseLandmark.LEFT_WRIST)
        val rightShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER)
        val rightElbow = pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW)
        val rightWrist = pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST)

        val isLeftVisible = isVisible(leftShoulder, leftElbow, leftWrist)
        val isRightVisible = isVisible(rightShoulder, rightElbow, rightWrist)

        if (!isLeftVisible && !isRightVisible) {
            return Feedback(state, "Kolun net olarak görünmüyor (Kameraya uzaklaş).", false, pose)
        }

        // Hangi taraf net görünüyorsa onu kullan
        val shoulder = if (isLeftVisible) leftShoulder!! else rightShoulder!!
        val elbow = if (isLeftVisible) leftElbow!! else rightElbow!!
        val wrist = if (isLeftVisible) leftWrist!! else rightWrist!!

        // 3D Vektör açısı ve Smoothing (EMA)
        val rawElbowAngle = AnalysisUtils.getAngle(shoulder, elbow, wrist)
        val elbowAngle = getSmoothedAngle("pushdownElbow", rawElbowAngle)

        var feedbackMessage = "Harika Form!"
        var repCounted = false

        // --- FORM KONTROLÜ ---
        // KURAL 1: Dirsek vücuttan çok fazla ayrılıyor mu? (Omuz hizasından sapma)
        if (abs(elbow.position3D.x - shoulder.position3D.x) > 120) {
            feedbackMessage = "Dirseğini gövdene sabitle!"
        }

        // Merkezcil DTW Makine Öğrenmesi ile Sayım
        if (matchPatternDTW("cablepushdown", elbowAngle, idealPushdownTemplate, threshold = 22.0, bufferSize = 20, cooldownMs = 1200)) {
            repCount++
            repCounted = true
            feedbackMessage = "MÜKEMMEL FORM! $repCount. tekrar"
        } else {
            // Anlık görsel geri bildirim (Sayımı etkilemez)
            if (elbowAngle > 160) { 
                state = ExerciseState.DOWN
            } else if (elbowAngle < 90) { 
                state = ExerciseState.UP
            }
        }

        return Feedback(state, feedbackMessage, repCounted, pose)
    }
}
