package com.example.arfitness.analysis

import com.google.mlkit.vision.pose.Pose

interface ExerciseAnalyzer {
    val requiredLandmarks: List<Int>
    fun analyze(pose: Pose): Feedback
}