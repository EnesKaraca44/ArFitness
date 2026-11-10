package com.example.arfitness.debug

import android.content.Context
import android.util.Log
import com.example.arfitness.analysis.SignalCsv
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors

/**
 * Yalnızca debug derlemelerinde: her karede sayaca giden sinyali CSV'ye yazar.
 *
 * Dosyalar uygulamaya özel klasöre yazılır (izin gerekmez):
 *   /sdcard/Android/data/<paket>/files/recordings/<hareket>_<tarih>.csv
 * Android Studio > Device Explorer ile ya da şu komutla alınabilir:
 *   adb pull /sdcard/Android/data/com.enes.arfitness/files/recordings/
 *
 * Kaydı, gerçek tekrar sayısını içerecek şekilde yeniden adlandırıp (ör. `squat_10reps_ali.csv`)
 * `app/src/test/resources/recordings/` klasörüne koyunca RecordedSessionsTest onu tekrar oynatır.
 */
class SignalRecorder(context: Context, exerciseKey: String) {

    private val io = Executors.newSingleThreadExecutor()
    private var writer: BufferedWriter? = null

    val file: File?

    init {
        val dir = context.getExternalFilesDir("recordings")
        file = dir?.let {
            it.mkdirs()
            val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            File(it, "${exerciseKey}_$stamp.csv")
        }
        io.execute {
            try {
                writer = file?.let { BufferedWriter(FileWriter(it)) }?.apply {
                    write(SignalCsv.HEADER)
                    newLine()
                }
                Log.d(TAG, "Sinyal kaydı: ${file?.absolutePath}")
            } catch (e: Exception) {
                Log.e(TAG, "Kayıt dosyası açılamadı", e)
            }
        }
    }

    @Volatile
    private var closed = false

    fun record(row: SignalCsv.Row) {
        // Kapanıştan sonra gelen geç kareler (ML Kit sonuçları asenkron) yok sayılır
        if (closed) return
        val line = SignalCsv.formatRow(row)
        io.execute {
            try {
                writer?.apply { write(line); newLine() }
            } catch (e: Exception) {
                Log.e(TAG, "Kayıt yazılamadı", e)
            }
        }
    }

    fun close() {
        if (closed) return
        closed = true
        io.execute {
            try {
                writer?.close()
            } catch (e: Exception) {
                Log.e(TAG, "Kayıt kapatılamadı", e)
            }
            writer = null
        }
        io.shutdown()
    }

    private companion object {
        const val TAG = "SignalRecorder"
    }
}
