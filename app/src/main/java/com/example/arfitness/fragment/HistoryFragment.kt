package com.example.arfitness.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.arfitness.R
import com.example.arfitness.adapter.HistoryAdapter
import com.example.arfitness.data.WorkoutSession
import com.example.arfitness.databinding.FragmentHistoryBinding
import com.example.arfitness.viewmodel.HistoryViewModel
import com.prolificinteractive.materialcalendarview.CalendarDay
import dagger.hilt.android.AndroidEntryPoint
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@AndroidEntryPoint
class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HistoryViewModel by viewModels()
    private lateinit var historyAdapter: HistoryAdapter

    private var allWorkouts: List<WorkoutSession> = emptyList()

    // Başlangıçta bugünün tarihi seçili olsun
    private var selectedDate: CalendarDay? = CalendarDay.today()

    // Başlangıçta "Tümü" çipi seçili olsun
    private var selectedChipId: Int = R.id.chipAll

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupMaterialCalendar()
        observeViewModel()
        setupFilterChips()
    }

    private fun setupRecyclerView() {
        historyAdapter = HistoryAdapter()
        binding.rvWorkoutHistory.apply {
            adapter = historyAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }
    private fun setupMaterialCalendar() {
        val calendarView = binding.calendarView

        // Başlangıçta takvimin bugünü göstermesini ve seçili olmasını sağla
        calendarView.setCurrentDate(CalendarDay.today(), true)
        calendarView.setDateSelected(CalendarDay.today(), true)

        // Kullanıcı bir tarih seçtiğinde
        calendarView.setOnDateChangedListener { _, date, selected ->
            selectedDate = if (selected) date else null
            filterAndUpdateList() // Listeyi yeniden filtrele
        }
    }


    private fun observeViewModel() {
        viewModel.workoutHistory.observe(viewLifecycleOwner) { historyList ->
            allWorkouts = historyList
            updateUI(historyList)
        }
    }

    private fun filterAndUpdateList() {
        var filteredList = allWorkouts

        // 1. Adım: Tarihe göre filtrele
        selectedDate?.let { day ->
            filteredList = filteredList.filter { workout ->
                val workoutDate = Instant.ofEpochMilli(workout.date)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()

                val selectedLocalDate = LocalDate.of(day.year, day.month, day.day)
                workoutDate == selectedLocalDate
            }
        }

        // 2. Adım: Çipe göre filtrele
        filteredList = when (selectedChipId) {
            R.id.chipSquat -> filteredList.filter { it.exerciseName.equals("squat", ignoreCase = true) }
            R.id.chipPushup -> filteredList.filter { it.exerciseName.equals("push-ups", ignoreCase = true) }
            R.id.chipBiceps -> filteredList.filter { it.exerciseName.equals("bicep curl", ignoreCase = true) }
            // ... diğer tüm egzersizlerin için filter satırları ...
            else -> filteredList // R.id.chipAll veya tanımsız bir ID ise, filtreleme yapma
        }

        // Son olarak, filtrelenmiş listeyi UI'a gönder
        updateUI(filteredList)
    }


    private fun updateUI(listToShow: List<WorkoutSession>) {
        if (listToShow.isEmpty()) {
            binding.layoutEmptyState.visibility = View.VISIBLE
            binding.rvWorkoutHistory.visibility = View.GONE
        } else {
            binding.layoutEmptyState.visibility = View.GONE
            binding.rvWorkoutHistory.visibility = View.VISIBLE
            historyAdapter.submitList(listToShow)
        }

        binding.tvTotalWorkouts.text = getString(R.string.workouts_completed, listToShow.size)
    }

    private fun setupFilterChips() {
        binding.chipGroupFilter.setOnCheckedChangeListener { _, checkedId ->
            val filteredList = when (checkedId) {
                R.id.chipSquat -> allWorkouts.filter { it.exerciseName.equals("squat", ignoreCase = true) }
                R.id.chipPushup -> allWorkouts.filter { it.exerciseName.equals("push-ups", ignoreCase = true) }
                R.id.chipBiceps -> allWorkouts.filter { it.exerciseName.equals("bicep curl", ignoreCase = true) }
                R.id.chipshulderpress -> allWorkouts.filter { it.exerciseName.equals("shoulder press", ignoreCase = true) }
                R.id.chipLunge -> allWorkouts.filter { it.exerciseName.equals("lunge", ignoreCase = true) }
                R.id.chipdumbelfly -> allWorkouts.filter { it.exerciseName.equals("dumbell fly", ignoreCase = true) }
                R.id.chipdumbelfly -> allWorkouts.filter { it.exerciseName.equals("dumbell fly", ignoreCase = true) }
                R.id.chipAll -> allWorkouts
                else -> allWorkouts
            }
            updateUI(filteredList)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}