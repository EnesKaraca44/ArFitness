package com.example.arfitness.fragment

import android.content.Context
import android.content.pm.ApplicationInfo
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
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.arfitness.R
import com.example.arfitness.analysis.ExerciseAnalyzer
import com.example.arfitness.analysis.ExerciseType
import com.example.arfitness.analysis.Feedback
import com.example.arfitness.analysis.SignalCsv
import com.example.arfitness.databinding.FragmentExerciseBinding
import com.example.arfitness.debug.SignalRecorder
import com.example.arfitness.entitiy.PoseAnalyzer
import com.example.arfitness.entitiy.RepAnnouncer
import com.example.arfitness.entitiy.WorkoutResult
import com.example.arfitness.viewmodel.ExerciseViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@AndroidEntryPoint
class ExerciseFragment : Fragment() {
    private var _binding: FragmentExerciseBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ExerciseViewModel by viewModels()
    private val args: ExerciseFragmentArgs by navArgs()

    private lateinit var exerciseType: ExerciseType
    private lateinit var exerciseAnalyzer: ExerciseAnalyzer

    // Kamera işlemleri için gerekli değişkenler
    private var kameraCalistirici: ExecutorService? = null

    // Yalnızca debug derlemelerinde: sayaca giden sinyalin kaydı (CSV, ekranda görünmez)
    private var signalRecorder: SignalRecorder? = null

    // Sesli + titreşimli sayım
    private var repAnnouncer: RepAnnouncer? = null
    private var lastAnnouncedHoldSeconds = 0L

    // PoseAnalyzer callback
    private val poseAnalyzer = PoseAnalyzer { pose, width, height, timestampMs ->
        if (!::exerciseAnalyzer.isInitialized) return@PoseAnalyzer

        // 1. Seçili egzersiz analizörünü çalıştır
        val feedbackRaporu = exerciseAnalyzer.analyze(pose, timestampMs)

        // 2. Rapordaki bilgileri ViewModel'e ilet (thread-safe postValue kullanılır)
        if (exerciseAnalyzer.isTimed) {
            viewModel.onHoldFrame(feedbackRaporu.isHolding, timestampMs)
        } else if (feedbackRaporu.isRepCounted) {
            viewModel.onRepCounted()
            repAnnouncer?.announceRep(viewModel.getRepCount())
        }
        feedbackRaporu.message?.let { mesaj ->
            viewModel.updateFeedback(mesaj, feedbackRaporu.isWarning)
            if (feedbackRaporu.isWarning && !feedbackRaporu.isRepCounted) {
                repAnnouncer?.announceWarning(mesaj, timestampMs)
            }
        }

        signalRecorder?.let { recordSignal(it, feedbackRaporu, timestampMs) }

        // 3. İskeleti ekrana çizen OverlayView'ı UI Thread'de güncelle
        activity?.runOnUiThread {
            _binding?.overlayView?.setPose(pose, width, height, isFrontCamera = true)
        }
    }

    private fun recordSignal(recorder: SignalRecorder, feedback: Feedback, timestampMs: Long) {
        val state = exerciseAnalyzer.debugState ?: return
        recorder.record(
            SignalCsv.Row(timestampMs, state.values, state.phase, feedback.isRepCounted, feedback.isHolding)
        )
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

        val type = ExerciseType.fromName(args.exerciseName)
        if (type == null) {
            // Eskiden tanınmayan her isim sessizce Push-Up analizörüne düşüyordu
            Log.e("ARFitForm", "Tanınmayan egzersiz: ${args.exerciseName}")
            Toast.makeText(requireContext(), R.string.unknown_exercise, Toast.LENGTH_LONG).show()
            findNavController().navigateUp()
            return
        }
        exerciseType = type
        exerciseAnalyzer = type.createAnalyzer()

        val isDebugBuild = (requireContext().applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        if (isDebugBuild) {
            signalRecorder = SignalRecorder(requireContext(), type.name.lowercase())
        }
        setupVoiceCounting()

        viewModel.reset()
        binding.exerciseTitle.text = getString(type.titleRes)
        showCameraHint(type)
        binding.repLabelText.setText(if (exerciseAnalyzer.isTimed) R.string.hold_time_label else R.string.repetitions)
        observeViewModel()
        setupButtons()

        kameraCalistirici = Executors.newSingleThreadExecutor()

        viewLifecycleOwner.lifecycleScope.launch {
            poseAnalyzer.hazirla()
            Log.d("ARFitForm", "PoseAnalyzer hazırlandı.")
            delay(1000L)
            kameraIzniniKontrolEt()
        }
    }

    /**
     * Sesli sayım: tercih "arfitness_prefs" içinde saklanır (varsayılan açık).
     * Seslendirme uygulamanın seçili dilinde yapılır.
     */
    private fun setupVoiceCounting() {
        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val voiceOn = prefs.getBoolean(KEY_VOICE_COUNT, true)
        val locale = resources.configuration.locales[0] ?: Locale.getDefault()
        repAnnouncer = RepAnnouncer(requireContext(), locale, voiceOn)
        updateVoiceToggleIcon(voiceOn)

        binding.voiceToggleButton.setOnClickListener {
            val enabled = !(repAnnouncer?.voiceEnabled ?: voiceOn)
            repAnnouncer?.voiceEnabled = enabled
            prefs.edit().putBoolean(KEY_VOICE_COUNT, enabled).apply()
            updateVoiceToggleIcon(enabled)
            Toast.makeText(
                requireContext(),
                if (enabled) R.string.voice_count_on else R.string.voice_count_off,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun updateVoiceToggleIcon(enabled: Boolean) {
        binding.voiceToggleButton.setImageResource(if (enabled) R.drawable.ic_volume_up else R.drawable.ic_volume_off)
        binding.voiceToggleButton.contentDescription =
            getString(if (enabled) R.string.voice_count_turn_off else R.string.voice_count_turn_on)
    }

    /** Telefonun nereye konulacağını başlangıçta gösterir; dokununca ya da birkaç saniye sonra kapanır. */
    private fun showCameraHint(type: ExerciseType) {
        binding.cameraHintText.text = getString(R.string.camera_placement_title) + "\n" + getString(type.cameraHintRes)
        binding.cameraHintCard.visibility = View.VISIBLE
        binding.cameraHintCard.setOnClickListener { it.visibility = View.GONE }
        viewLifecycleOwner.lifecycleScope.launch {
            delay(CAMERA_HINT_MS)
            _binding?.cameraHintCard?.visibility = View.GONE
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
            val isTimed = exerciseAnalyzer.isTimed
            val anlikTekrar = if (isTimed) 0 else viewModel.getRepCount()
            // Süreli hareketlerde (Plank) kaydedilen süre, doğru pozisyonda geçen tutma süresidir
            val gecenSure = if (isTimed) viewModel.getHoldSeconds() else viewModel.getElapsedSeconds()
            val egzersizAdi = getString(exerciseType.titleRes)
            val formPuani = exerciseAnalyzer.sessionFormScore

            viewModel.saveWorkout(egzersizAdi, anlikTekrar, gecenSure, isTimed, formPuani)

            val sonuc = WorkoutResult(
                exerciseName = egzersizAdi,
                totalReps = anlikTekrar,
                durationInSeconds = gecenSure,
                isTimed = isTimed,
                formScore = formPuani
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
                viewModel.startTimer()
                Log.d("Kamera", "Kamera ve görüntü analizi başarıyla bağlandı.")
            } catch (hata: Exception) {
                Log.e("Kamera", "Kamera bağlanırken hata oluştu.", hata)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    private fun observeViewModel() {
        if (exerciseAnalyzer.isTimed) {
            viewModel.holdSeconds.observe(viewLifecycleOwner) { seconds ->
                binding.repCountText.text = String.format("%d:%02d", seconds / 60, seconds % 60)
                // Plank: her 10 saniyede bir sesli bildirim ("30 saniye")
                if (seconds > 0 && seconds % HOLD_ANNOUNCE_EVERY_S == 0L && seconds != lastAnnouncedHoldSeconds) {
                    lastAnnouncedHoldSeconds = seconds
                    repAnnouncer?.announceText(getString(R.string.hold_seconds_spoken, seconds.toInt()))
                }
            }
        } else {
            viewModel.tekrarSayisi.observe(viewLifecycleOwner) { count ->
                binding.repCountText.text = count.toString()
            }
        }

        viewModel.feedback.observe(viewLifecycleOwner) { feedback ->
            binding.feedbackText.text = feedback.message
            // Form hatası / görünürlük uyarısı kırmızı, diğer mesajlar mavi
            val renk = if (feedback.isWarning) R.color.feedback_warning else R.color.feedback_info
            binding.feedbackCard.setCardBackgroundColor(ContextCompat.getColor(requireContext(), renk))
        }

        viewModel.timerValueString.observe(viewLifecycleOwner) { timeString ->
            binding.timerText.text = timeString
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        signalRecorder?.close()
        signalRecorder = null
        repAnnouncer?.release()
        repAnnouncer = null
        _binding = null
    }

    override fun onPause() {
        viewModel.pauseTimer()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        viewModel.resumeTimer()
    }

    override fun onDestroy() {
        super.onDestroy()
        kameraCalistirici?.shutdown()
        Log.d("Kamera", "Kamera Çalıştırıcı kapatıldı.")
    }

    private companion object {
        const val CAMERA_HINT_MS = 10_000L
        const val PREFS_NAME = "arfitness_prefs"
        const val KEY_VOICE_COUNT = "voice_count_enabled"
        const val HOLD_ANNOUNCE_EVERY_S = 10L
    }
}
