package com.example.arfitness.analysis

import com.google.mlkit.vision.pose.Pose

data class Feedback(val state: ExerciseState,val message: String? = null,     val isRepCounted: Boolean = false, val pose: Pose   )


// analysis paketi için
// Artık elimizde standart bir "rapor" formatı var (Feedback).
//Bu raporu dolduracak olan tüm analizörlerin uyması gereken bir "kural kitabı" var (ExerciseAnalyzer).
//Ve kullanıcının durumunu takip etmek için standart "etiketlerimiz" var (ExerciseState).
