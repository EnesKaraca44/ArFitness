package com.example.arfitness.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow


@Dao
interface UserDao {


    // 'REPLACE' stratejisi, yeni bir profil kaydedildiğinde eskisinin üzerine yazılmasını sağlar.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateUser(user: User)

    // Kullanıcıyı getiren fonksiyon. Tek bir kullanıcı olduğu için Flow ile anlık takip edebiliriz.
    @Query("SELECT * FROM user_profile WHERE id = 1")
    fun getUser(): Flow<User?> // Profil henüz oluşturulmamışsa null dönebilir

}