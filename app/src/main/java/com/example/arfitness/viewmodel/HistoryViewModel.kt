package com.example.arfitness.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.arfitness.data.WorkoutSession

import com.example.arfitness.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: WorkoutRepository
) : ViewModel() {

    // Repository'den gelen Flow<List<WorkoutSession>> verisini,
    // Fragment'ın kolayca dinleyebileceği bir LiveData'ya dönüştürüyoruz.
    val workoutHistory: LiveData<List<WorkoutSession>> = repository.getWorkoutHistory()
        .asLiveData(viewModelScope.coroutineContext)


    // --- YENİ ---
    // Takvimde gösterilecek günleri oluşturan fonksiyon
    fun getCalendarDays(): List<LocalDate> {
        val days = mutableListOf<LocalDate>()
        // Bugün'den başlayarak 30 gün geriye gidelim
        for (i in 0..30) {
            days.add(LocalDate.now().minusDays(i.toLong()))
        }
        return days.reversed() // Listeyi ters çevirerek en eski tarihin en başta olmasını sağlıyoruz
    }
}