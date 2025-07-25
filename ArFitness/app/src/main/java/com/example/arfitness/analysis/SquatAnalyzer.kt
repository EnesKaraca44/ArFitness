package com.example.arfitness.analysis

import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark

/**
 * Squat hareketini analiz etmekten sorumlu uzman sınıf.
 * ExerciseAnalyzer sözleşmesine uyar.
 */
class SquatAnalyzer : ExerciseAnalyzer {

    // Kullanıcının o anki durumunu takip etmek için bir değişken.
    // Başlangıçta 'NEUTRAL' (ayakta) durumunda.
    private var state: ExerciseState = ExerciseState.NEUTRAL

    /**
     * Gelen her bir iskelet karesini analiz eder ve bir Feedback döndürür.
     */
    override fun analyze(pose: Pose): Feedback {
        // Gerekli eklem noktalarını alıyoruz. Eğer biri bile bulunamazsa, analiz yapma.
        val leftHip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP)
        val leftKnee = pose.getPoseLandmark(PoseLandmark.LEFT_KNEE)
        val leftAnkle = pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE)

        val rightHip = pose.getPoseLandmark(PoseLandmark.RIGHT_HIP)
        val rightKnee = pose.getPoseLandmark(PoseLandmark.RIGHT_KNEE)
        val rightAnkle = pose.getPoseLandmark(PoseLandmark.RIGHT_ANKLE)

        if (leftHip == null || leftKnee == null || leftAnkle == null ||
            rightHip == null || rightKnee == null || rightAnkle == null) {
            // Eğer temel noktalar yoksa, bilinmeyen bir durum raporla.
            return Feedback(ExerciseState.UNKNOWN, "Vücut tam olarak görünmüyor.", pose = pose)
        }

        // --- Açıları Hesaplama ---
        // Yardımcı fonksiyonumuzu kullanarak diz açılarını hesaplıyoruz.
        val leftKneeAngle = AnalysisUtils.getAngle(leftHip, leftKnee, leftAnkle)
        val rightKneeAngle = AnalysisUtils.getAngle(rightHip, rightKnee, rightAnkle)

        // --- Tekrar Sayma Mantığı ---
        var repCounted = false

        // Eğer dizler yeterince büküldüyse (örn: 100 dereceden az) kullanıcı 'DOWN' durumundadır.
        if (leftKneeAngle < 100 && rightKneeAngle < 100) {
            // Eğer daha önce 'NEUTRAL' veya 'UP' durumundaysa, şimdi 'DOWN' oldu demektir.
            // Bu, bir tekrarın yarısının tamamlandığı anlamına gelir.
            if (state == ExerciseState.NEUTRAL || state == ExerciseState.UP) {
                state = ExerciseState.DOWN
            }
        }
        // Eğer dizler tekrar düzelmeye başladıysa (örn: 160 dereceden fazla) kullanıcı 'UP' durumundadır.
        else if (leftKneeAngle > 160 && rightKneeAngle > 160) {
            // Eğer daha önce 'DOWN' durumundaysa, bu bir tekrarın tamamlandığı andır!
            if (state == ExerciseState.DOWN) {
                state = ExerciseState.UP
                repCounted = true // Rapor formuna "bir tekrar saydım" diye işaretliyoruz.
            } else {
                // Eğer zaten ayaktaysa, durumu NEUTRAL yap.
                state = ExerciseState.NEUTRAL
            }
        }

        // --- Form Kontrolü ---


        // --- Raporu Oluşturma ---
        // Analiz sonucunda standart Feedback formunu doldurup geri gönderiyoruz.
        return Feedback(
            state = state,
            isRepCounted = repCounted,
            pose = pose
            // message = ... (form kontrolünden gelen mesaj)
        )
    }
}

