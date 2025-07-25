package com.example.arfitness.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.navigation.findNavController

import androidx.recyclerview.widget.RecyclerView
import com.example.arfitness.databinding.ItemExerciseBinding
import com.example.arfitness.entitiy.EgzersizDetay
import com.example.arfitness.entitiy.Exercise
import com.example.arfitness.fragment.AnasayfaFragmentDirections
import com.example.arfitness.R

class CardAdapter(var mContext: Context, var exercisesList: List<EgzersizDetay>) :
    RecyclerView.Adapter<CardAdapter.CardTasarimTutucu>() {

    inner class CardTasarimTutucu(var tasarim: ItemExerciseBinding) : RecyclerView.ViewHolder(tasarim.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardTasarimTutucu {
        val binding = ItemExerciseBinding.inflate(LayoutInflater.from(mContext), parent, false)
        return CardTasarimTutucu(binding)
    }

    override fun onBindViewHolder(holder: CardTasarimTutucu, position: Int) {
        // O anki egzersiz verisini listeden alıyoruz. Tipi artık EgzersizDetay.
        val egzersiz = exercisesList.get(position)
        val t = holder.tasarim

        // Verileri kartın üzerine yazdırıyoruz.
        t.tvExerciseName.text = getExerciseName(egzersiz.gorunenAd)
        t.tvExerciseDescription.text = getExerciseDescription(egzersiz.gorunenAd) // Egzersiz açıklaması

        t.btnStart.setOnClickListener {
            val geciss= AnasayfaFragmentDirections.anadanexercise(egzersiz.gorunenAd)
            Navigation.findNavController(it).navigate(geciss)
        }

        // Kartın tamamına tıklandığında çalışacak olan YENİ listener.
        holder.itemView.setOnClickListener { view ->

            val gecisAction = AnasayfaFragmentDirections
                .anadandetaya(egzersiz) //

            Navigation.findNavController(view).navigate(gecisAction)
        }
    }

    override fun getItemCount(): Int {
        return exercisesList.size
    }
    fun updateList(newList: List<EgzersizDetay>) {
        exercisesList = newList
        notifyDataSetChanged() // RecyclerView'a "liste değişti, kendini yenile" der.
    }
    
    private fun getExerciseName(exerciseName: String): String {
        return when (exerciseName) {
            "Squat" -> mContext.getString(R.string.squat)
            "Push-Up" -> mContext.getString(R.string.push_up)
            "Bicep Curl" -> mContext.getString(R.string.bicep_curl)
            "Shoulder Press" -> mContext.getString(R.string.shoulder_press)
            "Lunge" -> mContext.getString(R.string.lunge)
            "Dumbbell Fly" -> mContext.getString(R.string.dumbbell_fly)
            "Bench Press" -> mContext.getString(R.string.bench_press)
            "Plank" -> mContext.getString(R.string.plank)
            "Pull-up" -> mContext.getString(R.string.pull_up)
            "Lat Pulldown" -> mContext.getString(R.string.lat_pulldown)
            "Hammer Curl" -> mContext.getString(R.string.hammer_curl)
            "Cable Pushdown" -> mContext.getString(R.string.cable_pushdown)
            "Lateral Raise" -> mContext.getString(R.string.lateral_raise)
            "Front Raise" -> mContext.getString(R.string.front_raise)
            "Deadlift" -> mContext.getString(R.string.deadlift)
            else -> exerciseName
        }
    }
    
    private fun getExerciseDescription(exerciseName: String): String {
        return when (exerciseName) {
            "Squat" -> mContext.getString(R.string.leg_muscles)
            "Push-Up" -> mContext.getString(R.string.chest_shoulder_tricep)
            "Bicep Curl" -> mContext.getString(R.string.biceps)
            "Shoulder Press" -> mContext.getString(R.string.shoulders)
            "Lunge" -> mContext.getString(R.string.leg_complex)
            "Dumbbell Fly" -> mContext.getString(R.string.chest)
            "Bench Press" -> mContext.getString(R.string.chest)
            "Plank" -> mContext.getString(R.string.core_abs)
            "Pull-up" -> mContext.getString(R.string.lats_biceps_upper_back)
            "Lat Pulldown" -> mContext.getString(R.string.lats_biceps_upper_back)
            "Hammer Curl" -> mContext.getString(R.string.forearms_brachialis)
            "Cable Pushdown" -> mContext.getString(R.string.triceps)
            "Lateral Raise" -> mContext.getString(R.string.side_deltoids)
            "Front Raise" -> mContext.getString(R.string.front_deltoids)
            "Deadlift" -> mContext.getString(R.string.full_body)
            else -> ""
        }
    }
    
    private fun getStringResource(resourceKey: String): String {
        return when (resourceKey) {
            "leg_muscles" -> mContext.getString(R.string.leg_muscles)
            "chest_shoulder_tricep" -> mContext.getString(R.string.chest_shoulder_tricep)
            "biceps" -> mContext.getString(R.string.biceps)
            "shoulders" -> mContext.getString(R.string.shoulders)
            "leg_complex" -> mContext.getString(R.string.leg_complex)
            "chest" -> mContext.getString(R.string.chest)
            "core_abs" -> mContext.getString(R.string.core_abs)
            "lats_biceps_upper_back" -> mContext.getString(R.string.lats_biceps_upper_back)
            "forearms_brachialis" -> mContext.getString(R.string.forearms_brachialis)
            "triceps" -> mContext.getString(R.string.triceps)
            "side_deltoids" -> mContext.getString(R.string.side_deltoids)
            "front_deltoids" -> mContext.getString(R.string.front_deltoids)
            "full_body" -> mContext.getString(R.string.full_body)
            "squat_instructions" -> mContext.getString(R.string.squat_instructions)
            "pushup_instructions" -> mContext.getString(R.string.pushup_instructions)
            "bicep_instructions" -> mContext.getString(R.string.bicep_instructions)
            "shoulder_press_instructions" -> mContext.getString(R.string.shoulder_press_instructions)
            "lunge_instructions" -> mContext.getString(R.string.lunge_instructions)
            "dumbbell_fly_instructions" -> mContext.getString(R.string.dumbbell_fly_instructions)
            "bench_press_instructions" -> mContext.getString(R.string.bench_press_instructions)
            "plank_instructions" -> mContext.getString(R.string.plank_instructions)
            "pull_up_instructions" -> mContext.getString(R.string.pull_up_instructions)
            "lat_pulldown_instructions" -> mContext.getString(R.string.lat_pulldown_instructions)
            "hammer_curl_instructions" -> mContext.getString(R.string.hammer_curl_instructions)
            "cable_pushdown_instructions" -> mContext.getString(R.string.cable_pushdown_instructions)
            "lateral_raise_instructions" -> mContext.getString(R.string.lateral_raise_instructions)
            "front_raise_instructions" -> mContext.getString(R.string.front_raise_instructions)
            "deadlift_instructions" -> mContext.getString(R.string.deadlift_instructions)
            "beginner" -> mContext.getString(R.string.beginner)
            "intermediate" -> mContext.getString(R.string.intermediate)
            "hard" -> mContext.getString(R.string.hard)
            "advanced" -> mContext.getString(R.string.advanced)
            "duration_1_3_min" -> mContext.getString(R.string.duration_1_3_min)
            "duration_2_4_min" -> mContext.getString(R.string.duration_2_4_min)
            "duration_3_5_min" -> mContext.getString(R.string.duration_3_5_min)
            "duration_3_6_min" -> mContext.getString(R.string.duration_3_6_min)
            "duration_4_6_min" -> mContext.getString(R.string.duration_4_6_min)
            "duration_4_7_min" -> mContext.getString(R.string.duration_4_7_min)
            "duration_7_12_min" -> mContext.getString(R.string.duration_7_12_min)
            "duration_5_8_min" -> mContext.getString(R.string.duration_5_8_min)
            else -> resourceKey
        }
    }
}