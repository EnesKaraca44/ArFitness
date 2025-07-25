package com.example.arfitness

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SplashActivity : AppCompatActivity() {
    
    private val splashDuration = 3000L // 3 seconds
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        
        // Handle system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.splashRoot)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        // Start animations
        startAnimations()
        
        // Navigate to MainActivity after splash duration
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, splashDuration)
    }
    
    private fun startAnimations() {
        val logoImageView = findViewById<android.widget.ImageView>(R.id.splashLogo)
        val textView = findViewById<android.widget.TextView>(R.id.splashText)
        
        // Logo animation - scale and fade in
        val logoAnimation = AnimationUtils.loadAnimation(this, R.anim.splash_logo_animation)
        logoImageView.startAnimation(logoAnimation)
        
        // Text animation - fade in with delay
        val textAnimation = AnimationUtils.loadAnimation(this, R.anim.splash_text_animation)
        textView.startAnimation(textAnimation)
    }
} 