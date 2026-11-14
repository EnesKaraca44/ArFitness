package com.example.arfitness.fragment


import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.NavOptions
import com.example.arfitness.R
import com.example.arfitness.data.User
import com.example.arfitness.data.WorkoutSession
import com.example.arfitness.databinding.FragmentUserBinding
import com.example.arfitness.viewmodel.ProfileViewModel
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import dagger.hilt.android.AndroidEntryPoint
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

@AndroidEntryPoint
class UserFragment : Fragment() {

    private var _binding: FragmentUserBinding? = null
    private val binding get() = _binding!!

    // Hilt ile ProfileViewModel'i alıyoruz
    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ViewModel'den gelen verileri dinlemeye başla
        observeViewModel()

        // Butonların tıklama olaylarını ayarla
        setupButtonClickListeners()
    }

    /**
     * ViewModel'deki LiveData'ları dinler ve UI'ı günceller.
     */
    private fun observeViewModel() {
        // Kullanıcı profili değiştiğinde bu blok çalışır.
        viewModel.userProfile.observe(viewLifecycleOwner) { user ->
            // Eğer veritabanında bir kullanıcı varsa, UI'ı doldur.
            user?.let {
                updateUserProfileUI(it)
            }

            // --- YENİ: Grafik verisini dinle ---
            viewModel.weeklyChartData.observe(viewLifecycleOwner) { weeklyData ->
                // ViewModel'den gelen haftalık veriyle grafiği güncelle
                setupBarChart(weeklyData)
            }
        }

        // Antrenman geçmişi değiştiğinde bu blok çalışır.
        viewModel.workoutHistory.observe(viewLifecycleOwner) { historyList ->
            // Gelen liste ile istatistik kartlarını hesapla ve güncelle.
            updateStatsUI(historyList)
        }
    }




    /**
     * Gelen 'User' nesnesiyle profil kartını doldurur.
     */
    private fun updateUserProfileUI(user: User) {
        binding.tvUserName.text = user.name
        binding.tvUserEmail.text = user.email

        val metrics = buildList {
            if (user.age > 0) add(getString(R.string.age_years_format, user.age))
            if (user.height > 0) add(getString(R.string.height_value_format, user.height))
            if (user.weight > 0) add(getString(R.string.weight_value_format, user.weight))
        }
        binding.tvUserMetrics.text = metrics.joinToString(" · ")
        binding.tvUserMetrics.visibility = if (metrics.isEmpty()) View.GONE else View.VISIBLE
    }

    /**
     * Gelen antrenman listesiyle istatistik kartlarını doldurur.
     */
    private fun updateStatsUI(historyList: List<WorkoutSession>) {
        val totalWorkouts = historyList.size
        val totalReps = historyList.sumOf { it.repCount }
        val totalDurationSeconds = historyList.sumOf { it.durationInSeconds }

        binding.tvTotalWorkouts.text = totalWorkouts.toString()
        binding.tvTotalReps.text = totalReps.toString()
        binding.tvTotalTime.text = formatTotalTime(totalDurationSeconds)
    }






    /**
     * Butonların ve tıklanabilir kartların olaylarını ayarlar.
     */
    private fun setupButtonClickListeners() {
        binding.btnEditProfile.setOnClickListener {
            findNavController().navigate(R.id.action_userFragment_to_editProfileFragment)
        }
        binding.btnSettings.setOnClickListener {
            findNavController().navigate(R.id.action_userFragment_to_settingsFragment)
        }
        binding.btnLogout.setOnClickListener { confirmLogout() }
    }

    private fun confirmLogout() {
        com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.logout)
            .setMessage(R.string.logout_confirmation)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.logout) { _, _ ->
                requireActivity()
                    .getSharedPreferences("arfitness_prefs", Context.MODE_PRIVATE)
                    .edit()
                    .putBoolean("onboarding_completed", false)
                    .apply()

                val navOptions = NavOptions.Builder()
                    .setPopUpTo(R.id.userFragment, true)
                    .setLaunchSingleTop(true)
                    .build()
                findNavController().navigate(R.id.onboardingFragment, null, navOptions)
            }
            .show()
    }
    // --- YENİ FONKSİYONLAR ---
    /**
     * Gelen veriyle BarChart'ı kurar ve çizer.
     */
    private fun setupBarChart(data: Map<DayOfWeek, Int>) {
        val barChart = binding.barChart

        // 1. Veri Girişlerini (Entries) Oluşturma
        // PAZARTESİ'den PAZAR'a doğru sıralı bir liste oluşturuyoruz.
        val entries = mutableListOf<BarEntry>()
        val daysOfWeek = DayOfWeek.values() // Pazartesi'den başlar

        daysOfWeek.forEachIndexed { index, dayOfWeek ->
            // Haritadan o günün kalori değerini alıyoruz (eğer yoksa 0).
            val calorieValue = data[dayOfWeek]?.toFloat() ?: 0f
            entries.add(BarEntry(index.toFloat(), calorieValue))
        }

        // 2. Veri Setini (DataSet) Oluşturma
        val dataSet = BarDataSet(entries, "Yakılan Kalori") // Grafik içindeki etiket
        dataSet.color = ContextCompat.getColor(requireContext(), R.color.primary) // Barların rengi
        dataSet.valueTextColor = ContextCompat.getColor(requireContext(), R.color.text_main)
        dataSet.valueTextSize = 12f

        // 3. Grafik Verisini (BarData) Oluşturma
        val barData = BarData(dataSet)
        barChart.data = barData

        // --- GRAFİK STİL AYARLARI ---
        barChart.description.isEnabled = false // "Description Label" yazısını kaldır
        barChart.legend.isEnabled = false // "Yakılan Kalori" etiketini kaldır

        // X Ekseni (Alttaki Günler) Ayarları
        val xAxis = barChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(daysOfWeek.map {
            // "MONDAY" -> "Pzt"
            it.getDisplayName(TextStyle.SHORT, Locale("tr"))
        })
        xAxis.position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.textColor = ContextCompat.getColor(requireContext(), R.color.text_secondary)

        // Sol Y Ekseni (Sayılar) Ayarları
        barChart.axisLeft.textColor = ContextCompat.getColor(requireContext(), R.color.text_secondary)
        barChart.axisLeft.axisMinimum = 0f // Minimum değer 0'dan başlasın

        // Sağ Y Ekseni (Sayılar) Ayarları
        barChart.axisRight.isEnabled = false // Sağdaki ekseni gizle

        // Animasyon ve Yenileme
        barChart.animateY(1000) // 1 saniyelik animasyon
        barChart.invalidate() // Grafiği yeniden çiz
    }



    /**
     * Toplam saniyeyi "Xh Ym" formatına çeviren yardımcı fonksiyon.
     */
    private fun formatTotalTime(totalSeconds: Long): String {
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        return "${hours}h ${minutes}m"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
