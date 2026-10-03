package com.example.arfitness.fragment

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.arfitness.analysis.BicepCurlAnalyzer
import com.example.arfitness.analysis.DumbbellFlyAnalyzer
import com.example.arfitness.analysis.ExerciseAnalyzer
import com.example.arfitness.analysis.LungeAnalyzer
import com.example.arfitness.analysis.PushupAnalyzer
import com.example.arfitness.analysis.ShoulderPressAnalyzer
import com.example.arfitness.analysis.SquatAnalyzer
import com.example.arfitness.databinding.FragmentExerciseBinding
import com.example.arfitness.entitiy.PoseAnalyzer
import com.example.arfitness.entitiy.WorkoutResult
import com.example.arfitness.viewmodel.ExerciseViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@AndroidEntryPoint
class ExerciseFragment : Fragment() {
    private var _binding: FragmentExerciseBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ExerciseViewModel by viewModels()
    private val args: ExerciseFragmentArgs by navArgs()

    private lateinit var exerciseAnalyzer: ExerciseAnalyzer

    // Kamera işlemleri için gerekli değişkenler
    private var resimYakala: ImageCapture? = null
    private var kameraCalistirici: ExecutorService? = null

    // PoseAnalyzer callback
    private val poseAnalyzer = PoseAnalyzer { pose, width, height ->
        if (!::exerciseAnalyzer.isInitialized) return@PoseAnalyzer

        // 1. Seçili egzersiz analizörünü çalıştır
        val feedbackRaporu = exerciseAnalyzer.analyze(pose)

        // 2. Rapordaki bilgileri ViewModel'e ilet (thread-safe postValue kullanılır)
        if (feedbackRaporu.isRepCounted) {
            viewModel.onRepCounted()
        }
        feedbackRaporu.message?.let { mesaj ->
            viewModel.updateFeedback(mesaj)
        }

        // 3. İskeleti ekrana çizen OverlayView'ı UI Thread'de güncelle
        activity?.runOnUiThread {
            _binding?.overlayView?.setPose(pose, width, height, isFrontCamera = true)
        }
    }

    private val izinIstekBaslatici =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { izinVerildi ->
            if (izinVerildi) {
                kamerayiBaslat()
            } else {
                izinDialogunuGoster()
            }
        }

    private fun izinDialogunuGoster() {
        val ctx = context ?: return
        com.google.android.material.dialog.MaterialAlertDialogBuilder(ctx)
            .setTitle("Kamera İzni Gerekli")
            .setMessage("Egzersiz formunuzu yapay zeka ile analiz edebilmemiz için kamera iznine ihtiyacımız var. Kamera görüntüleriniz tamamen cihaz üzerinde işlenir, hiçbir sunucuya kaydedilmez veya aktarılmaz.")
            .setPositiveButton("Ayarları Aç") { _, _ ->
                val intent = android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = android.net.Uri.fromParts("package", requireContext().packageName, null)
                }
                startActivity(intent)
            }
            .setNegativeButton("Geri Dön") { _, _ ->
                findNavController().navigateUp()
            }
            .setCancelable(false)
            .show()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExerciseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.reset()
        setupAnalyzer()
        displayTitle()
        observeViewModel()
        setupButtons()

        viewModel.startTimer(60)

        kameraCalistirici = Executors.newSingleThreadExecutor()

        viewLifecycleOwner.lifecycleScope.launch {
            poseAnalyzer.hazirla()
            Log.d("ARFitForm", "PoseAnalyzer hazırlandı.")
            delay(1000L)
            kameraIzniniKontrolEt()
        }
    }

    private fun displayTitle() {
        val title = when (args.exerciseName.lowercase().trim()) {
            "squat" -> "Squat"
            "push_up", "push-up", "pushup", "push-ups" -> "Push-Up"
            "bicep_curl", "bicep curl", "biceps" -> "Bicep Curl"
            "shoulder_press", "shoulder press" -> "Shoulder Press"
            "lunge" -> "Lunge"
            "dumbbell_fly", "dumbbell fly", "dumbell fly" -> "Dumbbell Fly"
            else -> args.exerciseName
        }
        binding.exerciseTitle.text = title
    }

    private fun setupAnalyzer() {
        val rawName = args.exerciseName.lowercase().trim()
        exerciseAnalyzer = when {
            rawName.contains("squat") -> SquatAnalyzer()
            rawName.contains("push") -> PushupAnalyzer()
            rawName.contains("bicep") -> BicepCurlAnalyzer()
            rawName.contains("shoulder") -> ShoulderPressAnalyzer()
            rawName.contains("lunge") -> LungeAnalyzer()
            rawName.contains("fly") || rawName.contains("dumb") -> DumbbellFlyAnalyzer()
            else -> PushupAnalyzer()
        }
    }

    private fun kameraIzniniKontrolEt() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                kamerayiBaslat()
            }
            else -> {
                izinIstekBaslatici.launch(android.Manifest.permission.CAMERA)
            }
        }
    }

    private fun setupButtons() {
        binding.finishButton.setOnClickListener {
            viewModel.stopTimer()
            val anlikTekrar = viewModel.tekrarSayisi.value ?: 0
            val gecenSure = viewModel.getElapsedSeconds()
            val egzersizAdi = binding.exerciseTitle.text.toString()

            viewModel.saveWorkout(egzersizAdi, anlikTekrar, gecenSure)

            val sonuc = WorkoutResult(
                exerciseName = egzersizAdi,
                totalReps = anlikTekrar,
                durationInSeconds = gecenSure
            )

            val action = ExerciseFragmentDirections.exercisidenresulta(sonuc)
            findNavController().navigate(action)
        }
    }

    private fun kamerayiBaslat() {
        val context = context ?: return
        val kameraSaglayiciGelecegi = ProcessCameraProvider.getInstance(context)

        kameraSaglayiciGelecegi.addListener({
            try {
                val kameraSaglayici: ProcessCameraProvider = kameraSaglayiciGelecegi.get()

                val onizleme = Preview.Builder()
                    .build()
                    .also {
                        it.setSurfaceProvider(binding.previewView.surfaceProvider)
                    }

                val goruntuAnalizi = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also { analysis ->
                        kameraCalistirici?.let { executor ->
                            analysis.setAnalyzer(executor, poseAnalyzer)
                        }
                    }

                val kameraSecici = CameraSelector.DEFAULT_FRONT_CAMERA

                kameraSaglayici.unbindAll()
                kameraSaglayici.bindToLifecycle(
                    viewLifecycleOwner, kameraSecici, onizleme, goruntuAnalizi
                )
                Log.d("Kamera", "Kamera ve görüntü analizi başarıyla bağlandı.")
            } catch (hata: Exception) {
                Log.e("Kamera", "Kamera bağlanırken hata oluştu.", hata)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    private fun observeViewModel() {
        viewModel.tekrarSayisi.observe(viewLifecycleOwner) { count ->
            binding.repCountText.text = count.toString()
        }

        viewModel.feedbackMessage.observe(viewLifecycleOwner) { message ->
            binding.feedbackText.text = message
        }

        viewModel.timerValueString.observe(viewLifecycleOwner) { timeString ->
            binding.timerText.text = timeString
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        kameraCalistirici?.shutdown()
        Log.d("Kamera", "Kamera Çalıştırıcı kapatıldı.")
    }
}
