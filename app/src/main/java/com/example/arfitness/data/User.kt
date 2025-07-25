package com.example.arfitness.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class User(
    @PrimaryKey
    val id: Int = 1, // Sadece tek bir kullanıcı olacağı için ID'yi sabit tutuyoruz (Singleton Tablo)
    val name: String,
    val email: String="",
    val weight: Double, // Kilo
    val height: Int,    // Boy (cm)
    val goal: String="", // Hedef (örn: "Kilo Vermek", "Kas Geliştirmek")
    val age: Int=0,
    val gender: String="", // Cinsiyet (Erkek, Kadın)

)