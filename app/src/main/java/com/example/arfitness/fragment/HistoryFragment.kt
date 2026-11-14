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

    // Tarih filtresi kullanıcı takvimden bir gün seçene kadar uygulanmaz.
    private var selectedDate: CalendarDay? = null

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

        // Apply appearances directly as well as in XML so the active theme
        // cannot fall back to MaterialCalendarView's dark default text color.
        calendarView.setDateTextAppearance(R.style.CalendarDateText)
        calendarView.setWeekDayTextAppearance(R.style.CalendarWeekdayText)
        calendarView.setHeaderTextAppearance(R.style.CalendarHeaderText)

        // Bugünü mavi seçili göster; başlangıçta bu görsel seçim geçmişi tarihe göre filtrelemez.
        val today = CalendarDay.today()
        calendarView.setCurrentDate(today, true)
        calendarView.setDateSelected(today, true)

        // Kullanıcı bir tarih seçtiğinde
        calendarView.setOnDateChangedListener { _, date, selected ->
            selectedDate = if (selected) date else null
            filterAndUpdateList()
        }
    }


    private fun observeViewModel() {
        viewModel.workoutHistory.observe(viewLifecycleOwner) { historyList ->
            allWorkouts = historyList
            filterAndUpdateList()
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

        // 2. Adım: Hareket çipine göre filtrele. Birden fazla adlandırma biçimi desteklenir.
        val aliases = exerciseAliases[selectedChipId]
        if (aliases != null) {
            filteredList = filteredList.filter { workout ->
                normalizeExerciseName(workout.exerciseName) in aliases
            }
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
            selectedChipId = checkedId.takeIf { it != View.NO_ID } ?: R.id.chipAll
            filterAndUpdateList()
        }
    }

    private fun normalizeExerciseName(name: String): String = name
        .trim()
        .lowercase()
        .replace('_', ' ')
        .replace('-', ' ')
        .replace(Regex("\\s+"), " ")

    private val exerciseAliases by lazy {
        mapOf(
            R.id.chipSquat to setOf("squat"),
            R.id.chipPushup to setOf("push up", "push ups", "pushup", "pushups"),
            R.id.chipBiceps to setOf("biceps", "bicep curl", "biceps curl"),
            R.id.chipshulderpress to setOf("shoulder press"),
            R.id.chipLunge to setOf("lunge", "lunges"),
            R.id.chipdumbelfly to setOf("dumbbell fly", "dumbell fly", "dumbbell flyes", "dumbell flyes"),
            R.id.chipBenchPress to setOf("bench press"),
            R.id.chipPlank to setOf("plank"),
            R.id.chipPullUp to setOf("pull up", "pull ups", "pullup", "pullups"),
            R.id.chipLatPulldown to setOf("lat pulldown", "lat pull down"),
            R.id.chipHammerCurl to setOf("hammer curl"),
            R.id.chipCablePushdown to setOf("cable pushdown", "cable push down"),
            R.id.chipLateralRaise to setOf("lateral raise"),
            R.id.chipFrontRaise to setOf("front raise"),
            R.id.chipDeadlift to setOf("deadlift", "dead lift")
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
