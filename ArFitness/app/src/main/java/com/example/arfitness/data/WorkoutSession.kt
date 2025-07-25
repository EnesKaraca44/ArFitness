package com.example.arfitness.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workout_history")
data class WorkoutSession(@PrimaryKey(autoGenerate = true) val id:Int=0, val exerciseName: String, val repCount: Int, val durationInSeconds: Long, val date: Long,val formScore: Int,val caloriesBurned: Int )
