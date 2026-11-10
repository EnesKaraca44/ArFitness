package com.example.arfitness.analysis

/**
 * Bir analizörün tek bir kare için ürettiği rapor.
 */
data class Feedback(
    val message: String?,
    /** Bu karede bir tekrar tamamlandı. */
    val isRepCounted: Boolean = false,
    /** Form hatası veya görünürlük uyarısı: ekrandaki kart kırmızı gösterilir. */
    val isWarning: Boolean = false,
    /** Süreli hareketlerde (Plank) kullanıcı bu karede doğru pozisyonda mı; süre yalnızca o zaman işler. */
    val isHolding: Boolean = false,
)
