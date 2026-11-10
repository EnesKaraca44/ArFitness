package com.example.arfitness.analysis

/**
 * Analizörün kullandığı sayaç arayüzü: tek kanallı ([RepCounter]) ya da
 * sol/sağ kolu ayrı izleyen ([PairedRepCounter]).
 */
interface RepTracker {
    /** @param values kanal başına sinyal (0 = sol/tek, 1 = sağ); güvenilmeyen kanal null */
    fun update(values: List<Double?>, timeMs: Long): RepCounter.Update
}

/**
 * Histerezisli tekrar sayacı (saf Kotlin, Android bağımlılığı yok, JVM'de test edilebilir).
 *
 * Sinyal (genelde bir eklem açısı) iki bölge arasında gidip geldiğinde 1 tekrar sayar:
 *
 *     başlangıç bölgesi  →  hedef bölge  →  başlangıç bölgesi   = 1 tekrar
 *
 * - `restThreshold > peakThreshold` ise başlangıç bölgesi üsttedir
 *   (ör. squat: diz > 160° başlangıç, diz < 110° hedef).
 * - `restThreshold < peakThreshold` ise başlangıç bölgesi alttadır
 *   (ör. cable pushdown: dirsek < 95° başlangıç, dirsek > 155° hedef).
 *
 * İki eşik arasındaki boşluk histerezis görevi görür: eşik çevresindeki titreşim tekrar üretemez.
 *
 * Eski DTW sayacından farkları:
 * - Hareketsiz durmak hiçbir zaman tekrar saymaz; hedef bölgeye girip geri dönmek şarttır.
 * - Sayım kare sayısına değil zamana bağlıdır, telefonun fps'inden bağımsızdır.
 * - Kullanıcı başlangıç pozisyonunu [readyHoldMs] boyunca tutmadan sayım başlamaz.
 * - Güvenilir olmayan karelerde (`null`) durum dondurulur; uzun kesintide yarım tekrar iptal edilir.
 */
class RepCounter(
    private val restThreshold: Double,
    private val peakThreshold: Double,
    /** Bir bölgeye girildi sayılması için art arda gereken kare sayısı (tek karelik sıçramaları eler). */
    private val minDwellFrames: Int = 2,
    /** Bundan kısa süren tekrarlar gürültü kabul edilir. */
    private val minRepDurationMs: Long = 500,
    /** Sayım başlamadan önce başlangıç pozisyonunda kalınması gereken süre. */
    private val readyHoldMs: Long = 800,
    /** Bu süreden uzun veri kesintisi olursa devam eden tekrar iptal edilir. */
    private val maxGapMs: Long = 1000,
) : RepTracker {
    enum class Phase {
        /** Başlangıç pozisyonu henüz yeterince tutulmadı; sayım kapalı. */
        NOT_READY,
        /** Kesinti sonrası: tekrar başlamadan önce başlangıç bölgesine dönülmesi bekleniyor. */
        RECOVERING,
        /** Başlangıç bölgesinde, hazır. */
        AT_REST,
        /** Başlangıç bölgesinden çıktı, hedefe gidiyor. */
        MOVING,
        /** Hedef bölgeye ulaştı, başlangıca dönüş bekleniyor. */
        PEAK_REACHED,
    }

    data class Update(
        val phase: Phase,
        /** Bu karede bir tekrar tamamlandı. */
        val repCompleted: Boolean = false,
        /** Bu karede yarım bir tekrar bitti (hedefe ulaşmadan başlangıca dönüldü). */
        val partialRep: Boolean = false,
        /** Tamamlanan tekrarın başladığı an (yalnızca [repCompleted] true iken dolu). */
        val repStartedAtMs: Long? = null,
        /**
         * Tamamlanan tekrarın normalize edilmiş eğrisi (yalnızca [repCompleted] true iken dolu):
         * 0 = başlangıç eşiği, 1 = hedef eşiği. Form puanı ([RepScorer]) bundan hesaplanır.
         */
        val repShape: List<Double>? = null,
    ) {
        val isReady: Boolean get() = phase != Phase.NOT_READY
    }

    private val restIsHigh = restThreshold > peakThreshold
    private val midpoint = (restThreshold + peakThreshold) / 2.0

    var phase: Phase = Phase.NOT_READY
        private set

    private var restFrames = 0
    private var peakFrames = 0
    private var restEnteredAtMs: Long? = null
    private var repStartedAtMs = 0L
    private var lastValidAtMs: Long? = null
    private var passedMidpoint = false
    private var lastRestValue: Double? = null
    private val repShape = ArrayList<Double>()

    /** Eşiklere göre 0 (başlangıç) – 1 (hedef) ölçeği; iki yönde de çalışır. */
    private fun normalize(v: Double) = (v - restThreshold) / (peakThreshold - restThreshold)

    private fun appendShape(v: Double) {
        if (repShape.size < MAX_SHAPE_SAMPLES) repShape.add(normalize(v))
    }

    private fun inRest(v: Double) = if (restIsHigh) v >= restThreshold else v <= restThreshold
    private fun inPeak(v: Double) = if (restIsHigh) v <= peakThreshold else v >= peakThreshold
    private fun pastMidpoint(v: Double) = if (restIsHigh) v <= midpoint else v >= midpoint

    override fun update(values: List<Double?>, timeMs: Long): Update = update(values.firstOrNull(), timeMs)

    /**
     * @param value bu karedeki sinyal; kare güvenilir değilse null (durum dondurulur)
     * @param timeMs karenin zaman damgası (ms, monoton artan)
     */
    fun update(value: Double?, timeMs: Long): Update {
        if (value == null || value.isNaN()) {
            restFrames = 0
            peakFrames = 0
            restEnteredAtMs = null
            return Update(phase)
        }

        val last = lastValidAtMs
        lastValidAtMs = timeMs
        if (last != null && timeMs - last > maxGapMs) {
            onLongGap()
        }

        if (inRest(value)) restFrames++ else restFrames = 0
        if (inPeak(value)) peakFrames++ else peakFrames = 0
        if (pastMidpoint(value)) passedMidpoint = true

        val restStable = restFrames >= minDwellFrames
        val peakStable = peakFrames >= minDwellFrames

        if (phase == Phase.MOVING || phase == Phase.PEAK_REACHED) appendShape(value)

        when (phase) {
            Phase.NOT_READY -> {
                if (restStable) {
                    val since = restEnteredAtMs ?: timeMs.also { restEnteredAtMs = it }
                    if (timeMs - since >= readyHoldMs) enterRest()
                } else if (restFrames == 0) {
                    restEnteredAtMs = null
                }
            }

            Phase.RECOVERING -> if (restStable) enterRest()

            Phase.AT_REST -> if (restFrames == 0) {
                phase = Phase.MOVING
                repStartedAtMs = timeMs
                passedMidpoint = pastMidpoint(value)
                repShape.clear()
                lastRestValue?.let { repShape.add(normalize(it)) }
                appendShape(value)
                if (peakStable) phase = Phase.PEAK_REACHED
            } else {
                lastRestValue = value
            }

            Phase.MOVING -> when {
                peakStable -> phase = Phase.PEAK_REACHED
                restStable -> {
                    val partial = passedMidpoint
                    enterRest()
                    return Update(phase, partialRep = partial)
                }
            }

            Phase.PEAK_REACHED -> if (restStable) {
                val longEnough = timeMs - repStartedAtMs >= minRepDurationMs
                enterRest()
                return Update(
                    phase,
                    repCompleted = longEnough,
                    repStartedAtMs = if (longEnough) repStartedAtMs else null,
                    repShape = if (longEnough) repShape.toList() else null,
                )
            }
        }
        return Update(phase)
    }

    fun reset() {
        phase = Phase.NOT_READY
        restFrames = 0
        peakFrames = 0
        restEnteredAtMs = null
        lastValidAtMs = null
        passedMidpoint = false
        lastRestValue = null
        repShape.clear()
    }

    private fun enterRest() {
        phase = Phase.AT_REST
        restEnteredAtMs = null
        passedMidpoint = false
        lastRestValue = null
    }

    private companion object {
        /** ~20 sn @15 fps; dipte çok uzun beklenirse bellek sınırsız büyümesin. */
        const val MAX_SHAPE_SAMPLES = 300
    }

    private fun onLongGap() {
        restFrames = 0
        peakFrames = 0
        restEnteredAtMs = null
        if (phase == Phase.MOVING || phase == Phase.PEAK_REACHED || phase == Phase.AT_REST) {
            phase = Phase.RECOVERING
        }
    }
}
