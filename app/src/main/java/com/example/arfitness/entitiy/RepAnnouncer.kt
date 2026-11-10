package com.example.arfitness.entitiy

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.Locale

/**
 * Tekrarları sesli ("bir", "iki", ...) ve kısa bir titreşimle bildirir.
 *
 * - Seslendirme cihazın TTS motoruyla, uygulamanın dilinde yapılır. Bu dil cihazda yoksa kısa bir bip çalınır.
 * - Kullanıcı kulaklıkla müzik dinliyorsa müzik konuşma sırasında kısılır (audio focus "duck"), sonra geri gelir.
 * - Titreşim her tekrarda çalışır. Telefon genelde 2–3 m ötede durduğu için titreşim çoğunlukla
 *   zemin üzerinde duyulan bir "tık" işlevi görür; asıl bildirim sestir.
 *
 * @param voiceEnabled sesli sayım açık mı (ekrandaki hoparlör butonu); titreşim bundan bağımsızdır
 */
class RepAnnouncer(context: Context, locale: Locale, var voiceEnabled: Boolean) {

    private val appContext = context.applicationContext
    private val audioManager = appContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        (appContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        appContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    private val speechAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
        .build()

    private val focusRequest: AudioFocusRequest? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
            .setAudioAttributes(speechAttributes)
            .build()
    } else null

    private var tts: TextToSpeech? = null
    @Volatile
    private var ttsReady = false
    private var toneGenerator: ToneGenerator? = null

    init {
        tts = TextToSpeech(appContext) { status ->
            val engine = tts
            if (status == TextToSpeech.SUCCESS && engine != null) {
                val result = engine.setLanguage(locale)
                ttsReady = result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED
                engine.setAudioAttributes(speechAttributes)
                engine.setSpeechRate(1.1f)
                engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) = Unit
                    override fun onDone(utteranceId: String?) = abandonFocus()
                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) = abandonFocus()
                })
                if (!ttsReady) Log.w(TAG, "TTS dili desteklenmiyor: $locale, bip kullanılacak")
            } else {
                Log.w(TAG, "TTS başlatılamadı (durum $status), bip kullanılacak")
            }
        }
    }

    /** Tamamlanan tekrarı bildirir: titreşim + sayının seslendirilmesi. */
    fun announceRep(count: Int) {
        vibrate()
        speak(count.toString())
    }

    /** Süreli hareketlerde (Plank) ara bildirim, ör. "30 saniye". */
    fun announceText(text: String) {
        vibrate()
        speak(text)
    }

    private fun speak(text: String) {
        if (!voiceEnabled) return
        val engine = tts
        if (ttsReady && engine != null) {
            requestFocus()
            // QUEUE_FLUSH: hızlı tekrarlarda sayılar birikip geride kalmasın, hep son sayı söylensin
            engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, UTTERANCE_ID)
        } else {
            beep()
        }
    }

    private fun vibrate() {
        val v = vibrator ?: return
        if (!v.hasVibrator()) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(VIBRATION_MS, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            v.vibrate(VIBRATION_MS)
        }
    }

    private fun beep() {
        try {
            val generator = toneGenerator ?: ToneGenerator(AudioManager.STREAM_MUSIC, 80).also { toneGenerator = it }
            generator.startTone(ToneGenerator.TONE_PROP_BEEP, 150)
        } catch (e: RuntimeException) {
            Log.w(TAG, "Bip çalınamadı", e)
        }
    }

    private fun requestFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            focusRequest?.let { audioManager.requestAudioFocus(it) }
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
        }
    }

    private fun abandonFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            focusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(null)
        }
    }

    fun release() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        ttsReady = false
        toneGenerator?.release()
        toneGenerator = null
        abandonFocus()
    }

    private companion object {
        const val TAG = "RepAnnouncer"
        const val UTTERANCE_ID = "rep"
        const val VIBRATION_MS = 60L
    }
}
