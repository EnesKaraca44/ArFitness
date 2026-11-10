package com.example.arfitness.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigator
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.arfitness.R
import com.example.arfitness.databinding.FragmentResultBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ResultFragment : Fragment() {
    private  var _binding: FragmentResultBinding?=null
    private val binding get() = _binding!!

    private val args: ResultFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding= FragmentResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val result = args.workoutResult


        binding.tvExerciseName.text = getString(R.string.exercise_label, result.exerciseName)
        if (result.isTimed) {
            // Plank: tekrar yok, doğru pozisyonda geçen süre gösterilir
            binding.tvTotalReps.text = getString(R.string.hold_time_value, formatTime(result.durationInSeconds))
            binding.tvDuration.visibility = View.GONE
        } else {
            // Not: "total_reps" etiketinde sayı yeri olmadığı için eskiden tekrar sayısı hiç görünmüyordu
            binding.tvTotalReps.text = getString(R.string.total_reps_value, result.totalReps)
            binding.tvDuration.text = getString(R.string.duration_label, formatTime(result.durationInSeconds))
        }
        result.formScore?.let { score ->
            binding.tvFormScore.text = getString(R.string.form_score_value, score)
            binding.tvFormScore.visibility = View.VISIBLE
        }


        binding.btnGoHome.setOnClickListener {

            findNavController().navigate(R.id.anasayfaFragment)
        }
    }

    // Yardımcı zaman formatlama fonksiyonu
    private fun formatTime(seconds: Long): String {
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%02d:%02d", minutes, remainingSeconds)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}