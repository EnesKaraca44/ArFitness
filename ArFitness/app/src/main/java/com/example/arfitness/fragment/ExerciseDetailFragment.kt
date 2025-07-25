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
            tvExerciseTitle.text = egzersiz.gorunenAd
            tvInstructions.text = egzersiz.aciklama
            tvTargetMuscles.text = egzersiz.hedefKaslar

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
}