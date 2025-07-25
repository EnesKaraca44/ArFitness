package com.example.arfitness.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.arfitness.databinding.FragmentExerciseDetailBinding
import com.example.arfitness.entitiy.EgzersizDetay
import com.example.arfitness.R
import dagger.hilt.android.AndroidEntryPoint
import kotlin.getValue
import com.bumptech.glide.Glide

@AndroidEntryPoint
class ExerciseDetailFragment : Fragment() {


    private var _binding: FragmentExerciseDetailBinding? = null
    private val binding get() = _binding!!

    // Safe Args ile gelen argümanları almak için
    private val argumanlar: ExerciseDetailFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExerciseDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Gelen egzersiz detaylarını bir değişkene ata
        val egzersiz = argumanlar.egzersizDetay

        // Verileri arayüzdeki elemanlara yerleştir
        uiGuncelle(egzersiz)

        // Butonun tıklama olayını ayarla
        butonlariAyarla(egzersiz)
    }

    private fun uiGuncelle(egzersiz: EgzersizDetay) {
        binding.apply {
            tvExerciseTitle.text = getExerciseName(egzersiz.gorunenAd)
            tvInstructions.text = getStringResource(egzersiz.aciklama)
            tvTargetMuscles.text = getStringResource(egzersiz.hedefKaslar)
            tvDuration.text = getStringResource(egzersiz.tahminiSure)
            tvDifficulty.text = getStringResource(egzersiz.zorlukSeviyesi)

            // Glide kütüphanesi ile GIF'i yükle
            Glide.with(this@ExerciseDetailFragment)
                .asGif()
                .load(egzersiz.gifKaynakId)
                .into(ivExerciseAnimation)
        }
    }

    private fun butonlariAyarla(egzersiz: EgzersizDetay) {
        binding.btnStartWorkout.setOnClickListener {
            // Egzersiz ekranına geçerken sadece kod adını gönderiyoruz.
            val gecisAction = ExerciseDetailFragmentDirections
                .exercisdendetaya(egzersiz.kodAdi)
            findNavController().navigate(gecisAction)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    private fun getExerciseName(exerciseName: String): String {
        return when (exerciseName) {
            "Squat" -> getString(R.string.squat)
            "Push-Up" -> getString(R.string.push_up)
            "Bicep Curl" -> getString(R.string.bicep_curl)
            "Shoulder Press" -> getString(R.string.shoulder_press)
            "Lunge" -> getString(R.string.lunge)
            "Dumbbell Fly" -> getString(R.string.dumbbell_fly)
            "Bench Press" -> getString(R.string.bench_press)
            "Plank" -> getString(R.string.plank)
            "Pull-up" -> getString(R.string.pull_up)
            "Lat Pulldown" -> getString(R.string.lat_pulldown)
            "Hammer Curl" -> getString(R.string.hammer_curl)
            "Cable Pushdown" -> getString(R.string.cable_pushdown)
            "Lateral Raise" -> getString(R.string.lateral_raise)
            "Front Raise" -> getString(R.string.front_raise)
            "Deadlift" -> getString(R.string.deadlift)
            else -> exerciseName
        }
    }
    
    private fun getStringResource(resourceKey: String): String {
        return when (resourceKey) {
            "leg_muscles" -> getString(R.string.leg_muscles)
            "chest_shoulder_tricep" -> getString(R.string.chest_shoulder_tricep)
            "biceps" -> getString(R.string.biceps)
            "shoulders" -> getString(R.string.shoulders)
            "leg_complex" -> getString(R.string.leg_complex)
            "chest" -> getString(R.string.chest)
            "core_abs" -> getString(R.string.core_abs)
            "lats_biceps_upper_back" -> getString(R.string.lats_biceps_upper_back)
            "forearms_brachialis" -> getString(R.string.forearms_brachialis)
            "triceps" -> getString(R.string.triceps)
            "side_deltoids" -> getString(R.string.side_deltoids)
            "front_deltoids" -> getString(R.string.front_deltoids)
            "full_body" -> getString(R.string.full_body)
            "squat_instructions" -> getString(R.string.squat_instructions)
            "pushup_instructions" -> getString(R.string.pushup_instructions)
            "bicep_instructions" -> getString(R.string.bicep_instructions)
            "shoulder_press_instructions" -> getString(R.string.shoulder_press_instructions)
            "lunge_instructions" -> getString(R.string.lunge_instructions)
            "dumbbell_fly_instructions" -> getString(R.string.dumbbell_fly_instructions)
            "bench_press_instructions" -> getString(R.string.bench_press_instructions)
            "plank_instructions" -> getString(R.string.plank_instructions)
            "pull_up_instructions" -> getString(R.string.pull_up_instructions)
            "lat_pulldown_instructions" -> getString(R.string.lat_pulldown_instructions)
            "hammer_curl_instructions" -> getString(R.string.hammer_curl_instructions)
            "cable_pushdown_instructions" -> getString(R.string.cable_pushdown_instructions)
            "lateral_raise_instructions" -> getString(R.string.lateral_raise_instructions)
            "front_raise_instructions" -> getString(R.string.front_raise_instructions)
            "deadlift_instructions" -> getString(R.string.deadlift_instructions)
            "beginner" -> getString(R.string.beginner)
            "intermediate" -> getString(R.string.intermediate)
            "hard" -> getString(R.string.hard)
            "advanced" -> getString(R.string.advanced)
            "duration_1_3_min" -> getString(R.string.duration_1_3_min)
            "duration_2_4_min" -> getString(R.string.duration_2_4_min)
            "duration_3_5_min" -> getString(R.string.duration_3_5_min)
            "duration_3_6_min" -> getString(R.string.duration_3_6_min)
            "duration_4_6_min" -> getString(R.string.duration_4_6_min)
            "duration_4_7_min" -> getString(R.string.duration_4_7_min)
            "duration_5_8_min" -> getString(R.string.duration_5_8_min)
            "duration_7_12_min" -> getString(R.string.duration_7_12_min)
            else -> resourceKey
        }
    }
}