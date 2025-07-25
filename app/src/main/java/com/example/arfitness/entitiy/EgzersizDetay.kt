package com.example.arfitness.entitiy

import android.os.Parcelable
import com.example.arfitness.analysis.EgzersizKategorisiEnum
import kotlinx.parcelize.Parcelize

@Parcelize
data class EgzersizDetay(
    val gorunenAd: String,      // Ekranda görünecek ad, örn: "Şınav"
    val kodAdi: String,         // Kod içinde kullanacağımız ad, örn: "push-ups"
    val aciklama: String,       // Egzersizin nasıl yapılacağı
    val hedefKaslar: String,    // Çalıştırdığı kaslar, örn: "Göğüs, Omuz"
    val gifKaynakId: Int, // `R.drawable.gif_sinav` gibi GIF'in ID'si
    val tahminiSure: String,
    val zorlukSeviyesi: String,
    val kategori: EgzersizKategorisiEnum
) : Parcelable