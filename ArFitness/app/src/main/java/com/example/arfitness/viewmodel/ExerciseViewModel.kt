package com.example.arfitness.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Job
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arfitness.data.WorkoutSession
import com.example.arfitness.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class ExerciseViewModel @Inject constructor(private val repository: WorkoutRepository) : ViewModel() {

    // --- Tekrar Sayısı (LiveData olarak) ---
    private val _tekrarSayisi = MutableLiveData<Int>(0)
    val tekrarSayisi: LiveData<Int> = _tekrarSayisi

    // --- Geri Bildirim Mesajı (LiveData olarak) ---
    private val _feedbackMessage = MutableLiveData<String>("Harekete Başla!")
    val feedbackMessage: LiveData<String> = _feedbackMessage

    // --- Zamanlayıcı (LiveData olarak) ---
    private val _timerValueString = MutableLiveData<String>("00:00")
    val timerValueString: LiveData<String> = _timerValueString

    // Toplam geçen süre (saniye cinsinden)
    private var totalDurationSeconds: Long = 60L
    private var elapsedSeconds: Long = 0L

    // Coroutine işini tutacak değişken. Bu, zamanlayıcıyı durdurmamızı sağlar.
    private var timerJob: Job? = null

    // Bu fonksiyonlar, analizörden gelen sonuçlara göre ViewModel'i güncelleyecek.
    fun onRepCounted() {
        val current = _tekrarSayisi.value ?: 0
        _tekrarSayisi.postValue(current + 1)
    }

    fun updateFeedback(message: String) {
        // Aynı mesajı tekrar tekrar göstermemek için kontrol
        if (_feedbackMessage.value != message) {
            _feedbackMessage.postValue(message)
        }
    }

    fun getElapsedSeconds(): Long {
        return if (elapsedSeconds > 0) elapsedSeconds else 1L
    }

    fun saveWorkout(exerciseName: String, reps: Int, duration: Long) {
        viewModelScope.launch {
            // Basit form skoru ve kalori hesabı
            val calculatedScore = if (reps > 0) 85 + (reps.coerceAtMost(15)) else 70
            val calculatedCalories = (reps * 1.5).toInt()
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
        _tekrarSayisi.postValue(0)
        _feedbackMessage.postValue("Egzersize Başla!")
        _timerValueString.postValue("00:00")
        elapsedSeconds = 0L
    }

    fun startTimer(totalSeconds: Long = 60L) {
        totalDurationSeconds = totalSeconds
        elapsedSeconds = 0L
        Log.d("TimerDebug", "startTimer çağrıldı! Süre: $totalSeconds saniye")
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            for (second in totalSeconds downTo 0) {
                elapsedSeconds = totalSeconds - second
                _timerValueString.postValue(formatTime(second))
                Log.d("TimerDebug", "Kalan süre: ${formatTime(second)}, Geçen: $elapsedSeconds")
                delay(1000L)
            }
            _feedbackMessage.postValue("Süre Doldu! Harika İş!")
        }
    }

    /**
     * Zamanlayıcıyı durdurur.
     */
    fun stopTimer() {
        timerJob?.cancel()
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
}