package com.example.arfitness.analysis

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt

/**
 * Tamamlanan bir tekrarın form puanı (0–100).
 *
 * DTW artık sayım için değil, burada kullanılır: tekrar sayıldıktan sonra o tekrarın eğrisi
 * ideal bir tekrar eğrisiyle karşılaştırılır. İniş ve kalkış ayrı ayrı zamanda normalize edilir
 * ve DTW küçük hız farklarını esnetir; böylece tempo değil hareketin ŞEKLİ puanlanır:
 *  - tam hareket açıklığı (hedefin biraz ötesine inmek) → yüksek puan
 *  - hedefe zar zor değip dönmek, yolda duraksamak/sekmek → daha düşük puan
 *
 * Puanın diğer yarısı, tekrar sırasında form uyarısı verilen karelerin oranından gelir
 * (ör. şınavda kalça düşüyor, curl'de dirsek savruluyor).
 */
object RepScorer {

    private const val POINTS = 32
    /** İdeal tekrarın hedef eşiğinin ne kadar ötesine gittiği (1.0 = tam eşikte). */
    private const val IDEAL_DEPTH = 1.15
    /**
     * Ölçek (simülasyonla ayarlandı): ideal bir tekrar bile eşik kırpması yüzünden ~0.012 mesafe
     * verir. [BASELINE_DISTANCE] altı tam puan, üstündeki her [DISTANCE_RANGE] 100 puan düşürür.
     *   derin squat ≈ 97, paralel ≈ 90, hızlı iniş + yavaş kalkış ≈ 92, sekme ≈ 80–90,
     *   hedefe zar zor değmek ≈ 84, dipte 1 sn duraksama ≈ 82
     */
    private const val BASELINE_DISTANCE = 0.010
    private const val DISTANCE_RANGE = 0.05

    /** İdeal tekrar: başlangıçtan (0) hedefin biraz ötesine yumuşak gidiş ve aynı yoldan dönüş. */
    private val template: List<Double> = splitResample(
        List(201) { i -> IDEAL_DEPTH * (0.5 - 0.5 * cos(2 * PI * i / 200)) }, POINTS
    )

    /**
     * @param shape [RepCounter.Update.repShape]: 0 = başlangıç eşiği, 1 = hedef eşiği
     */
    fun shapeScore(shape: List<Double>): Int {
        if (shape.size < 3) return 0
        // Başlangıç bölgesinin ötesi (ör. dizi tam kilitlemek) ve idealden derin inmek cezalandırılmaz
        val clipped = shape.map { it.coerceIn(0.0, IDEAL_DEPTH) }
        val distance = DTWUtils.computeDTWDistance(splitResample(clipped, POINTS), template)
        val penalty = (distance - BASELINE_DISTANCE).coerceAtLeast(0.0) / DISTANCE_RANGE
        return (100.0 * (1.0 - penalty)).roundToInt().coerceIn(0, 100)
    }

    /**
     * @param warningFraction tekrar sırasında form uyarısı verilen karelerin oranı (0–1)
     */
    fun repScore(shape: List<Double>, warningFraction: Double): Int {
        val formPart = 100.0 * (1.0 - warningFraction.coerceIn(0.0, 1.0))
        return (0.5 * shapeScore(shape) + 0.5 * formPart).roundToInt()
    }

    /**
     * İniş ve kalkışı tepe noktasından ayırıp her birini [n]/2 noktaya ayrı ayrı örnekler.
     * Böylece "hızlı iniş + yavaş kalkış" gibi tempo farkları şekil hatası sayılmaz.
     * Tepe, en yüksek değere yakın bölgenin (dipte bekleme) ortası alınır.
     */
    internal fun splitResample(values: List<Double>, n: Int): List<Double> {
        val max = values.max()
        val nearPeak = values.indices.filter { values[it] >= max - 0.05 }
        val peak = nearPeak[nearPeak.size / 2].coerceIn(1, values.size - 2)
        val half = n / 2
        return resample(values.subList(0, peak + 1), half) + resample(values.subList(peak, values.size), n - half)
    }

    /** Eğriyi zamanda eşit aralıklı [n] noktaya doğrusal olarak yeniden örnekler. */
    internal fun resample(values: List<Double>, n: Int): List<Double> {
        if (values.size == 1) return List(n) { values[0] }
        return List(n) { i ->
            val pos = i.toDouble() * (values.size - 1) / (n - 1)
            val lo = pos.toInt().coerceAtMost(values.size - 2)
            val frac = pos - lo
            values[lo] * (1 - frac) + values[lo + 1] * frac
        }
    }
}
