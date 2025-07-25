package com.example.arfitness.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow


@Dao
interface WorkoutDao {
    // 'onConflict' stratejisi, eğer aynı id'ye sahip bir veri eklenmeye çalışılırsa ne yapılacağını söyler.
    // 'IGNORE' çakışmayı görmezden gelir, 'REPLACE' eskisini yenisiyle değiştirir.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: WorkoutSession) //session hareket

    // 'onConflict' stratejisi, eğer aynı id'ye sahip bir veri eklenmeye çalışılırsa ne yapılacağını söyler.
    // 'IGNORE' çakışmayı görmezden gelir, 'REPLACE' eskisini yenisiyle değiştirir.
    @Query("SELECT * FROM workout_history ORDER BY date DESC")
    fun getAllSessions(): Flow<List<WorkoutSession>>

    @Query("DELETE FROM workout_history")
    suspend fun clearHistory()

}