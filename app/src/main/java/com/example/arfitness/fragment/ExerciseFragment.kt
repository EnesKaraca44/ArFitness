package com.example.arfitness.fragment

import androidx.core.content.ContextCompat
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.core.Preview
import android.util.Log
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.navigation.NavArgs
import androidx.navigation.fragment.navArgs
import com.example.arfitness.R
import com.example.arfitness.databinding.FragmentExerciseBinding
import com.example.arfitness.entitiy.PoseAnalyzer
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import androidx.lifecycle.lifecycleScope
import com.example.arfitness.analysis.SquatAnalyzer
import com.example.arfitness.viewmodel.ExerciseViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.arfitness.analysis.ExerciseAnalyzer
import com.example.arfitness.analysis.LungeAnalyzer
import kotlinx.coroutines.flow.collectLatest
import com.example.arfitness.analysis.PushupAnalyzer
import com.example.arfitness.analysis.BicepCurlAnalyzer
import com.example.arfitness.analysis.DumbbellFlyAnalyzer
import com.example.arfitness.analysis.ShoulderPressAnalyzer
import com.example.arfitness.entitiy.WorkoutResult
import android.speech.tts.TextToSpeech
import com.example.arfitness.analysis.BenchPressAnalyzer
import com.example.arfitness.analysis.CablePushdownAnalyzer
import com.example.arfitness.analysis.DeadliftAnalyzer
import com.example.arfitness.analysis.FrontRaiseAnalyzer
import com.example.arfitness.analysis.HammerCurlAnalyzer
import com.example.arfitness.analysis.LatPulldownAnalyzer
import com.example.arfitness.analysis.LateralRaiseAnalyzer
import com.example.arfitness.analysis.PlankAnalyzer
import com.example.arfitness.analysis.PullupAnalyzer
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark
import java.util.Locale
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ExerciseFragment : Fragment(),TextToSpeech.OnInitListener {
    private lateinit var tts: TextToSpeech
    private lateinit var binding: FragmentExerciseBinding
    private val viewModel: ExerciseViewModel by viewModels()
    private val squatAnalyzer = SquatAnalyzer()

    private lateinit var exerciseAnalyzer: ExerciseAnalyzer
    private val args: ExerciseFragmentArgs by navArgs()


    private var isPositionCorrect = false

    // Kamera işlemleri için gerekli değişkenler
    private var resimYakala: ImageCapture? = null
    private lateinit var kameraCalistirici: ExecutorService

    // PoseAnalyzer'ı bir sınıf değişkeni olarak oluşturuyoruz.
    // Bu, her iskelet bulunduğunda ne yapılacağını tanımlayan bir lambda fonksiyonu alır.
    private val poseAnalyzer = PoseAnalyzer { pose, width, height ->

        checkUserPosition(pose)
        if (isPositionCorrect) {
            val feedbackRaporu = exerciseAnalyzer.analyze(pose)
            // ... ViewModel'i güncelleme kodları ...
        }

        activity?.runOnUiThread {
            // İskeleti sadece pozisyon doğruysa çiz (isteğe bağlı)
            if (isPositionCorrect) {
                binding.overlayView.setPose(pose, width, height)
            }
        }

        val feedbackRaporu = squatAnalyzer.analyze(pose)

        // 2. Rapordaki bilgileri işle.
        // Eğer raporda "yeni bir tekrar sayıldı" işareti varsa:
        if (feedbackRaporu.isRepCounted) {
            // ViewModel'e "tekrar sayısını bir artır" komutu gönder.
            viewModel.onRepCounted()
        }
        // Eğer raporda bir mesaj varsa (örn: "Sırtını dik tut!")
        feedbackRaporu.message?.let { mesaj ->
            // ViewModel'e "geri bildirim mesajını güncelle" komutu gönder.
            viewModel.updateFeedback(mesaj)
        }
        // Her iskelet tespit edildiğinde bu blok çalışır.
        // Bu blok ana iş parçacığında (UI Thread) çalışmaz, bu yüzden UI güncellemeleri
        // için `activity?.runOnUiThread { ... }` kullanmalıyız.
        activity?.runOnUiThread {
            // TODO: Adım 3 - İskeleti ekrana çizen OverlayView'ı burada güncelle.
            // binding.poseOverlayView.setPose(pose)
            Log.d(
                "PoseAnalyzerCallback",
                "İskelet verisi geldi! Nokta sayısı: ${pose.allPoseLandmarks.size}"
            )
            activity?.runOnUiThread {
                binding.overlayView.setPose(pose, width, height)
            }

        }
    }
    // --- YENİ YARDIMCI FONKSİYON ---
    private fun checkUserPosition(pose: Pose) {
        // O anki egzersizin ihtiyaç duyduğu noktaları al
        val required = exerciseAnalyzer.requiredLandmarks

        // Görüntüdeki mevcut noktaları al
        val visible = pose.allPoseLandmarks.map { it.landmarkType }

        // Gerekli tüm noktalar, görünür noktaların içinde mi?
        val allVisible = visible.containsAll(required)

        if (allVisible) {
            if (!isPositionCorrect) { // Eğer daha önce pozisyon hatalıysa, şimdi düzeldi
                isPositionCorrect = true
                viewModel.updateFeedback("Pozisyon harika! Başlayabilirsin.")
            }
        } else {
            isPositionCorrect = false
            // --- AKILLI TAVSİYE ---
            // Gerekli olan ama görünmeyen İLK noktayı bul
            val missingLandmark = required.firstOrNull { !visible.contains(it) }

            // Eksik noktaya göre tavsiye ver
            val advice = when (missingLandmark) {
                PoseLandmark.LEFT_ANKLE, PoseLandmark.RIGHT_ANKLE -> "Ayakların görünmüyor. Biraz geri gitmeyi dene."
                PoseLandmark.LEFT_WRIST, PoseLandmark.RIGHT_WRIST -> "Kolların tam görünmüyor."
                PoseLandmark.LEFT_SHOULDER, PoseLandmark.RIGHT_SHOULDER -> "Lütfen kameranın karşısına tam olarak geç."
                else -> "Vücudunun tamamını kadraja sığdır."
            }
            viewModel.updateFeedback(advice)
        }
    }


    private val izinIstekBaslatici =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()  //boolern değeri sorar
        ) { izinVerildi ->
            // Kullanıcı izin isteğine cevap verdiğinde bu blok çalışır.
            if (izinVerildi) {
                // İzin verildiyse, kamerayı başlat.
                kamerayiBaslat()

            } else {
                // İzin verilmediyse, kullanıcıya bir uyarı göster.
                Toast.makeText(
                    requireContext(),
                    "Uygulamanın çalışması için kamera izni gereklidir.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentExerciseBinding.inflate(inflater, container, false)

        val bundle: ExerciseFragmentArgs by navArgs()

        //val gelenad=bundle.exercise.name
        // val gelenresim=bundle.exercise.imageResId
binding.exerciseTitle.text=args.exerciseName

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tts = TextToSpeech(requireContext(), this)

        // Initialize feedback message with string resource
        viewModel.updateFeedbackMessage(getString(R.string.get_ready))

        observeViewModel()
        setupAnalyzer()
        setupButtons() //Butonların tıklama olaylarını ayarlayan fonksiyonu çağır.
        viewModel.startTimer(60)

        // Kamera işlemlerini ana uygulama akışını (UI thread) yormadan,
        // arka planda çalıştıracak bir servis oluşturuyoruz.
        kameraCalistirici = Executors.newSingleThreadExecutor()

        // Coroutine kullanarak süreci başlatıyoruz.
        // Bu, uygulamanın donmasını engeller.
        viewLifecycleOwner.lifecycleScope.launch {
            // 1. Adım (Arka Planda): Ağır olan yapay zeka modelini hazırla.
            poseAnalyzer.hazirla()
            Log.d("ARFitForm", "PoseAnalyzer hazırlandı.")

            // 2. Adım: Hazırlık bittikten sonra, kamera iznini kontrol et ve kamerayı başlat.


            // ML Kit'in arka plandaki ağır model yükleme işlemlerini bitirmesi için
            // ona birkaç saniye "nefes alma" süresi veriyoruz.
            delay(2000L) // 2 saniye bekle
            Log.d("ARFitForm", "2 saniye beklendi, şimdi kamera başlatılıyor.")
            // --- DEĞİŞİKLİK BİTTİ ---


            // Kamera izni kontrol sürecini başlatıyoruz.
            kameraIzniniKontrolEt()
        }
    }

    // : Sesli geri bildirim için bir fonksiyon ---
    private fun speak(text: String) {
        // Motorun meşgul olup olmadığını kontrol et ve sesi çal
        if (tts.isSpeaking) {
            tts.stop()
        }
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
    }
    // --- YENİ: OnInitListener'dan gelen zorunlu metot ---
    // Bu metot, TTS motoru hazır olduğunda otomatik olarak çağrılır.
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            // Dil olarak Türkçe'yi ayarla
            val result = tts.setLanguage(Locale("tr", "TR"))
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "Türkçe dili bu cihazda desteklenmiyor.")
            } else {
                Log.i("TTS", "TextToSpeech motoru başarıyla başlatıldı.")
            }
        } else {
            Log.e("TTS", "TextToSpeech motoru başlatılamadı!")
        }
    }


    private fun kameraIzniniKontrolEt() {
        when {
            // İzin zaten verilmiş mi diye kontrol ediyoruz.
            ContextCompat.checkSelfPermission( //Bu metod, uygulamanızın belirli bir izne  sahip olup olmadığını kontrol eder.
                requireContext(), //int döndürür
                android.Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                Log.d("Kameraİzni", "İzin zaten verilmiş. Kamera başlatılıyor.")
                kamerayiBaslat()

            }

            else -> {
                // İzin daha önce verilmemiş, kullanıcıdan izin istiyoruz.
                Log.d("Kameraİzni", "İzin isteniyor...")
                izinIstekBaslatici.launch(android.Manifest.permission.CAMERA)
            }
        }
    }

    private fun setupButtons() {
        // XML'deki 'Antrenmanı Bitir' butonunu bul ve tıklama dinleyicisi ata.
        binding.finishButton.setOnClickListener {

            // 1. O anki verileri ViewModel'den al.
            // .value kullandığımız için LiveData'nın en son değerini anında alırız.
            val anlikTekrar = viewModel.tekrarSayisi.value ?: 0

            // ViewModel'e, o anki zamanı döndüren bir fonksiyon eklemek daha iyi olur.
            // Şimdilik varsayımsal bir süre kullanalım.
            val gecenSure = 60L // TODO: Bu kısmı ViewModel'den gelen gerçek süre ile değiştir.

            val egzersizAdi = args.exerciseName
            viewModel.saveWorkout(egzersizAdi, anlikTekrar, gecenSure)

            // 2. Bu verilerle bir Sonuç (WorkoutResult) nesnesi oluştur.
            val sonuc = WorkoutResult(
                exerciseName = egzersizAdi,
                totalReps = anlikTekrar,
                durationInSeconds = gecenSure
            )


            val action = ExerciseFragmentDirections.exercisidenresulta(sonuc)
            findNavController().navigate(action)
        }
    }

    /**

     * Kameranın önizlemesini ekrandaki `PreviewView`'a bağlar.
     */
    private fun kamerayiBaslat() {
        val kameraSaglayiciGelecegi = ProcessCameraProvider.getInstance(requireContext())

        kameraSaglayiciGelecegi.addListener({
            val kameraSaglayici: ProcessCameraProvider = kameraSaglayiciGelecegi.get()

            val onizleme = Preview.Builder()
                .setTargetResolution(android.util.Size(1920, 1080)) // ✅ Geniş aspect ratio ekledik
                .build()
                .also {
                    it.setSurfaceProvider(binding.previewView.surfaceProvider)
                }

            val goruntuAnalizi = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(kameraCalistirici, poseAnalyzer)
                }

            val kameraSecici = CameraSelector.DEFAULT_FRONT_CAMERA

            try {
                kameraSaglayici.unbindAll()

                val kamera = kameraSaglayici.bindToLifecycle(
                    this, kameraSecici, onizleme, goruntuAnalizi
                )

                // ✅ Kamera zoom oranını sıfırladık
                kamera.cameraControl.setZoomRatio(1.0f)

                Log.d("Kamera", "Kamera ve görüntü analizi başarıyla bağlandı.")
            } catch (hata: Exception) {
                Log.e("Kamera", "Kamera bağlanırken bir hata oluştu.", hata)
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }


    /**
     * Fragment yok edildiğinde (örneğin kullanıcı geri tuşuna bastığında),
     * oluşturduğumuz `kameraCalistirici` servisini kapatarak kaynak sızıntısını önleriz.
     */
    override fun onDestroy() {
        // 'tts' değişkeninin başlatılıp başlatılmadığını kontrol et
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
        super.onDestroy()
        kameraCalistirici.shutdown()
        Log.d("Kamera", "Kamera Çalıştırıcı kapatıldı.")
    }

    private fun observeViewModel() {
        // Tekrar sayısı değiştiğinde, ekrandaki TextView'ı güncelle.
        viewModel.tekrarSayisi.observe(viewLifecycleOwner) { count ->
            binding.repCountText.text = count.toString()
        }

        // Geri bildirim mesajı değiştiğinde, ekrandaki TextView'ı güncelle.
        viewModel.feedbackMessage.observe(viewLifecycleOwner) { message ->
            binding.feedbackText.text = message
            if (message != null) {
                speak(message)
            }
            viewModel.timerValueString.observe(viewLifecycleOwner) { timeString ->
                binding.timerText.text = timeString
            }
        }
        viewModel.timerValueString.observe(viewLifecycleOwner) { timeString ->
            Log.d("TimerDebug", "Fragment, LiveData'dan yeni zamanı aldı: $timeString")
            binding.timerText.text = timeString
        }
    }

    private fun setupAnalyzer() {
        // Navigasyondan gelen egzersiz adını al (bu adı nav_graph'ta tanımlamalısın)
        val exerciseName = args.exerciseName // "push-ups", "squat" vb.

        // Gelen isme göre doğru analizörü ata
        exerciseAnalyzer = when (exerciseName) {
            "push-ups" -> PushupAnalyzer()
            "squat" -> SquatAnalyzer()
            "lunge" -> LungeAnalyzer()
            "bicep curl" -> BicepCurlAnalyzer()
            "shoulder press" -> ShoulderPressAnalyzer()
            "dumbbell fly" -> DumbbellFlyAnalyzer()
            "plank" -> PlankAnalyzer()
            "bench press" -> BenchPressAnalyzer()
            "pull-up" -> PullupAnalyzer()
            "lat pulldown" -> LatPulldownAnalyzer()
            "hammer curl" -> HammerCurlAnalyzer()
            "cable pushdown" -> CablePushdownAnalyzer()
            "lateral raise" -> LateralRaiseAnalyzer()
            "front raise" -> FrontRaiseAnalyzer()
            "deadlift" -> DeadliftAnalyzer()
            else -> PushupAnalyzer() // Varsayılan olarak
        }


    }
}










