package com.example.arfitness.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.example.arfitness.data.User
import com.example.arfitness.data.WorkoutSession

import com.example.arfitness.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: WorkoutRepository
) : ViewModel() {

    // 1. Kullanıcı profilini veritabanından anlık olarak dinle.
    val userProfile: LiveData<User?> = repository.getUserProfile().asLiveData(viewModelScope.coroutineContext)

    // 2. Tüm antrenman geçmişini veritabanından anlık olarak dinle.
    val workoutHistory: LiveData<List<WorkoutSession>> = repository.getWorkoutHistory().asLiveData(viewModelScope.coroutineContext)

    // --- YENİ FONKSİYON ---
    fun saveUserProfile(user: User) {
        viewModelScope.launch {
            repository.saveUserProfile(user)

        }
    }
    // --- YENİ: Grafik için hazırlanmış veri ---
    // workoutHistory her değiştiğinde, bu 'map' bloğu otomatik olarak çalışır
    // ve haftalık veriyi hesaplayıp yeni bir LiveData olarak sunar.
    val weeklyChartData: LiveData<Map<DayOfWeek, Int>> = workoutHistory.map { historyList ->
        // Bu hafta Pazartesi'den Pazar'a olan tarih aralığını bul
        val today = LocalDate.now()
        val startOfWeek = today.with(DayOfWeek.MONDAY)
        val endOfWeek = today.with(DayOfWeek.SUNDAY)

        // Haftanın günleri için boş bir harita oluştur
        val weeklyCalories = DayOfWeek.values().associateWith { 0 }.toMutableMap()

        // Sadece bu haftaya ait antrenmanları filtrele
        val thisWeeksWorkouts = historyList.filter {
            val workoutDate = Instant.ofEpochMilli(it.date)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
            !workoutDate.isBefore(startOfWeek) && !workoutDate.isAfter(endOfWeek)
        }

        // Bu haftanın antrenmanlarını günlere göre grupla ve toplam kaoriyi göster
        thisWeeksWorkouts.forEach { workout ->
            val day = Instant.ofEpochMilli(workout.date)
                .atZone(ZoneId.systemDefault())
                .toLocalDate().dayOfWeek
            weeklyCalories[day] = (weeklyCalories[day] ?: 0) + workout.caloriesBurned
        }

        // Hesaplanmış haftalık veriyi döndür
        weeklyCalories
    }


}