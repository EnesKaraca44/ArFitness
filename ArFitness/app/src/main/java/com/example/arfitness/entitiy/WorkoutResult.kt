package com.example.arfitness.entitiy

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class WorkoutResult(val exerciseName:String,val totalReps: Int,val durationInSeconds:Long):
    Parcelable
