package com.example.arfitness.analysis

import androidx.annotation.StringRes
import com.example.arfitness.R

/**
 * Uygulamadaki 15 hareketin tek kaynağı.
 *
 * Egzersiz ekranına hem kod adı ("push-ups") hem görünen ad ("Push-Up") gelebildiği için
 * isimler normalize edilip takma ad listesiyle birebir eşleştirilir. `contains` ile tahmin
 * yapılmaz: tanınmayan bir isim artık sessizce Push-Up analizörüne düşmez.
 */
enum class ExerciseType(
    @StringRes val titleRes: Int,
    /** Telefonun nereye konulacağı: analizörün sinyali bu çekim açısına göre seçildi. */
    @StringRes val cameraHintRes: Int,
    private val aliases: Set<String>,
) {
    SQUAT(R.string.squat, R.string.camera_hint_squat, setOf("squat", "squats")),
    PUSH_UP(R.string.push_up, R.string.camera_hint_push_up, setOf("push up", "push ups", "pushup", "pushups")),
    BICEP_CURL(R.string.bicep_curl, R.string.camera_hint_curl, setOf("bicep curl", "biceps curl", "biceps", "bicep")),
    SHOULDER_PRESS(R.string.shoulder_press, R.string.camera_hint_shoulder_press, setOf("shoulder press")),
    LUNGE(R.string.lunge, R.string.camera_hint_lunge, setOf("lunge", "lunges")),
    DUMBBELL_FLY(R.string.dumbbell_fly, R.string.camera_hint_dumbbell_fly, setOf("dumbbell fly", "dumbell fly", "dumbbell flyes", "dumbell flyes")),
    BENCH_PRESS(R.string.bench_press, R.string.camera_hint_bench_press, setOf("bench press")),
    PLANK(R.string.plank, R.string.camera_hint_plank, setOf("plank")),
    PULL_UP(R.string.pull_up, R.string.camera_hint_pull_up, setOf("pull up", "pull ups", "pullup", "pullups")),
    LAT_PULLDOWN(R.string.lat_pulldown, R.string.camera_hint_lat_pulldown, setOf("lat pulldown", "lat pull down")),
    HAMMER_CURL(R.string.hammer_curl, R.string.camera_hint_curl, setOf("hammer curl")),
    CABLE_PUSHDOWN(R.string.cable_pushdown, R.string.camera_hint_cable_pushdown, setOf("cable pushdown", "cable push down")),
    LATERAL_RAISE(R.string.lateral_raise, R.string.camera_hint_raise, setOf("lateral raise")),
    FRONT_RAISE(R.string.front_raise, R.string.camera_hint_raise, setOf("front raise")),
    DEADLIFT(R.string.deadlift, R.string.camera_hint_deadlift, setOf("deadlift", "dead lift"));

    fun createAnalyzer(): ExerciseAnalyzer = when (this) {
        SQUAT -> SquatAnalyzer()
        PUSH_UP -> PushupAnalyzer()
        BICEP_CURL -> BicepCurlAnalyzer()
        SHOULDER_PRESS -> ShoulderPressAnalyzer()
        LUNGE -> LungeAnalyzer()
        DUMBBELL_FLY -> DumbbellFlyAnalyzer()
        BENCH_PRESS -> BenchPressAnalyzer()
        PLANK -> PlankAnalyzer()
        PULL_UP -> PullupAnalyzer()
        LAT_PULLDOWN -> LatPulldownAnalyzer()
        HAMMER_CURL -> HammerCurlAnalyzer()
        CABLE_PUSHDOWN -> CablePushdownAnalyzer()
        LATERAL_RAISE -> LateralRaiseAnalyzer()
        FRONT_RAISE -> FrontRaiseAnalyzer()
        DEADLIFT -> DeadliftAnalyzer()
    }

    companion object {
        fun normalize(name: String): String = name
            .trim()
            .lowercase()
            .replace('_', ' ')
            .replace('-', ' ')
            .replace(Regex("\\s+"), " ")

        /** Kod adı veya görünen addan hareketi bulur; tanınmazsa null döner. */
        fun fromName(name: String): ExerciseType? {
            val key = normalize(name)
            return entries.firstOrNull { key in it.aliases }
        }
    }
}
