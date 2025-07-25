package com.example.arfitness.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.arfitness.analysis.EgzersizKategorisiEnum
import com.example.arfitness.repository.WorkoutRepository
import com.example.arfitness.entitiy.EgzersizDetay
import com.example.arfitness.entitiy.EgzersizKategorisi


import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AnasayfaViewModel @Inject constructor(
    private val repository: WorkoutRepository
) : ViewModel() {

    // 1. Repository'den tüm egzersizlerin düz listesini al.
    private val tumEgzersizler: List<EgzersizDetay> = repository.getExerciseDetailList()


    val tumKategoriliEgzersizler: List<EgzersizKategorisi> = repository.getCategorizedExercises()

    // Fragment'ın dinleyeceği, o an gösterilecek olan filtrelenmiş egzersiz listesi.
    private val _filtrelenmisEgzersizler = MutableLiveData<List<EgzersizDetay>>()
    val filtrelenmisEgzersizler: LiveData<List<EgzersizDetay>> = _filtrelenmisEgzersizler

    init {
        // Uygulama açıldığında, ilk kategorinin egzersizlerini göster.
        if (tumKategoriliEgzersizler.isNotEmpty()) {
            filtrele(tumKategoriliEgzersizler.first().kategoriAdi)
        }
    }

    // Bir kategoriye tıklandığında bu fonksiyon çağrılır.
    fun filtrele(kategoriAdi: String) {
        val secilenKategori = tumKategoriliEgzersizler.find { it.kategoriAdi == kategoriAdi }
        // LiveData'yı, seçilen kategorinin egzersizleriyle güncelle.
        _filtrelenmisEgzersizler.value = secilenKategori?.egzersizler ?: emptyList()
    }
}