package com.example.arfitness.repository

import com.example.arfitness.data.WorkoutSession
import com.example.arfitness.datasource.WorkoutDataSource
import com.example.arfitness.entitiy.EgzersizDetay
import kotlinx.coroutines.flow.Flow
import com.example.arfitness.R

class WorkoutRepository(private val dataSource: WorkoutDataSource) {
    fun getWorkoutHistory(): Flow<List<WorkoutSession>> {
        return dataSource.getAllSessions()
    }
    suspend fun saveWorkoutSession(session: WorkoutSession) {
        dataSource.insertSession(session)
    }
    suspend fun clearWorkoutHistory() {
        dataSource.clearHistory()
    }

    fun getExerciseDetailList(): List<EgzersizDetay> {
        return listOf(
            EgzersizDetay(
                gorunenAd = "Squat",
                kodAdi = "Squat",
                aciklama = "• Sırtınızı dik tutun.\n• Dizlerinizin parmak uçlarınızı geçmemesine özen gösterin.",
                hedefKaslar = "Kalça, Ön Bacak, Arka Bacak",
                gifKaynakId = R.drawable.gif_squat, // Bu GIF'leri drawable klasörüne ekle
                tahminiSure = "3-5 dk",
                zorlukSeviyesi = "Orta"
            ),
            EgzersizDetay(
                gorunenAd = "Push-Up",
                kodAdi = "Push-Up",
                aciklama = "• Vücudunuzu baştan topuklara kadar düz bir çizgi halinde tutun.",
                hedefKaslar = "Göğüs, Omuz, Arka Kol",
                gifKaynakId = R.drawable.gif_pushup,
                tahminiSure = "2-4 dk",
                zorlukSeviyesi = "Zor"
            ),
            EgzersizDetay(
                gorunenAd = "Bicep Curl",
                kodAdi = "Bicep Curl",
                aciklama = "• Dirseklerinizi vücudunuza sabit tutarak dumbbell'ları omuzlarınıza doğru kaldırın.",
                hedefKaslar = "Pazu (Biceps)",
                gifKaynakId = R.drawable.gif_bicep_curl,
                tahminiSure = "5-8 dk",
                zorlukSeviyesi = "Başlangıç"
            ) ,
            EgzersizDetay(
                gorunenAd = "Shoulder Press",
            kodAdi = "Shoulder Press",
            aciklama = "• Dumbbell'ları omuz hizasında avuç içleriniz ileri bakacak şekilde tutun.\n• Kollarınızı tamamen yukarı kaldırana kadar dumbbell'ları başınızın üzerine doğru itin.\n• Kontrollü bir şekilde başlangıç pozisyonuna geri dönün.",
            hedefKaslar = "Omuz (Deltoid)",
            gifKaynakId = R.drawable.gif_shoulderpress,
            tahminiSure = "5-8 dk",
            zorlukSeviyesi = "Başlangıç"
        ),
            EgzersizDetay(
                gorunenAd = "Lunge",
                kodAdi = "Lunge",
                aciklama = "• Bir ayağınızla öne doğru büyük bir adım atın.\n• Her iki dizinizi de yaklaşık 90 derece bükerek arka diziniz yere yaklaşana kadar vücudunuzu indirin.\n• Ön dizinizin ayak bileğinizin üzerinde olduğundan emin olun.\n• Başlangıç pozisyonuna geri dönmek için ön ayağınızla itin ve diğer bacağınızla tekrarlayın.",
                hedefKaslar = "Bacak (Quadriceps, Hamstring, Gluteus)",
                gifKaynakId = R.drawable.gif_lunge,
                tahminiSure = "5-8 dk",
                zorlukSeviyesi = "Başlangıç"
            ),
            EgzersizDetay(
                gorunenAd = "Dumbbell Fly",
                kodAdi = "Dumbell fly",
                aciklama = "• Sırt üstü bir bench'e uzanın, her elinizde birer dumbbell tutun.\n• Kollarınızı hafifçe bükerek dumbbell'ları göğsünüzün üzerinde birleştirin, avuç içleriniz birbirine baksın.\n• Kontrollü bir şekilde kollarınızı yana doğru açarak dumbbell'ları omuz hizasına indirin, göğsünüzde bir gerilme hissedin.\n• Göğüs kaslarınızı sıkarak dumbbell'ları tekrar yukarı kaldırın.",
                hedefKaslar = "Göğüs (Pectoralis Major)",
                gifKaynakId = R.drawable.gif_fly_dumbell,
                tahminiSure = "5-8 dk",
                zorlukSeviyesi = "Başlangıç"
            )
        )
    }


}