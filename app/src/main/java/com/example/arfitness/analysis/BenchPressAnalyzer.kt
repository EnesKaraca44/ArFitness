package com.example.arfitness.analysis

import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark
import kotlin.math.abs
import kotlin.math.atan2

class BenchPressAnalyzer : ExerciseAnalyzer() {

    override val requiredLandmarks = listOf(
        PoseLandmark.RIGHT_SHOULDER, PoseLandmark.LEFT_SHOULDER,
        PoseLandmark.RIGHT_ELBOW, PoseLandmark.LEFT_ELBOW,
        PoseLandmark.RIGHT_WRIST, PoseLandmark.LEFT_WRIST
    )

    // Bench Press İdeal Zaman Serisi Şablonu (Kollar kilitli yukarıda -> Göğse iniş -> Yukarı itiş)
    private val idealBenchTemplate = listOf(
        170.0, 140.0, 100.0, 70.0, 50.0, 70.0, 100.0, 140.0, 170.0
    )

    private var state: ExerciseState = ExerciseState.UP
    private var repCount = 0

    override fun analyze(pose: Pose): Feedback {
        val leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER)
        val leftElbow = pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW)
        val leftWrist = pose.getPoseLandmark(PoseLandmark.LEFT_WRIST)
        val rightShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER)
        val rightElbow = pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW)
        val rightWrist = pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST)

        var feedbackMessage = "Harika Form!"
        var repCounted = false

        val isLeftVisible = isVisible(leftShoulder, leftElbow, leftWrist)
        val isRightVisible = isVisible(rightShoulder, rightElbow, rightWrist)

        if (!isLeftVisible && !isRightVisible) {
            return Feedback(state, "Kollarının net görünmesi lazım (Kameraya uzaklaş).", false, pose)
        }

        // Hangi kol net görünüyorsa onu kullan (Yan profil desteği)
        val shoulder = if (isLeftVisible) leftShoulder!! else rightShoulder!!
        val elbow = if (isLeftVisible) leftElbow!! else rightElbow!!
        val wrist = if (isLeftVisible) leftWrist!! else rightWrist!!

        // 3D Matematik + Titreme (EMA) Filtresi
        val rawElbowAngle = AnalysisUtils.getAngle(shoulder, elbow, wrist)
        val elbowAngle = getSmoothedAngle("benchElbow", rawElbowAngle)

        // FORM KONTROLÜ
        if (state == ExerciseState.DOWN) {
            // İniş yeterli mi? Bench press'te bilek dirseğin hemen üstündedir, y koordinatı olarak altındadır (Android'de Y aşağı artar).
            if (wrist.position3D.y < elbow.position3D.y) {
                feedbackMessage = "Barı göğsüne kadar indir!"
            }
        } else if (state == ExerciseState.UP) {
            if (elbowAngle < 150) {
                feedbackMessage = "Kollarını tam kilitle!"
            }
        }

        // Merkezcil DTW Makine Öğrenmesi ile Bench Press Sayımı
        if (matchPatternDTW("benchpress", elbowAngle, idealBenchTemplate, threshold = 22.0, bufferSize = 25, cooldownMs = 1500)) {
            repCount++
            repCounted = true
            feedbackMessage = "MÜKEMMEL FORM! $repCount. tekrar"
        } else {
            // Anlık görsel geri bildirim (Sayımı etkilemez)
            if (elbowAngle < 90) { // Daha derine inebilsin
                state = ExerciseState.DOWN
            } else if (elbowAngle > 150) { // Biraz daha esnek kilitlenme
                state = ExerciseState.UP
            }
        }

        return Feedback(state, feedbackMessage, repCounted, pose)
    }
}
