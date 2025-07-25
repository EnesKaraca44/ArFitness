package com.example.arfitness.repository

import com.example.arfitness.data.WorkoutSession
import com.example.arfitness.datasource.WorkoutDataSource
import com.example.arfitness.entitiy.EgzersizDetay
import kotlinx.coroutines.flow.Flow
import com.example.arfitness.R
import com.example.arfitness.analysis.EgzersizKategorisiEnum
import com.example.arfitness.data.User
import com.example.arfitness.entitiy.EgzersizKategorisi

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
                kodAdi = "squat",
                aciklama = "squat_instructions",
                hedefKaslar = "leg_muscles",
                gifKaynakId = R.drawable.gif_squat,
                tahminiSure = "duration_3_5_min",
                zorlukSeviyesi = "intermediate" ,
                kategori = EgzersizKategorisiEnum.BACAK
            ),
            EgzersizDetay(
                gorunenAd = "Push-Up",
                kodAdi = "push-ups",
                aciklama = "pushup_instructions",
                hedefKaslar = "chest_shoulder_tricep",
                gifKaynakId = R.drawable.gif_pushup,
                tahminiSure = "duration_2_4_min",
                zorlukSeviyesi = "hard",
                kategori = EgzersizKategorisiEnum.GOGUS
            ),
            EgzersizDetay(
                gorunenAd = "Bicep Curl",
                kodAdi = "bicep curl",
                aciklama = "bicep_instructions",
                hedefKaslar = "biceps",
                gifKaynakId = R.drawable.gif_bicep_curl,
                tahminiSure = "duration_5_8_min",
                zorlukSeviyesi = "beginner",
                kategori = EgzersizKategorisiEnum.KOL
            ) ,
            EgzersizDetay(
                gorunenAd = "Shoulder Press",
                kodAdi = "shoulder press",
                aciklama = "shoulder_press_instructions",
                hedefKaslar = "shoulders",
                gifKaynakId = R.drawable.gif_shoulderpress,
                tahminiSure = "duration_5_8_min",
                zorlukSeviyesi = "beginner",
                kategori = EgzersizKategorisiEnum.OMUZ
            ),
            EgzersizDetay(
                gorunenAd = "Lunge",
                kodAdi = "lunge",
                aciklama = "lunge_instructions",
                hedefKaslar = "leg_complex",
                gifKaynakId = R.drawable.gif_lunge,
                tahminiSure = "duration_5_8_min",
                zorlukSeviyesi = "beginner",
                kategori = EgzersizKategorisiEnum.BACAK
            ),
            EgzersizDetay(
                gorunenAd = "Dumbbell Fly",
                kodAdi = "dumbbell fly",
                aciklama = "dumbbell_fly_instructions",
                hedefKaslar = "chest",
                gifKaynakId = R.drawable.gif_fly_dumbell,
                tahminiSure = "duration_5_8_min",
                zorlukSeviyesi = "beginner",
                kategori = EgzersizKategorisiEnum.GOGUS
            ),
            EgzersizDetay(
                gorunenAd = "Bench Press",
                kodAdi = "bench press",
                aciklama = "bench_press_instructions",
                hedefKaslar = "chest",
                gifKaynakId = R.drawable.gif_bench_press,
                tahminiSure = "duration_5_8_min",
                zorlukSeviyesi = "beginner",
                kategori = EgzersizKategorisiEnum.GOGUS
            ),
            EgzersizDetay(
                gorunenAd = "Plank",
                kodAdi = "plank",
                aciklama = "plank_instructions",
                hedefKaslar = "core_abs",
                gifKaynakId = R.drawable.gif_plank,
                tahminiSure = "duration_1_3_min",
                zorlukSeviyesi = "beginner",
                kategori = EgzersizKategorisiEnum.KARIN
            ),
            EgzersizDetay(
                gorunenAd = "Pull-up",
                kodAdi = "pull-up",
                aciklama = "pull_up_instructions",
                hedefKaslar = "lats_biceps_upper_back",
                gifKaynakId = R.drawable.gif_pull_up,
                tahminiSure = "duration_3_6_min",
                zorlukSeviyesi = "advanced",
                kategori = EgzersizKategorisiEnum.SIRT
            ),
            EgzersizDetay(
                gorunenAd = "Lat Pulldown",
                kodAdi = "lat pulldown",
                aciklama = "lat_pulldown_instructions",
                hedefKaslar = "lats_biceps_upper_back",
                gifKaynakId = R.drawable.gif_lat_pulldown,
                tahminiSure = "duration_5_8_min",
                zorlukSeviyesi = "intermediate",
                kategori = EgzersizKategorisiEnum.SIRT
            ),
            EgzersizDetay(
                gorunenAd = "Hammer Curl",
                kodAdi = "hammer curl",
                aciklama = "hammer_curl_instructions",
                hedefKaslar = "forearms_brachialis",
                gifKaynakId = R.drawable.gif_hammer_curl,
                tahminiSure = "duration_5_8_min",
                zorlukSeviyesi = "beginner",
                kategori = EgzersizKategorisiEnum.KOL
            ),
            EgzersizDetay(
                gorunenAd = "Cable Pushdown",
                kodAdi = "cable pushdown",
                aciklama = "cable_pushdown_instructions",
                hedefKaslar = "triceps",
                gifKaynakId = R.drawable.gif_cable_pushdown,
                tahminiSure = "duration_4_7_min",
                zorlukSeviyesi = "beginner",
                kategori = EgzersizKategorisiEnum.KOL
            ),
            EgzersizDetay(
                gorunenAd = "Lateral Raise",
                kodAdi = "lateral raise",
                aciklama = "lateral_raise_instructions",
                hedefKaslar = "side_deltoids",
                gifKaynakId = R.drawable.gif_lateral_raise,
                tahminiSure = "duration_4_6_min",
                zorlukSeviyesi = "intermediate",
                kategori = EgzersizKategorisiEnum.OMUZ
            ),
            EgzersizDetay(
                gorunenAd = "Front Raise",
                kodAdi = "front raise",
                aciklama = "front_raise_instructions",
                hedefKaslar = "front_deltoids",
                gifKaynakId = R.drawable.gif_front_raise,
                tahminiSure = "duration_4_6_min",
                zorlukSeviyesi = "intermediate",
                kategori = EgzersizKategorisiEnum.OMUZ
            ),
            EgzersizDetay(
                gorunenAd = "Deadlift",
                kodAdi = "deadlift",
                aciklama = "deadlift_instructions",
                hedefKaslar = "full_body",
                gifKaynakId = R.drawable.gif_deadlift,
                tahminiSure = "duration_7_12_min",
                zorlukSeviyesi = "advanced",
                kategori = EgzersizKategorisiEnum.TUM_VUCUT
            )
        )
    }
    // --- YENİ KULLANICI FONKSİYONLARI ---
    fun getUserProfile(): Flow<User?> {
        // DataSource üzerinden DAO'ya ulaşmamız gerekiyor.
        // Bunun için DataSource'u da güncellememiz lazım.
        return dataSource.getUser()
    }
    suspend fun saveUserProfile(user: User) {
        dataSource.insertOrUpdateUser(user)
    }

    fun getCategorizedExercises(): List<EgzersizKategorisi> {
        // Önce tüm egzersizlerin tam listesini al (bu zaten sende var)
        val tumEgzersizler = getExerciseDetailList()

        // Şimdi bu listeyi kategorilere ayıralım
        return EgzersizKategorisiEnum.values().map { kategoriEnum ->
            EgzersizKategorisi(
                kategoriAdi = kategoriEnum.gorunenAd,
                kategoriIkonId = when (kategoriEnum) {
                    EgzersizKategorisiEnum.BACAK -> R.drawable.ic_bacakkk
                    EgzersizKategorisiEnum.GOGUS -> R.drawable.ic_gogusss
                    EgzersizKategorisiEnum.KOL -> R.drawable.ic_kolll
                    EgzersizKategorisiEnum.OMUZ -> R.drawable.ic_omuzzz
                    EgzersizKategorisiEnum.SIRT -> R.drawable.ic_sirtttt
                    EgzersizKategorisiEnum.KARIN -> R.drawable.ic_karinnn
                    EgzersizKategorisiEnum.TUM_VUCUT -> R.drawable.gif_plank
                },
                egzersizler = tumEgzersizler.filter { it.kategori == kategoriEnum }
            )


        }
    }

}