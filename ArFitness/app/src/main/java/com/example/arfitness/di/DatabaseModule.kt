package com.example.arfitness.di

import android.content.Context
import com.example.arfitness.data.AppDatabase
import com.example.arfitness.data.WorkoutDao
import com.example.arfitness.datasource.WorkoutDataSource
import com.example.arfitness.repository.WorkoutRepository

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    // Hilt'e, AppDatabase'in nasıl oluşturulacağını öğretiyoruz.
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    // Hilt'e, WorkoutDao'nun nasıl oluşturulacağını öğretiyoruz.
    // (AppDatabase'i kullanarak)
    @Provides
    @Singleton
    fun provideWorkoutDao(appDatabase: AppDatabase): WorkoutDao {
        return appDatabase.workoutDao()
    }

    // Hilt'e, WorkoutLocalDataSource'un nasıl oluşturulacağını öğretiyoruz.
    // (WorkoutDao'yu kullanarak)
    @Provides
    @Singleton
    fun provideWorkoutLocalDataSource(workoutDao: WorkoutDao): WorkoutDataSource {
        return WorkoutDataSource(workoutDao)
    }

    // Hilt'e, WorkoutRepository'nin nasıl oluşturulacağını öğretiyoruz.
    // (WorkoutLocalDataSource'u kullanarak)
    @Provides
    @Singleton
    fun provideWorkoutRepository(localDataSource: WorkoutDataSource): WorkoutRepository {
        return WorkoutRepository(localDataSource)
    }
}
