package com.example.arfitness.entitiy

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * @param isTimed Plank gibi süreli hareketlerde true; o durumda [durationInSeconds]
 * doğru pozisyonda geçen tutma süresidir ve [totalReps] kullanılmaz.
 * @param formScore seansın form puanı (0–100); puanlanacak tekrar yoksa null
 */
@Parcelize
data class WorkoutResult(
    val exerciseName: String,
    val totalReps: Int,
    val durationInSeconds: Long,
    val isTimed: Boolean = false,
    val formScore: Int? = null,
) : Parcelable
