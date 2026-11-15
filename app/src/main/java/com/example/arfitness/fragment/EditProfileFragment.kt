package com.example.arfitness.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.arfitness.R
import com.example.arfitness.data.User
import com.example.arfitness.databinding.FragmentEditProfileBinding
import com.example.arfitness.viewmodel.ProfileViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EditProfileFragment : Fragment() {
    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProfileViewModel by viewModels()

    private var selectedGoal: String? = null
    private var selectedGender: String = "Erkek"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnBack.setOnClickListener { findNavController().popBackStack() }

        setupGenderSelection()
        setupGoalSelection()

        // Mevcut kullanıcı verilerini forma doldur
        viewModel.userProfile.observe(viewLifecycleOwner) { user ->
            user?.let { profile ->
                populateProfileData(profile)
                binding.btnSaveProfile.setOnClickListener {
                    saveProfile(profile)
                }
            }
        }
    }

    private fun populateProfileData(profile: User) {
        binding.etName.setText(profile.name)
        if (profile.age > 0) binding.etAge.setText(profile.age.toString())
        if (profile.weight > 0) binding.etWeight.setText(profile.weight.toString())
        if (profile.height > 0) binding.etHeight.setText(profile.height.toString())

        // Cinsiyet
        val gender = if (profile.gender.isNotBlank()) profile.gender else "Erkek"
        updateGenderChips(gender)

        // Hedef
        val goal = profile.goal.ifBlank { getString(R.string.weight_loss) }
        preselectGoal(goal)
    }

    private fun setupGenderSelection() {
        binding.chipMale.setOnClickListener { updateGenderChips("Erkek") }
        binding.chipFemale.setOnClickListener { updateGenderChips("Kadın") }
        binding.chipOther.setOnClickListener { updateGenderChips("Diğer") }
    }

    private fun updateGenderChips(selected: String) {
        selectedGender = selected
        val neonColor = ContextCompat.getColor(requireContext(), R.color.neon_lime)
        val secondaryColor = ContextCompat.getColor(requireContext(), R.color.text_secondary)

        binding.chipMale.setBackgroundResource(if (selected == "Erkek") R.drawable.bg_chip_selected else R.drawable.bg_chip_unselected)
        binding.chipMale.setTextColor(if (selected == "Erkek") neonColor else secondaryColor)
        binding.chipMale.text = if (selected == "Erkek") "Erkek ✓" else getString(R.string.gender_male)

        binding.chipFemale.setBackgroundResource(if (selected == "Kadın") R.drawable.bg_chip_selected else R.drawable.bg_chip_unselected)
        binding.chipFemale.setTextColor(if (selected == "Kadın") neonColor else secondaryColor)
        binding.chipFemale.text = if (selected == "Kadın") "Kadın ✓" else getString(R.string.gender_female)

        binding.chipOther.setBackgroundResource(if (selected == "Diğer") R.drawable.bg_chip_selected else R.drawable.bg_chip_unselected)
        binding.chipOther.setTextColor(if (selected == "Diğer") neonColor else secondaryColor)
        binding.chipOther.text = if (selected == "Diğer") "Diğer ✓" else getString(R.string.gender_other)
    }

    private fun setupGoalSelection() {
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

    private fun selectGoalCard(targetCard: View, goalText: String) {
        selectedGoal = goalText
        val cards = listOf(
            binding.cardWeightLoss to binding.rbWeightLoss,
            binding.cardMuscleGain to binding.rbMuscleGain,
            binding.cardStrengthTraining to binding.rbStrengthTraining,
            binding.cardGeneralFitness to binding.rbGeneralFitness
        )
        cards.forEach { (card, rb) ->
            val isSelected = (card == targetCard)
            rb.isChecked = isSelected
            card.setBackgroundResource(
                if (isSelected) R.drawable.bg_goal_card_selected else R.drawable.bg_goal_card_unselected
            )
        }
    }

    private fun preselectGoal(goalName: String) {
        val targetCard = when {
            goalName.contains("Kilo", ignoreCase = true) || goalName.contains("Weight", ignoreCase = true) -> binding.cardWeightLoss
            goalName.contains("Kas", ignoreCase = true) || goalName.contains("Muscle", ignoreCase = true) -> binding.cardMuscleGain
            goalName.contains("Güç", ignoreCase = true) || goalName.contains("Strength", ignoreCase = true) -> binding.cardStrengthTraining
            else -> binding.cardGeneralFitness
        }
        val targetText = when (targetCard) {
            binding.cardWeightLoss -> getString(R.string.weight_loss)
            binding.cardMuscleGain -> getString(R.string.muscle_gain)
            binding.cardStrengthTraining -> getString(R.string.strength_training)
            else -> getString(R.string.general_fitness)
        }
        selectGoalCard(targetCard, targetText)
    }

    private fun saveProfile(currentUser: User) {
        val name = binding.etName.text?.toString()?.trim().orEmpty()
        val ageText = binding.etAge.text?.toString()?.trim().orEmpty()
        val weight = binding.etWeight.text?.toString()?.trim()?.replace(',', '.')?.toDoubleOrNull()
        val height = binding.etHeight.text?.toString()?.trim()?.toIntOrNull()
        val age = ageText.toIntOrNull()

        if (name.isBlank()) {
            Toast.makeText(requireContext(), R.string.enter_name_message, Toast.LENGTH_SHORT).show()
            binding.etName.requestFocus()
            return
        }

        if (ageText.isNotBlank() && (age == null || age !in 1..120)) {
            Toast.makeText(requireContext(), R.string.invalid_age, Toast.LENGTH_SHORT).show()
            binding.etAge.requestFocus()
            return
        }

        if (binding.etWeight.text?.isNotBlank() == true && (weight == null || weight !in 1.0..500.0)) {
            Toast.makeText(requireContext(), R.string.invalid_weight_value, Toast.LENGTH_SHORT).show()
            binding.etWeight.requestFocus()
            return
        }

        if (binding.etHeight.text?.isNotBlank() == true && (height == null || height !in 50..300)) {
            Toast.makeText(requireContext(), R.string.invalid_height_value, Toast.LENGTH_SHORT).show()
            binding.etHeight.requestFocus()
            return
        }

        viewModel.saveUserProfile(currentUser.copy(
            name = name,
            age = age ?: currentUser.age,
            weight = weight ?: currentUser.weight,
            height = height ?: currentUser.height,
            goal = selectedGoal ?: currentUser.goal,
            gender = selectedGender
        ))

        Toast.makeText(requireContext(), R.string.profile_updated, Toast.LENGTH_SHORT).show()
        findNavController().popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
