package com.example.arfitness.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.arfitness.R
import com.example.arfitness.data.User
import com.example.arfitness.databinding.FragmentOnboardingBinding
import com.example.arfitness.viewmodel.ProfileViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OnboardingFragment : Fragment() {

    private var _binding: FragmentOnboardingBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels()

    private var selectedGoal: String? = null
    private var selectedGender: String = "Erkek"
    private var selectedExperience: String = "Orta Seviye"
    private var selectedWeeklyDays: String = "4 Gün"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOnboardingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupTopBar()
        setupGenderSelection()
        setupGoalSelection()
        setupHabitsSelection()
        setupStartButton()
    }

    private fun setupTopBar() {
        binding.btnBack.setOnClickListener {
            if (!findNavController().popBackStack()) {
                markOnboardingAsCompleted()
                findNavController().navigate(OnboardingFragmentDirections.actionOnboardingFragmentToAnasayfaFragment())
            }
        }

        binding.btnSkip.setOnClickListener {
            saveDefaultAndContinue()
        }
    }

    private fun setupGenderSelection() {
        val maleChip = binding.chipMale
        val femaleChip = binding.chipFemale
        val otherChip = binding.chipOther

        fun updateGenderChips(selected: String) {
            selectedGender = selected

            val neonColor = ContextCompat.getColor(requireContext(), R.color.neon_lime)
            val secondaryColor = ContextCompat.getColor(requireContext(), R.color.text_secondary)

            maleChip.setBackgroundResource(if (selected == "Erkek") R.drawable.bg_chip_selected else R.drawable.bg_chip_unselected)
            maleChip.setTextColor(if (selected == "Erkek") neonColor else secondaryColor)
            maleChip.text = if (selected == "Erkek") "Erkek ✓" else getString(R.string.gender_male)

            femaleChip.setBackgroundResource(if (selected == "Kadın") R.drawable.bg_chip_selected else R.drawable.bg_chip_unselected)
            femaleChip.setTextColor(if (selected == "Kadın") neonColor else secondaryColor)
            femaleChip.text = if (selected == "Kadın") "Kadın ✓" else getString(R.string.gender_female)

            otherChip.setBackgroundResource(if (selected == "Diğer") R.drawable.bg_chip_selected else R.drawable.bg_chip_unselected)
            otherChip.setTextColor(if (selected == "Diğer") neonColor else secondaryColor)
            otherChip.text = if (selected == "Diğer") "Diğer ✓" else getString(R.string.gender_other)
        }

        maleChip.setOnClickListener { updateGenderChips("Erkek") }
        femaleChip.setOnClickListener { updateGenderChips("Kadın") }
        otherChip.setOnClickListener { updateGenderChips("Diğer") }
    }

    private fun setupGoalSelection() {
        // Varsayılan hedef
        selectedGoal = getString(R.string.weight_loss)

        val cards = listOf(
            binding.cardWeightLoss to binding.rbWeightLoss,
            binding.cardMuscleGain to binding.rbMuscleGain,
            binding.cardStrengthTraining to binding.rbStrengthTraining,
            binding.cardGeneralFitness to binding.rbGeneralFitness
        )

        fun selectGoalCard(targetCard: View, goalText: String) {
            selectedGoal = goalText
            cards.forEach { (card, rb) ->
                val isSelected = (card == targetCard)
                rb.isChecked = isSelected
                card.setBackgroundResource(
                    if (isSelected) R.drawable.bg_goal_card_selected else R.drawable.bg_goal_card_unselected
                )
            }
        }

        binding.cardWeightLoss.setOnClickListener {
            selectGoalCard(binding.cardWeightLoss, getString(R.string.weight_loss))
        }
        binding.cardMuscleGain.setOnClickListener {
            selectGoalCard(binding.cardMuscleGain, getString(R.string.muscle_gain))
        }
        binding.cardStrengthTraining.setOnClickListener {
            selectGoalCard(binding.cardStrengthTraining, getString(R.string.strength_training))
        }
        binding.cardGeneralFitness.setOnClickListener {
            selectGoalCard(binding.cardGeneralFitness, getString(R.string.general_fitness))
        }
    }

    private fun setupHabitsSelection() {
        val neonColor = ContextCompat.getColor(requireContext(), R.color.neon_lime)
        val secondaryColor = ContextCompat.getColor(requireContext(), R.color.text_secondary)

        // Deneyim Seviyesi
        val expChips = listOf(
            binding.chipExpBeginner to getString(R.string.exp_beginner),
            binding.chipExpIntermediate to getString(R.string.exp_intermediate),
            binding.chipExpAdvanced to getString(R.string.exp_advanced)
        )

        expChips.forEach { (chip, name) ->
            chip.setOnClickListener {
                selectedExperience = name
                expChips.forEach { (c, cName) ->
                    val isSel = (cName == name)
                    c.setBackgroundResource(if (isSel) R.drawable.bg_chip_selected else R.drawable.bg_chip_unselected)
                    c.setTextColor(if (isSel) neonColor else secondaryColor)
                }
            }
        }

        // Haftalık Gün Hedefi
        val dayChips = listOf(
            binding.chipDays2 to "2 Gün",
            binding.chipDays3 to "3 Gün",
            binding.chipDays4 to "4 Gün",
            binding.chipDays5 to "5+ Gün"
        )

        dayChips.forEach { (chip, days) ->
            chip.setOnClickListener {
                selectedWeeklyDays = days
                binding.tvSelectedDaysLabel.text = "$days / Hafta"
                dayChips.forEach { (c, cDays) ->
                    val isSel = (cDays == days)
                    c.setBackgroundResource(if (isSel) R.drawable.bg_chip_selected else R.drawable.bg_chip_unselected)
                    c.setTextColor(if (isSel) neonColor else secondaryColor)
                }
            }
        }
    }

    private fun setupStartButton() {
        binding.btnStartJourney.setOnClickListener {
            val name = binding.etUserName.text?.toString()?.trim().orEmpty()
            val email = binding.etUserEmail.text?.toString()?.trim().orEmpty()
            val ageText = binding.etAge.text?.toString()?.trim().orEmpty()
            val weightText = binding.etWeight.text?.toString()?.trim().orEmpty()
            val heightText = binding.etHeight.text?.toString()?.trim().orEmpty()

            val age = ageText.toIntOrNull()
            val weight = weightText.replace(',', '.').toDoubleOrNull()
            val height = heightText.toIntOrNull()

            if (name.isBlank()) {
                Toast.makeText(requireContext(), R.string.enter_name_message, Toast.LENGTH_SHORT).show()
                binding.etUserName.requestFocus()
                return@setOnClickListener
            }

            if (email.isNotBlank() && !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(requireContext(), R.string.invalid_email_message, Toast.LENGTH_SHORT).show()
                binding.etUserEmail.requestFocus()
                return@setOnClickListener
            }

            val newUser = User(
                name = name,
                email = email,
                goal = selectedGoal ?: getString(R.string.general_fitness),
                weight = weight ?: 75.0,
                height = height ?: 175,
                age = age ?: 25,
                gender = selectedGender
            )

            viewModel.saveUserProfile(newUser)
            markOnboardingAsCompleted()

            val action = OnboardingFragmentDirections.actionOnboardingFragmentToAnasayfaFragment()
            findNavController().navigate(action)
        }
    }

    private fun saveDefaultAndContinue() {
        val defaultUser = User(
            name = "Sporcu",
            email = "",
            goal = selectedGoal ?: getString(R.string.general_fitness),
            weight = 75.0,
            height = 175,
            age = 25,
            gender = selectedGender
        )
        viewModel.saveUserProfile(defaultUser)
        markOnboardingAsCompleted()
        val action = OnboardingFragmentDirections.actionOnboardingFragmentToAnasayfaFragment()
        findNavController().navigate(action)
    }

    private fun markOnboardingAsCompleted() {
        val sharedPref = requireActivity().getSharedPreferences("arfitness_prefs", Context.MODE_PRIVATE)
        sharedPref.edit().putBoolean("onboarding_completed", true).apply()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
