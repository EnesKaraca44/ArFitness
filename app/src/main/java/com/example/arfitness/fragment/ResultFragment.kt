package com.example.arfitness.fragment

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.arfitness.R
import com.example.arfitness.databinding.FragmentResultBinding
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

@AndroidEntryPoint
class ResultFragment : Fragment() {
    private var _binding: FragmentResultBinding? = null
    private val binding get() = _binding!!

    private val args: ResultFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val result = args.workoutResult
        val timeFormatted = formatTime(result.durationInSeconds)

        // 1. Üst Başlık & Egzersiz Bilgisi
        binding.tvHeaderExercise.text = result.exerciseName

        // 2. Performans Metrikleri
        binding.tvMetricDuration.text = timeFormatted

        val calculatedCalories = if (result.isTimed) {
            (result.durationInSeconds * 4 / 60).toInt().coerceAtLeast(3)
        } else {
            (result.totalReps * 1.5).toInt().coerceAtLeast(4)
        }
        binding.tvMetricCalories.text = "$calculatedCalories kcal"

        if (result.isTimed) {
            // Plank gibi süreli egzersizler
            binding.tvMetricRepsLabel.text = getString(R.string.metric_hold_time)
            binding.tvMetricReps.text = timeFormatted
        } else {
            binding.tvMetricRepsLabel.text = getString(R.string.metric_reps)
            binding.tvMetricReps.text = "${result.totalReps} Tekrar"
        }

        val score = result.formScore ?: 88
        binding.tvMetricFormScore.text = "%$score"

        // 3. Story Kartı Alanı (Paylaşılacak Tasarım)
        binding.tvStoryExerciseBadge.text = result.exerciseName.uppercase(Locale.getDefault())
        binding.tvStoryBigTitle.text = "${result.exerciseName.uppercase(Locale.getDefault())} SEANSI\nBAŞARIYLA BİTTİ!"
        binding.tvStoryDuration.text = timeFormatted
        binding.tvStoryCalories.text = "$calculatedCalories kcal"
        binding.tvStoryScore.text = "%$score"

        // 4. Aksiyon Butonları
        binding.btnBack.setOnClickListener { navigateToHome() }
        binding.btnGoHome.setOnClickListener { navigateToHome() }
        binding.btnTopShare.setOnClickListener { shareStoryCard() }
        binding.btnShareStory.setOnClickListener { shareStoryCard() }
    }

    private fun navigateToHome() {
        findNavController().navigate(R.id.anasayfaFragment)
    }

    private fun shareStoryCard() {
        val storyCard = binding.cardStory
        if (storyCard.width <= 0 || storyCard.height <= 0) {
            Toast.makeText(requireContext(), "Kart hazırlanıyor...", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            // Kartın yüksek kaliteli Bitmap görüntüsünü oluştur
            val bitmap = Bitmap.createBitmap(storyCard.width, storyCard.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            storyCard.draw(canvas)

            // Cache klasörüne kaydet
            val imagesFolder = File(requireContext().cacheDir, "shared_images").apply { mkdirs() }
            val imageFile = File(imagesFolder, "arfitness_story_${System.currentTimeMillis()}.png")
            FileOutputStream(imageFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }

            // FileProvider ile güvenli URI al
            val authority = "${requireContext().packageName}.fileprovider"
            val contentUri = FileProvider.getUriForFile(requireContext(), authority, imageFile)

            // Android Paylaşım Menüsünü Aç
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_TEXT, getString(R.string.share_message))
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            startActivity(Intent.createChooser(shareIntent, getString(R.string.share_title)))
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Paylaşım açılamadı: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun formatTime(seconds: Long): String {
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, remainingSeconds)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
