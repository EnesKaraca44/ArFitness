package com.example.arfitness.analysis

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * DTW tabanlı form puanı: daha iyi tekrar daha yüksek puan almalı.
 * Tekrarlar gerçek akıştaki gibi RepCounter'dan geçirilerek üretilir.
 */
class RepScorerTest {

    /**
     * Squat sinyali (175° ayakta) → [bottom] → 175°; [bounce] kalkarken araya giren sekme (derece),
     * [pauseFrames] dipte bekleme. Dönen değer: sayaçtan çıkan tekrar eğrisi.
     */
    private fun squatShape(bottom: Double, bounce: Double = 0.0, pauseFrames: Int = 0, downFrames: Int = 15, upFrames: Int = 15): List<Double> {
        val counter = RepCounter(restThreshold = 160.0, peakThreshold = 110.0, minRepDurationMs = 300)
        var t = 0L
        var shape: List<Double>? = null
        fun feed(v: Double) {
            counter.update(v, t).repShape?.let { shape = it }
            t += 66
        }
        repeat(20) { feed(175.0) }
        for (k in 0..downFrames) feed(175 - (175 - bottom) * (0.5 - 0.5 * cos(PI * k / downFrames)))
        repeat(pauseFrames) { feed(bottom) }
        for (k in 1..upFrames) {
            val x = k.toDouble() / upFrames
            var v = bottom + (175 - bottom) * (0.5 - 0.5 * cos(PI * x))
            if (x in 0.3..0.6) v -= bounce * sin((x - 0.3) / 0.3 * PI)
            feed(v)
        }
        repeat(10) { feed(175.0) }
        return checkNotNull(shape) { "tekrar sayılmadı" }
    }

    @Test
    fun deepSmoothRep_scoresHigh() {
        assertTrue(RepScorer.shapeScore(squatShape(85.0)) >= 90)
    }

    @Test
    fun deeperIsBetter_thanBarelyReachingTarget() {
        assertTrue(RepScorer.shapeScore(squatShape(85.0)) > RepScorer.shapeScore(squatShape(108.0)))
    }

    @Test
    fun smoothIsBetter_thanBouncing() {
        assertTrue(RepScorer.shapeScore(squatShape(90.0)) > RepScorer.shapeScore(squatShape(90.0, bounce = 40.0)))
    }

    @Test
    fun tempoDifference_isToleratedByDtw() {
        // Hızlı iniş + yavaş kalkış, şekli bozmaz; puan düzgün tekrara yakın kalmalı
        val even = RepScorer.shapeScore(squatShape(90.0))
        val uneven = RepScorer.shapeScore(squatShape(90.0, downFrames = 6, upFrames = 24))
        assertTrue("eşit=$even, dengesiz=$uneven", even - uneven <= 10)
    }

    @Test
    fun flatShape_scoresZero() {
        assertEquals(0, RepScorer.shapeScore(List(20) { 0.0 }))
    }

    @Test
    fun formWarnings_lowerRepScore() {
        val shape = squatShape(85.0)
        val clean = RepScorer.repScore(shape, warningFraction = 0.0)
        val sloppy = RepScorer.repScore(shape, warningFraction = 0.6)
        assertTrue(clean >= 95)
        assertEquals(clean - 30, sloppy)
    }

    @Test
    fun resample_keepsEndpoints() {
        val r = RepScorer.resample(listOf(0.0, 1.0, 0.0), 5)
        assertEquals(listOf(0.0, 0.5, 1.0, 0.5, 0.0), r)
    }
}
