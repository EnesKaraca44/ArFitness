package com.example.arfitness.analysis

import com.google.mlkit.vision.pose.Pose
import kotlin.math.roundToInt

/**
 * Tekrar sayılan tüm hareketlerin ortak iskeleti.
 *
 * Alt sınıf yalnızca iki şey yapar:
 *  1. [read]: pozdan sinyali çıkarır (ör. diz açısı; kol hareketlerinde sol/sağ ayrı) ve varsa form uyarısı verir.
 *  2. [counter]: o sinyal için başlangıç/hedef eşiklerini tanımlar.
 *
 * Sayım, başlangıç pozisyonu kapısı, yarım tekrar uyarısı ve mesaj önceliği burada tek yerde yapılır.
 */
abstract class RepExerciseAnalyzer : ExerciseAnalyzer() {

    /** `internal`: kayıtlı sinyalleri tekrar oynatan testler aynı eşiklerle sayabilsin diye. */
    internal abstract val counter: RepTracker

    /** Sayım başlamadan önce kullanıcıdan beklenen başlangıç pozisyonu. */
    protected abstract val readyMessage: String
    /** Başlangıç pozisyonundayken: hareketi başlat. */
    protected abstract val goMessage: String
    /** Hedef noktaya ulaşıldı: başlangıca dön. */
    protected abstract val returnMessage: String
    /** Hedefe ulaşmadan geri dönüldü. */
    protected abstract val partialMessage: String

    protected var repCount = 0
        private set

    /** Pozdan okunan sinyal. */
    protected class Reading private constructor(
        val values: List<Double?>,
        val message: String?,
        val isWarning: Boolean,
    ) {
        val isAvailable: Boolean get() = values.any { it != null }

        companion object {
            /** Tek kanallı sinyal; [formWarning] varsa kart kırmızı gösterilir (sayımı etkilemez). */
            fun of(value: Double, formWarning: String? = null) =
                Reading(listOf(value), formWarning, formWarning != null)

            /** Sol/sağ kol ayrı sinyal ([PairedRepCounter] için); görünmeyen kol null. */
            fun pair(left: Double?, right: Double?, formWarning: String? = null) =
                Reading(listOf(left, right), formWarning, formWarning != null)

            /** Bu karede sinyal okunamıyor (görünmüyor ya da yanlış pozisyon); sayaç dondurulur. */
            fun unavailable(message: String) = Reading(emptyList(), message, true)
        }
    }

    protected abstract fun read(pose: Pose): Reading

    private var stickyMessage: String? = null
    private var stickyIsWarning = false
    private var stickyUntilMs = 0L

    private var lastValues: List<Double?> = emptyList()
    private var lastPhase: RepCounter.Phase = RepCounter.Phase.NOT_READY

    // Form puanı: tekrar boyunca form uyarısı verilen karelerin oranı + DTW şekil puanı
    private var repFrames = 0
    private var repWarningFrames = 0
    private val repScores = mutableListOf<Int>()

    override val debugState: DebugState
        get() = DebugState(lastValues, lastPhase.name)

    override val sessionFormScore: Int?
        get() = if (repScores.isEmpty()) null else repScores.average().roundToInt()

    final override fun analyze(pose: Pose, timestampMs: Long): Feedback {
        val reading = read(pose)
        val update = counter.update(reading.values, timestampMs)
        lastValues = reading.values
        lastPhase = update.phase

        val inRep = update.phase == RepCounter.Phase.MOVING || update.phase == RepCounter.Phase.PEAK_REACHED
        if (inRep && reading.isAvailable) {
            repFrames++
            if (reading.isWarning) repWarningFrames++
        }

        if (update.repCompleted) {
            repCount++
            val warningFraction = if (repFrames == 0) 0.0 else repWarningFrames.toDouble() / repFrames
            val score = update.repShape?.let { RepScorer.repScore(it, warningFraction) }
            if (score != null) repScores += score
            resetRepStats()

            val message = if (score != null) "$repCount. tekrar! Form %$score" else "$repCount. tekrar!"
            showSticky(message, isWarning = false, timestampMs, REP_MESSAGE_MS)
            return Feedback(message, isRepCounted = true)
        }
        if (!inRep) resetRepStats()

        if (!reading.isAvailable) {
            return Feedback(reading.message, isWarning = reading.isWarning)
        }
        if (!update.isReady || update.phase == RepCounter.Phase.RECOVERING) {
            return Feedback(readyMessage)
        }
        if (update.partialRep) {
            showSticky(partialMessage, isWarning = true, timestampMs, PARTIAL_MESSAGE_MS)
        }
        if (timestampMs < stickyUntilMs) {
            return Feedback(stickyMessage, isWarning = stickyIsWarning)
        }
        if (reading.message != null) {
            return Feedback(reading.message, isWarning = reading.isWarning)
        }
        val guidance = when (update.phase) {
            RepCounter.Phase.PEAK_REACHED -> returnMessage
            else -> goMessage
        }
        return Feedback(guidance)
    }

    private fun resetRepStats() {
        repFrames = 0
        repWarningFrames = 0
    }

    private fun showSticky(message: String, isWarning: Boolean, nowMs: Long, durationMs: Long) {
        stickyMessage = message
        stickyIsWarning = isWarning
        stickyUntilMs = nowMs + durationMs
    }

    companion object {
        private const val REP_MESSAGE_MS = 1200L
        private const val PARTIAL_MESSAGE_MS = 1500L
    }
}
