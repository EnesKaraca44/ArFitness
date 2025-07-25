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
    private val _feedbackMessage = MutableLiveData<String>("")
    val feedbackMessage: LiveData<String> = _feedbackMessage

    // --- Zamanlayıcı (LiveData olarak) ---
    private val _timerValueString = MutableLiveData<String>("00:30") // Başlangıç değeri
    val timerValueString: LiveData<String> = _timerValueString

    // Coroutine işini tutacak değişken. Bu, zamanlayıcıyı durdurmamızı sağlar.
    private var timerJob: Job? = null


    // Bu fonksiyonlar, analizörden gelen sonuçlara göre ViewModel'i güncelleyecek.
    fun onRepCounted() {
        _tekrarSayisi.value = (_tekrarSayisi.value ?: 0) + 1
    }
    
    fun updateFeedbackMessage(message: String) {
        _feedbackMessage.value = message
    }
    fun updateFeedback(message: String) {
        // Aynı mesajı tekrar tekrar göstermemek için kontrol
        if (_feedbackMessage.value != message) {
            _feedbackMessage.value = message
        }
    }
    fun saveWorkout(exerciseName: String, reps: Int, duration: Long) {
        // viewModelScope, bu işlemin ViewModel'in yaşam döngüsüne bağlı bir
        // coroutine içinde güvenle yapılmasını sağlar.
        viewModelScope.launch {
            val calculatedScore = 90 // Şimdilik varsayılan skor
            val calculatedCalories = (reps * 1.5).toInt() // Her tekrar için 1.5 kalori gibi basit bir formül
            val session = WorkoutSession(
                exerciseName = exerciseName,
                repCount = reps,
                durationInSeconds = duration,
                date = System.currentTimeMillis(), // O anki zamanı milisaniye olarak alıyoruz
                        formScore = calculatedScore,
                caloriesBurned = calculatedCalories
            )
            repository.saveWorkoutSession(session)


        }
    }

    fun reset() {
        _tekrarSayisi.value = 0
        // Feedback messages will be set from the fragments using string resources
        stopTimer() // Çalışan zamanlayıcıyı durdur
        _timerValueString.value = "00:30"
    }

    fun startTimer(gerisayimsure: Long) {
        Log.d("TimerDebug", "LiveData ile startTimer çağrıldı! Süre: $gerisayimsure saniye")
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            for (second in gerisayimsure downTo 0) {
                // Arka plandan LiveData'yı güvenle güncellemek için .postValue() kullanılır.
                _timerValueString.postValue(formatTime(second))
                Log.d("TimerDebug", "LiveData güncellendi: ${formatTime(second)}")
                delay(1000L)
            }
            // Timer end message will be set from the fragment using string resources
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