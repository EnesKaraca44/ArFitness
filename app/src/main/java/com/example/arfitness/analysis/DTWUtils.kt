package com.example.arfitness.analysis

import kotlin.math.abs
import kotlin.math.min

/**
 * Dynamic Time Warping (DTW) Algoritması
 * İki zaman serisinin (farklı hızlarda bile olsalar) birbirine olan şekil/patern benzerliğini hesaplar.
 */
object DTWUtils {

    /**
     * İki açısal zaman serisi arasındaki DTW mesafesini döndürür.
     * Mesafe ne kadar küçükse, iki hareket birbirine o kadar "karakter" olarak benziyor demektir.
     */
    fun computeDTWDistance(series1: List<Double>, series2: List<Double>): Double {
        val n = series1.size
        val m = series2.size

        if (n == 0 || m == 0) return Double.MAX_VALUE

        val dtw = Array(n + 1) { DoubleArray(m + 1) { Double.POSITIVE_INFINITY } }
        dtw[0][0] = 0.0

        for (i in 1..n) {
            for (j in 1..m) {
                // Maliyet: İki açı arasındaki mutlak fark
                val cost = abs(series1[i - 1] - series2[j - 1])
                
                // En düşük maliyetli geçmiş yolu bul (Ekleme, Silme, Eşleşme)
                val minPrev = min(dtw[i - 1][j], min(dtw[i][j - 1], dtw[i - 1][j - 1]))
                dtw[i][j] = cost + minPrev
            }
        }
        
        // Uzunluğa bölerek normalize ediyoruz (farklı uzunluktaki diziler adil karşılaştırılsın diye)
        return dtw[n][m] / (n + m)
    }
}
