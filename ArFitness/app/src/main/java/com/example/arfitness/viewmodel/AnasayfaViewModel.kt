package com.example.arfitness.viewmodel

import androidx.lifecycle.ViewModel
import com.example.arfitness.repository.WorkoutRepository
import com.example.arfitness.entitiy.EgzersizDetay


import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AnasayfaViewModel @Inject constructor(
    private val repository: WorkoutRepository
) : ViewModel() {

    // Egzersiz listesini Repository'den bir kere alıp tutuyoruz.
    val egzersizListesi: List<EgzersizDetay> = repository.getExerciseDetailList()

}