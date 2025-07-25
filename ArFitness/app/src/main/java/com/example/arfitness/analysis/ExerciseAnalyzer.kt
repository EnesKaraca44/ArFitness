package com.example.arfitness.analysis

import com.google.mlkit.vision.pose.Pose

interface ExerciseAnalyzer {
    fun analyze(pose: Pose): Feedback
}