package com.example.arfitness

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.navigation.fragment.NavHostFragment
import dagger.hilt.android.AndroidEntryPoint
import androidx.navigation.ui.setupWithNavController
import com.example.arfitness.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    // View Binding için bir değişken tanımlıyoruz
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        // Splash Screen'i kur
        installSplashScreen()

        super.onCreate(savedInstanceState)

        // --- VIEW BINDING KURULUMU ---
        // 1. Layout'u inflate et ve binding nesnesini oluştur
        binding = ActivityMainBinding.inflate(layoutInflater)
        // 2. setContentView'i SADECE BİR KERE, binding.root ile çağır
        setContentView(binding.root)

        // --- NAVIGASYON KURULUMU ---
        // 3. NavHostFragment'ı doğru ID ile bul
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // 4. BottomNavigationView'ı NavController ile bağla
        binding.bottomNavigationView.setupWithNavController(navController)

        // 5. Antrenman kamerası açıkken alt menüyü (BottomNav) tamamen gizle!
        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.bottomNavigationView.isVisible = destination.id != R.id.exerciseFragment
            // Alt boşluğun sahibi değişti (BottomNav <-> kök layout), inset'leri yeniden uygula
            ViewCompat.requestApplyInsets(binding.root)
        }

        // --- PADDING (İÇ BOŞLUK) AYARI ---
        // 6. BottomNavigationView, sistem navigasyon çubuğu yüksekliğini kendi alt padding'i
        //    olarak zaten ekliyor. Kök layout da alttan padding verirse boşluk iki kat olur
        //    (ikonların altındaki siyah alan). Bu yüzden alt inset'i yalnızca BottomNav
        //    gizliyken (antrenman ekranı) kök layout'a uygula.
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val bottom = if (binding.bottomNavigationView.isVisible) 0 else systemBars.bottom
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, bottom)
            insets
        }
    }
}