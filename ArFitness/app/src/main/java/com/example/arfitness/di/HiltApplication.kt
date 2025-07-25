package com.example.arfitness // Kendi paket adınız

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

// Hilt'e uygulamanın başlangıç noktasının burası olduğunu söylüyoruz.
@HiltAndroidApp
class HiltApplication : Application() {
    // Genellikle bu sınıfın içi başlangıçta boştur.
}