package com.example.arfitness.analysis

/**
 * Lateral Raise: kollar yanlara omuz hizasına kalkar; sayım mantığı [RaiseAnalyzer] içinde.
 * (Eski analizör iki kolun aynı anda kalkmasını ve sabit piksel eşiklerini şart koşuyordu.)
 */
class LateralRaiseAnalyzer : RaiseAnalyzer(key = "lateralWrist") {
    override val goMessage = "Kollarını yanlara, omuz hizasına kaldır"
}
