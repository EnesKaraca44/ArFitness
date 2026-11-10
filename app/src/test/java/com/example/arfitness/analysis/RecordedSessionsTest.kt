package com.example.arfitness.analysis

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import java.io.File

/**
 * Telefonda (debug derlemesi) kaydedilen gerçek sinyalleri, uygulamadaki aynı eşiklerle
 * tekrar oynatır ve sayılan tekrarın gerçek tekrar sayısına eşit olduğunu doğrular.
 *
 * Kayıt eklemek için: app/src/test/resources/recordings/README.md
 * Dosya adı: <hareket>_<gerçek tekrar>reps[_not].csv   ör. squat_10reps_yavas.csv, plank_0reps.csv
 */
class RecordedSessionsTest {

    private val nameRegex = Regex("""^([a-z_]+?)_(\d+)reps.*\.csv$""")

    private fun recordings(): List<File> {
        val dir = javaClass.classLoader?.getResource("recordings")?.toURI()?.let(::File) ?: return emptyList()
        return dir.listFiles { f -> f.name.endsWith(".csv") }?.sortedBy { it.name } ?: emptyList()
    }

    @Test
    fun recordedSessions_countMatchesRealReps() {
        val files = recordings()
        assumeTrue("Henüz kayıt yok (app/src/test/resources/recordings/)", files.isNotEmpty())

        val failures = mutableListOf<String>()
        for (file in files) {
            val match = nameRegex.matchEntire(file.name)
            if (match == null) {
                failures += "${file.name}: dosya adı <hareket>_<N>reps.csv biçiminde olmalı"
                continue
            }
            val type = runCatching { ExerciseType.valueOf(match.groupValues[1].uppercase()) }.getOrNull()
            if (type == null) {
                failures += "${file.name}: '${match.groupValues[1]}' bilinmeyen hareket"
                continue
            }
            val analyzer = type.createAnalyzer() as? RepExerciseAnalyzer ?: continue // Plank: süreli

            val expected = match.groupValues[2].toInt()
            val counted = replay(analyzer.counter, SignalCsv.parse(file.readLines().asSequence()))
            if (counted != expected) failures += "${file.name}: beklenen $expected, sayılan $counted"
        }
        assertTrue(failures.joinToString("\n"), failures.isEmpty())
    }

    /** Kayıt formatının uygulamanın yazdığı formatla birebir okunabildiğini doğrular. */
    @Test
    fun csvFormat_roundTrip_andReplay() {
        val rows = mutableListOf<SignalCsv.Row>()
        var t = 0L
        fun add(v: Double?) { rows += SignalCsv.Row(t, listOf(v, null), "X", false, false); t += 66 }
        repeat(20) { add(175.0) }
        repeat(5) {
            repeat(10) { add(90.0) }
            repeat(3) { add(null) }   // kısa görünmezlik
            repeat(10) { add(175.0) }
        }
        val csv = (listOf(SignalCsv.HEADER) + rows.map(SignalCsv::formatRow)).joinToString("\n")
        val parsed = SignalCsv.parse(csv.lineSequence())

        assertEquals(rows.size, parsed.size)
        assertEquals(5, replay(SquatAnalyzer().counter, parsed))
    }

    private fun replay(counter: RepTracker, rows: List<SignalCsv.Row>): Int =
        rows.count { counter.update(it.values, it.timestampMs).repCompleted }
}
