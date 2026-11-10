package com.example.arfitness.viewmodel

import android.os.SystemClock
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Job
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arfitness.data.WorkoutSession
import com.example.arfitness.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

/** Ekrandaki geri bildirim kartının durumu: uyarıysa kart kırmızı gösterilir. */
data class FeedbackUi(val message: String, val isWarning: Boolean = false)

@HiltViewModel
class ExerciseViewModel @Inject constructor(private val repository: WorkoutRepository) : ViewModel() {

    // --- Tekrar Sayısı (LiveData olarak) ---
    // Sayaç LiveData'dan okunmaz: postValue asenkron olduğu için arka arkaya gelen iki
    // tekrarda `.value` henüz güncellenmemiş olabilir ve bir tekrar kaybolurdu.
    private val repCount = AtomicInteger(0)
    private val _tekrarSayisi = MutableLiveData<Int>(0)
    val tekrarSayisi: LiveData<Int> = _tekrarSayisi

    // --- Plank gibi süreli hareketler: doğru pozisyonda geçen süre ---
    private val _holdSeconds = MutableLiveData(0L)
    val holdSeconds: LiveData<Long> = _holdSeconds
    private var holdMs = 0L
    private var lastHoldFrameMs: Long? = null
    private var lastPostedHoldSeconds = 0L

    // --- Geri Bildirim: mesaj + uyarı mı? (LiveData olarak) ---
    private val _feedback = MutableLiveData(FeedbackUi("Harekete Başla!"))
    val feedback: LiveData<FeedbackUi> = _feedback

    // --- Zamanlayıcı (LiveData olarak) ---
    private val _timerValueString = MutableLiveData<String>("00:00")
    val timerValueString: LiveData<String> = _timerValueString

    // Toplam geçen süre (saniye cinsinden)
    private var elapsedSeconds: Long = 0L
    private var accumulatedElapsedMs: Long = 0L
    private var sessionStartedAtElapsedMs: Long? = null
    private var hasTimerStarted = false

    // Coroutine işini tutacak değişken. Bu, zamanlayıcıyı durdurmamızı sağlar.
    private var timerJob: Job? = null

    // Bu fonksiyonlar, analizörden gelen sonuçlara göre ViewModel'i güncelleyecek.
    fun onRepCounted() {
        _tekrarSayisi.postValue(repCount.incrementAndGet())
    }

    fun getRepCount(): Int = repCount.get()

    /**
     * Süreli hareketlerde her kare çağrılır. Süre yalnızca art arda gelen "doğru pozisyon"
     * kareleri arasında işler; kareler arası uzun boşluklar (kamera durdu vs.) sayılmaz.
     */
    fun onHoldFrame(isHolding: Boolean, timestampMs: Long) {
        val last = lastHoldFrameMs
        if (isHolding && last != null) {
            holdMs += (timestampMs - last).coerceIn(0L, MAX_HOLD_FRAME_GAP_MS)
        }
        lastHoldFrameMs = if (isHolding) timestampMs else null

        val seconds = holdMs / 1000L
        if (seconds != lastPostedHoldSeconds) {
            lastPostedHoldSeconds = seconds
            _holdSeconds.postValue(seconds)
        }
    }

    fun getHoldSeconds(): Long = holdMs / 1000L

    fun updateFeedback(message: String, isWarning: Boolean = false) {
        // Aynı mesajı tekrar tekrar göstermemek için kontrol
        val yeni = FeedbackUi(message, isWarning)
        if (_feedback.value != yeni) {
            _feedback.postValue(yeni)
        }
    }

    fun getElapsedSeconds(): Long {
        updateElapsedDuration()
        return elapsedSeconds
    }

    /**
     * @param isTimed süreli hareketlerde (Plank) [duration] doğru pozisyonda geçen tutma süresidir.
     * @param formScore analizörün ölçtüğü form puanı (tekrar başına DTW şekil puanı + form uyarıları);
     * puanlanacak bir şey yoksa null → 0 kaydedilir.
     */
    fun saveWorkout(exerciseName: String, reps: Int, duration: Long, isTimed: Boolean = false, formScore: Int? = null) {
        viewModelScope.launch {
            // Eskiden "85 + tekrar sayısı" gibi sabit bir formüldü; artık ölçülen puan kullanılıyor
            val calculatedScore = formScore ?: 0
            // Plank yaklaşık 4 kcal/dk
            val calculatedCalories = if (isTimed) (duration * 4 / 60).toInt() else (reps * 1.5).toInt()
            val session = WorkoutSession(
                exerciseName = exerciseName,
                repCount = reps,
                durationInSeconds = duration,
                date = System.currentTimeMillis(),
                formScore = calculatedScore,
                caloriesBurned = calculatedCalories
            )
            repository.saveWorkoutSession(session)
        }
    }

    fun reset() {
        stopTimer()
        repCount.set(0)
        _tekrarSayisi.postValue(0)
        holdMs = 0L
        lastHoldFrameMs = null
        lastPostedHoldSeconds = 0L
        _holdSeconds.postValue(0L)
        _feedback.postValue(FeedbackUi("Egzersize Başla!"))
        _timerValueString.postValue("00:00")
        elapsedSeconds = 0L
        accumulatedElapsedMs = 0L
        sessionStartedAtElapsedMs = null
        hasTimerStarted = false
    }

    fun startTimer() {
        if (hasTimerStarted) return
        hasTimerStarted = true
        elapsedSeconds = 0L
        accumulatedElapsedMs = 0L
        startTimerSegment()
    }

    fun resumeTimer() {
        if (hasTimerStarted && sessionStartedAtElapsedMs == null) {
            startTimerSegment()
        }
    }

    private fun startTimerSegment() {
        if (timerJob?.isActive == true) return
        sessionStartedAtElapsedMs = SystemClock.elapsedRealtime()
        _timerValueString.postValue(formatTime(elapsedSeconds))
        timerJob = viewModelScope.launch {
            while (isActive) {
                updateElapsedDuration()
                _timerValueString.value = formatTime(elapsedSeconds)
                delay(1000L)
            }
        }
    }

    /**
     * Zamanlayıcıyı durdurur.
     */
    fun stopTimer() {
        pauseTimer()
        hasTimerStarted = false
    }

    fun pauseTimer() {
        sessionStartedAtElapsedMs?.let { startTime ->
            accumulatedElapsedMs += SystemClock.elapsedRealtime() - startTime
        }
        updateElapsedDuration()
        timerJob?.cancel()
        timerJob = null
        sessionStartedAtElapsedMs = null
    }

    private fun updateElapsedDuration() {
        val currentSegmentMs = sessionStartedAtElapsedMs?.let { startTime ->
            SystemClock.elapsedRealtime() - startTime
        } ?: 0L
        elapsedSeconds = ((accumulatedElapsedMs + currentSegmentMs) / 1000L).coerceAtLeast(0L)
    }

    /**
     * Saniyeyi "mm:ss" formatında bir String'e çeviren yardımcı fonksiyon.
     */
    private fun formatTime(seconds: Long): String {
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%02d:%02d", minutes, remainingSeconds)
    }

    /**
     * ViewModel temizlendiğinde (Fragment kapandığında) zamanlayıcının da durduğundan emin ol.
     */
    override fun onCleared() {
        super.onCleared()
        stopTimer()
    }

    private companion object {
        /** İki kare arasında bundan uzun boşluk varsa fazlası tutma süresine eklenmez. */
        const val MAX_HOLD_FRAME_GAP_MS = 500L
    }
}
