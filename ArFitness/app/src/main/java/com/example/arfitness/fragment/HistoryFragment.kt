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
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HistoryViewModel by viewModels()
    private lateinit var historyAdapter: HistoryAdapter

    private var allWorkouts: List<WorkoutSession> = emptyList()

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

    private fun observeViewModel() {
        viewModel.workoutHistory.observe(viewLifecycleOwner) { historyList ->
            allWorkouts = historyList
            updateStats(historyList)
            filterList(binding.chipGroupFilter.checkedChipId)
        }
    }

    private fun updateStats(list: List<WorkoutSession>) {
        if (list.isEmpty()) {
            binding.tvTotalTime.text = "0 dk"
            binding.tvBestScore.text = "0%"
            return
        }
        val totalSeconds = list.sumOf { it.durationInSeconds }
        val minutes = totalSeconds / 60
        val hours = minutes / 60
        val remainingMinutes = minutes % 60

        binding.tvTotalTime.text = if (hours > 0) {
            "${hours}s ${remainingMinutes}dk"
        } else {
            "${remainingMinutes} dk"
        }

        val bestScore = list.maxOfOrNull { it.formScore } ?: 0
        binding.tvBestScore.text = "$bestScore%"
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

        binding.tvTotalWorkouts.text = "${listToShow.size} antrenman listelendi"
    }

    private fun setupFilterChips() {
        binding.chipGroupFilter.setOnCheckedChangeListener { _, checkedId ->
            filterList(checkedId)
        }
    }

    private fun filterList(checkedId: Int) {
        val filteredList = when (checkedId) {
            R.id.chipSquat -> allWorkouts.filter { it.exerciseName.contains("squat", ignoreCase = true) }
            R.id.chipPushup -> allWorkouts.filter { it.exerciseName.contains("push", ignoreCase = true) }
            R.id.chipBiceps -> allWorkouts.filter { it.exerciseName.contains("bicep", ignoreCase = true) }
            R.id.chipshulderpress -> allWorkouts.filter { it.exerciseName.contains("shoulder", ignoreCase = true) }
            R.id.chipLunge -> allWorkouts.filter { it.exerciseName.contains("lunge", ignoreCase = true) }
            R.id.chipdumbelfly -> allWorkouts.filter {
                it.exerciseName.contains("fly", ignoreCase = true) || it.exerciseName.contains("dumb", ignoreCase = true)
            }
            else -> allWorkouts
        }
        updateUI(filteredList)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}