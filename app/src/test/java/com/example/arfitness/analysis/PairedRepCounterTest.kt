package com.example.arfitness.analysis

import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.math.PI
import kotlin.math.cos

/**
 * İki kollu sayım: aynı anda yapılan curl 1 tekrar, sırayla (alternatif) yapılan curl kol başına 1 tekrar.
 */
class PairedRepCounterTest {

    private fun curlCounter() = PairedRepCounter(
        left = RepCounter(restThreshold = 145.0, peakThreshold = 70.0, minRepDurationMs = 600),
        right = RepCounter(restThreshold = 145.0, peakThreshold = 70.0, minRepDurationMs = 600),
    )

    /**
     * Her [periodMs] içinde, [offsetMs]'ten başlayıp [durMs] süren bir curl (170° → 45° → 170°);
     * geri kalan zamanda kol aşağıda (170°). İlk [holdMs] boyunca kol aşağıda bekler.
     */
    private fun arm(periodMs: Long, offsetMs: Long, durMs: Long, holdMs: Long = 1500): (Long) -> Double = { t ->
        if (t < holdMs) 170.0 else {
            val local = ((t - holdMs) % periodMs) - offsetMs
            if (local < 0 || local >= durMs) 170.0
            else 170.0 - 125.0 * (0.5 - 0.5 * cos(2 * PI * local / durMs))
        }
    }

    private fun run(
        counter: RepTracker,
        seconds: Double,
        fps: Int = 15,
        left: (Long) -> Double?,
        right: (Long) -> Double?,
    ): Int {
        var reps = 0
        for (i in 0 until (seconds * fps).toInt()) {
            val t = i * 1000L / fps
            if (counter.update(listOf(left(t), right(t)), t).repCompleted) reps++
        }
        return reps
    }

    @Test
    fun bothArmsTogether_countOnce() {
        val signal = arm(periodMs = 3000, offsetMs = 0, durMs = 2500)
        assertEquals(10, run(curlCounter(), 1.5 + 30.0, left = signal, right = signal))
    }

    @Test
    fun bothArmsTogether_withLaggingArm_countOnce() {
        // Yorulan kol 400 ms geride kalıyor
        val leftArm = arm(periodMs = 3000, offsetMs = 0, durMs = 2200)
        val rightArm = arm(periodMs = 3000, offsetMs = 400, durMs = 2200)
        assertEquals(10, run(curlCounter(), 1.5 + 30.0, left = leftArm, right = rightArm))
    }

    @Test
    fun alternatingArms_countEachArm() {
        // Her 3 sn'de önce sol (0–1.5 sn), sonra sağ (1.5–3.0 sn)
        val leftArm = arm(periodMs = 3000, offsetMs = 0, durMs = 1400)
        val rightArm = arm(periodMs = 3000, offsetMs = 1500, durMs = 1400)
        assertEquals(20, run(curlCounter(), 1.5 + 30.0, left = leftArm, right = rightArm))
    }

    @Test
    fun alternatingArms_withSomeOverlap_stillCountEachArm() {
        // Sağ kol, sol kol daha inerken kalkmaya başlıyor (~%20 örtüşme)
        val leftArm = arm(periodMs = 3000, offsetMs = 0, durMs = 1800)
        val rightArm = arm(periodMs = 3000, offsetMs = 1000, durMs = 1800)
        assertEquals(20, run(curlCounter(), 1.5 + 30.0, left = leftArm, right = rightArm))
    }

    @Test
    fun singleArm_otherNotVisible() {
        val leftArm = arm(periodMs = 3000, offsetMs = 0, durMs = 2500)
        assertEquals(10, run(curlCounter(), 1.5 + 30.0, left = leftArm, right = { null }))
    }

    @Test
    fun singleArm_otherHangingStill() {
        val rightArm = arm(periodMs = 3000, offsetMs = 0, durMs = 2500)
        assertEquals(10, run(curlCounter(), 1.5 + 30.0, left = { 170.0 }, right = rightArm))
    }

    @Test
    fun bothArmsStill_countNothing() {
        assertEquals(0, run(curlCounter(), 30.0, left = { 170.0 }, right = { 168.0 }))
    }
}
