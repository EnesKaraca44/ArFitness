package com.example.arfitness.analysis

enum class ExerciseState {


    NEUTRAL, // Başlangıç veya bitiş pozisyonu (ayakta durmak gibi)
    DOWN,    // Hareketin en alt noktası (çömelmiş veya göğüs yere yakın)
    UP,      // Hareketin yukarı doğru olan kısmı

    OPEN,
    CLOSED,
    PLANKING, // Plank gibi statik duruşlar için
    UNKNOWN  // Henüz bir durum tespit edilemediğinde
}