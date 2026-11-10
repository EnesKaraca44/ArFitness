package com.example.arfitness.analysis

import java.util.Locale

/**
 * Debug derlemelerinde kaydedilen sinyal dosyalarının (CSV) formatı.
 * Kayıt (uygulama) ve tekrar oynatma (unit test) aynı kodu kullanır, böylece format hiç ayrışmaz.
 *
 * Satır: karenin zamanı, sayaca giden 1–2 kanal (boş = o karede güvenilmez), sayaç durumu,
 * uygulamanın o karede tekrar sayıp saymadığı, Plank için doğru pozisyonda olup olmadığı.
 */
object SignalCsv {

    const val HEADER = "timestamp_ms,v0,v1,phase,rep_counted,is_holding"

    data class Row(
        val timestampMs: Long,
        val values: List<Double?>,
        val phase: String,
        val repCounted: Boolean,
        val isHolding: Boolean,
    )

    fun formatRow(row: Row): String {
        // Locale.US: Türkçe cihazda ondalık ayırıcı virgül olup CSV'yi bozmasın
        fun num(v: Double?) = v?.let { String.format(Locale.US, "%.4f", it) } ?: ""
        return listOf(
            row.timestampMs.toString(),
            num(row.values.getOrNull(0)),
            num(row.values.getOrNull(1)),
            row.phase,
            if (row.repCounted) "1" else "0",
            if (row.isHolding) "1" else "0",
        ).joinToString(",")
    }

    fun parse(lines: Sequence<String>): List<Row> = lines
        .map { it.trim() }
        .filter { it.isNotEmpty() && !it.startsWith("timestamp_ms") }
        .map { line ->
            val c = line.split(",")
            Row(
                timestampMs = c[0].toLong(),
                values = listOf(c.getOrNull(1)?.toDoubleOrNull(), c.getOrNull(2)?.toDoubleOrNull()),
                phase = c.getOrElse(3) { "" },
                repCounted = c.getOrNull(4) == "1",
                isHolding = c.getOrNull(5) == "1",
            )
        }
        .toList()
}
