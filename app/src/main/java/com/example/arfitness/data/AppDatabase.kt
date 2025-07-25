package com.example.arfitness.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

//@Database, bu sınıfın bir Room veritabanı olduğunu belirtir.
// 'entities' parametresi, bu veritabanının hangi tabloları içereceğini söyler.
// 'version' ise veritabanının sürüm numarasıdır. İleride tabloda bir değişiklik yaparsak

@Database(entities = [WorkoutSession::class,User::class], version = 3, exportSchema = false)
abstract class AppDatabase: RoomDatabase() {

    // Bu veritabanının hangi DAOları kullandığını belirtiyoruz.
    abstract fun workoutDao(): WorkoutDao
    abstract fun userDao(): UserDao

    companion object {
        // @Volatile, bu değişkenin değerinin tüm thread'ler tarafından anında görülmesini sağlar.
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            // Eğer INSTANCE null ise (yani daha önce hiç veritabanı oluşturulmamışsa),
            // 'synchronized' bloğu içinde güvenli bir şekilde oluştur.
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "arfitform_database" // Veritabanı dosyasının adı
                ).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

}