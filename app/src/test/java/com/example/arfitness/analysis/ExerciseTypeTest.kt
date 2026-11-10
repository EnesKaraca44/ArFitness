package com.example.arfitness.analysis

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Eski `contains` eşlemesinde 15 hareketin 9'u PushupAnalyzer'a düşüyordu.
 * Bu test, uygulamadaki her kod adı ve görünen adın kendi analizörüne gittiğini garanti eder.
 */
class ExerciseTypeTest {

    // WorkoutRepository'deki (kodAdi, gorunenAd) çiftleri ve beklenen analizör
    private val cases = listOf(
        Triple("squat", "Squat", SquatAnalyzer::class),
        Triple("push-ups", "Push-Up", PushupAnalyzer::class),
        Triple("bicep curl", "Bicep Curl", BicepCurlAnalyzer::class),
        Triple("shoulder press", "Shoulder Press", ShoulderPressAnalyzer::class),
        Triple("lunge", "Lunge", LungeAnalyzer::class),
        Triple("dumbbell fly", "Dumbbell Fly", DumbbellFlyAnalyzer::class),
        Triple("bench press", "Bench Press", BenchPressAnalyzer::class),
        Triple("plank", "Plank", PlankAnalyzer::class),
        Triple("pull-up", "Pull-up", PullupAnalyzer::class),
        Triple("lat pulldown", "Lat Pulldown", LatPulldownAnalyzer::class),
        Triple("hammer curl", "Hammer Curl", HammerCurlAnalyzer::class),
        Triple("cable pushdown", "Cable Pushdown", CablePushdownAnalyzer::class),
        Triple("lateral raise", "Lateral Raise", LateralRaiseAnalyzer::class),
        Triple("front raise", "Front Raise", FrontRaiseAnalyzer::class),
        Triple("deadlift", "Deadlift", DeadliftAnalyzer::class),
    )

    @Test
    fun everyExercise_mapsToItsOwnAnalyzer() {
        for ((code, displayName, expected) in cases) {
            for (name in listOf(code, displayName)) {
                val type = ExerciseType.fromName(name)
                checkNotNull(type) { "'$name' tanınmadı" }
                assertEquals("'$name' yanlış analizöre gitti", expected, type.createAnalyzer()::class)
            }
        }
    }

    @Test
    fun allFifteenExercisesCovered() {
        assertEquals(ExerciseType.entries.size, cases.map { ExerciseType.fromName(it.first) }.toSet().size)
    }

    @Test
    fun unknownName_returnsNull_insteadOfPushup() {
        assertNull(ExerciseType.fromName("burpee"))
    }

    @Test
    fun onlyPlankIsTimed() {
        for (type in ExerciseType.entries) {
            val timed = type.createAnalyzer().isTimed
            if (type == ExerciseType.PLANK) assertTrue(timed) else assertFalse("$type süreli olmamalı", timed)
        }
    }
}
