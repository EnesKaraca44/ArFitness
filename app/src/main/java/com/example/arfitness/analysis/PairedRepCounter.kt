package com.example.arfitness.analysis

import kotlin.math.max
import kotlin.math.min

/**
 * Sol ve sağ kolu ayrı [RepCounter]'larla izler; böylece hem iki kol aynı anda hem de
 * sırayla (alternatif) yapılan curl / raise / press doğru sayılır.
 *
 * İki kolun tamamladığı tekrarların zaman aralıkları büyük ölçüde örtüşüyorsa bu, iki kolun
 * birlikte yaptığı TEK tekrardır ve bir kez sayılır. Örtüşme az ya da hiç yoksa kollar sırayla
 * çalışıyordur ve her kol ayrı tekrar sayılır.
 *
 *   Aynı anda:  sol [0.0 sn – 2.0 sn], sağ [0.3 – 2.4]  → örtüşme %85  → 1 tekrar
 *   Alternatif: sol [0.0 – 1.5],       sağ [1.2 – 2.7]  → örtüşme %20  → 2 tekrar
 *
 * Tek bir kol görünmüyorsa (ya da hiç hareket etmiyorsa) diğer kol tek başına sayılır.
 */
class PairedRepCounter(
    private val left: RepCounter,
    private val right: RepCounter,
    /** İki kolun tekrarı, kısa olanın süresinin bu oranından fazla örtüşürse tek tekrar sayılır. */
    private val mergeOverlap: Double = 0.5,
) : RepTracker {

    private class ArmRep(val startMs: Long, val endMs: Long) {
        var merged = false
    }

    private var lastLeft: ArmRep? = null
    private var lastRight: ArmRep? = null

    override fun update(values: List<Double?>, timeMs: Long): RepCounter.Update {
        val l = left.update(values.getOrNull(0), timeMs)
        val r = right.update(values.getOrNull(1), timeMs)

        // Sayılan tekrarın form puanı, o tekrarı tamamlayan kolun eğrisinden hesaplanır
        var counted = false
        var shape: List<Double>? = null
        if (l.repCompleted && onArmRep(isLeft = true, l.repStartedAtMs ?: timeMs, timeMs)) {
            counted = true
            shape = l.repShape
        }
        if (r.repCompleted && onArmRep(isLeft = false, r.repStartedAtMs ?: timeMs, timeMs)) {
            counted = true
            shape = shape ?: r.repShape
        }

        return RepCounter.Update(
            phase = combine(l.phase, r.phase),
            repCompleted = counted,
            partialRep = !counted && (l.partialRep || r.partialRep),
            repShape = shape,
        )
    }

    /** @return bu kol tekrarı yeni bir tekrar olarak sayılmalı mı */
    private fun onArmRep(isLeft: Boolean, startMs: Long, endMs: Long): Boolean {
        val rep = ArmRep(startMs, endMs)
        val other = if (isLeft) lastRight else lastLeft
        val sameRep = other != null && !other.merged && overlapRatio(rep, other) >= mergeOverlap
        if (sameRep) {
            other!!.merged = true
            rep.merged = true
        }
        if (isLeft) lastLeft = rep else lastRight = rep
        return !sameRep
    }

    private fun overlapRatio(a: ArmRep, b: ArmRep): Double {
        val overlap = min(a.endMs, b.endMs) - max(a.startMs, b.startMs)
        if (overlap <= 0) return 0.0
        val shorter = max(1L, min(a.endMs - a.startMs, b.endMs - b.startMs))
        return overlap.toDouble() / shorter
    }

    /** Ekrandaki yönlendirme için en ileri aşamadaki kolun durumu kullanılır. */
    private fun combine(a: RepCounter.Phase, b: RepCounter.Phase): RepCounter.Phase =
        if (PHASE_ORDER.indexOf(a) >= PHASE_ORDER.indexOf(b)) a else b

    private companion object {
        val PHASE_ORDER = listOf(
            RepCounter.Phase.NOT_READY,
            RepCounter.Phase.RECOVERING,
            RepCounter.Phase.AT_REST,
            RepCounter.Phase.MOVING,
            RepCounter.Phase.PEAK_REACHED,
        )
    }
}
