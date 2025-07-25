package com.example.arfitness

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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

        // --- PADDING (İÇ BOŞLUK) AYARI ---
        // 5. 'findViewById' yerine binding.root kullanarak en dıştaki layout'a eriş
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}