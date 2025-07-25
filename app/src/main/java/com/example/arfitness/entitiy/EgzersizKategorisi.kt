package com.example.arfitness.entitiy

data class EgzersizKategorisi(
    val kategoriAdi: String,
    val kategoriIkonId: Int,
    val egzersizler: List<EgzersizDetay>
)