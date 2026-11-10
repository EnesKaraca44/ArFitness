package com.example.arfitness.analysis

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.PI
import kotlin.math.cos
import kotlin.random.Random

/**
 * RepCounter'ı sentetik açı sinyalleriyle test eder. Senaryolar, eski DTW sayacının
 * hatalı saydığı durumlardır (bkz. analiz: hareketsiz dururken 30 sn'de 19 squat).
 */
class RepCounterTest {

    private fun squatCounter() = RepCounter(restThreshold = 160.0, peakThreshold = 110.0, minRepDurationMs = 700)

    /** [fn] fonksiyonunu [seconds] boyunca [fps] hızında örnekleyip sayılan tekrarları döndürür. */
    private fun run(
        counter: RepCounter,
        seconds: Double,
        fps: Int,
        startMs: Long = 0,
        fn: (tMs: Long) -> Double?,
    ): Pair<Int, Int> {
        var reps = 0
        var partials = 0
        val frames = (seconds * fps).toInt()
        for (i in 0 until frames) {
            val t = startMs + (i * 1000L / fps)
            val u = counter.update(fn(t), t)
            if (u.repCompleted) reps++
            if (u.partialRep) partials++
        }
        return reps to partials
    }

    /** top → bottom → top kosinüs dalgası; her periyot 1 tekrar. */
    private fun reps(periodMs: Long, top: Double, bottom: Double, depth: Double = 1.0): (Long) -> Double = { t ->
        val ph = (t % periodMs).toDouble() / periodMs
        top - (top - bottom) * depth * (0.5 - 0.5 * cos(2 * PI * ph))
    }

    /** Önce [holdMs] boyunca başlangıçta bekle, sonra [inner] sinyalini uygula. */
    private fun afterHold(holdMs: Long, restValue: Double, inner: (Long) -> Double): (Long) -> Double = { t ->
        if (t < holdMs) restValue else inner(t - holdMs)
    }

    @Test
    fun standingStill_countsNothing() {
        val (reps, _) = run(squatCounter(), 30.0, 15) { 175.0 }
        assertEquals(0, reps)
    }

    @Test
    fun holdingBottom_countsNothing() {
        val (reps, _) = run(squatCounter(), 30.0, 15) { 85.0 }
        assertEquals(0, reps)
    }

    @Test
    fun tenRealReps_15fps() {
        val signal = afterHold(1500, 175.0, reps(3000, 175.0, 85.0))
        val (reps, _) = run(squatCounter(), 1.5 + 30.0, 15, fn = signal)
        assertEquals(10, reps)
    }

    @Test
    fun fifteenRealReps_lowFps8() {
        val signal = afterHold(1500, 175.0, reps(2000, 175.0, 85.0))
        val (reps, _) = run(squatCounter(), 1.5 + 30.0, 8, fn = signal)
        assertEquals(15, reps)
    }

    @Test
    fun fastReps_oneSecondEach() {
        val counter = RepCounter(150.0, 100.0, minRepDurationMs = 500)
        val signal = afterHold(1500, 170.0, reps(1000, 170.0, 75.0))
        val (reps, _) = run(counter, 1.5 + 20.5, 15, fn = signal)
        assertEquals(20, reps)
    }

    @Test
    fun halfReps_notCounted_butFlaggedPartial() {
        // 175° → ~121° → 175°: orta noktayı (135°) geçer ama hedefe (110°) inmez
        val signal = afterHold(1500, 175.0, reps(3000, 175.0, 85.0, depth = 0.6))
        val (reps, partials) = run(squatCounter(), 1.5 + 30.0, 15, fn = signal)
        assertEquals(0, reps)
        assertEquals(10, partials)
    }

    @Test
    fun noiseAroundRest_countsNothing() {
        val rnd = Random(42)
        val (reps, partials) = run(squatCounter(), 60.0, 15) { 165.0 + rnd.nextDouble(-15.0, 15.0) }
        assertEquals(0, reps)
        assertEquals(0, partials)
    }

    @Test
    fun singleFrameSpike_countsNothing() {
        val counter = squatCounter()
        val (reps, _) = run(counter, 10.0, 15) { t -> if (t in 3000L..3010L) 80.0 else 175.0 }
        assertEquals(0, reps)
    }

    @Test
    fun twoFrameSpike_tooFastToBeARep() {
        // 2 kare boyunca hedef bölgeye sıçrama: bölgeye girmiş sayılır ama
        // tekrar süresi (~200 ms) minRepDuration'dan kısa olduğu için sayılmaz.
        val counter = squatCounter()
        val (reps, _) = run(counter, 10.0, 15) { t -> if (t in 3000L..3066L) 80.0 else 175.0 }
        assertEquals(0, reps)
    }

    @Test
    fun movementBeforeReady_notCounted() {
        // Başlangıç pozisyonunda hiç beklemeden hemen hareket: ilk tekrar sayılmaz,
        // çünkü sayaç önce başlangıç pozisyonunun tutulmasını bekler.
        val counter = squatCounter()
        val (reps, _) = run(counter, 1.0, 15, fn = reps(3000, 120.0, 85.0))
        assertEquals(0, reps)
        assertFalse(counter.update(120.0, 1100).isReady)
    }

    @Test
    fun becomesReady_afterHoldingStartPosition() {
        val counter = squatCounter()
        var ready = false
        for (i in 0..15) {
            ready = counter.update(175.0, i * 66L).isReady
        }
        assertTrue(ready)
    }

    @Test
    fun restBelowDirection_cablePushdown() {
        // Başlangıç dirsek bükük (<95°), hedef kol düz (>155°)
        val counter = RepCounter(restThreshold = 95.0, peakThreshold = 155.0)
        val signal = afterHold(1500, 80.0, reps(2500, 80.0, 170.0))
        val (reps, _) = run(counter, 1.5 + 25.0, 15, fn = signal)
        assertEquals(10, reps)
    }

    @Test
    fun longGapMidRep_abortsRep() {
        val counter = squatCounter()
        var t = 0L
        repeat(20) { counter.update(175.0, t); t += 66 }          // hazır
        repeat(5) { counter.update(130.0, t); t += 66 }           // inmeye başla
        repeat(5) { counter.update(90.0, t); t += 66 }            // dip
        repeat(30) { counter.update(null, t); t += 66 }           // ~2 sn kayıp (kadrajdan çıktı)
        var counted = false
        repeat(10) { if (counter.update(175.0, t).repCompleted) counted = true; t += 66 }
        assertFalse(counted)

        // Sonraki tam tekrar normal sayılmalı
        repeat(12) { counter.update(90.0, t); t += 66 }
        repeat(10) { if (counter.update(175.0, t).repCompleted) counted = true; t += 66 }
        assertTrue(counted)
    }

    @Test
    fun shortGap_doesNotBreakRep() {
        val counter = squatCounter()
        var t = 0L
        repeat(20) { counter.update(175.0, t); t += 66 }
        repeat(8) { counter.update(90.0, t); t += 66 }
        repeat(4) { counter.update(null, t); t += 66 }            // ~260 ms titreme
        var counted = false
        repeat(10) { if (counter.update(175.0, t).repCompleted) counted = true; t += 66 }
        assertTrue(counted)
    }
}
