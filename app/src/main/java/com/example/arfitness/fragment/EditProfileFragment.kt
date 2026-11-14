package com.example.arfitness.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnBack.setOnClickListener { findNavController().popBackStack() }

        // Mevcut kullanıcı verilerini forma doldur
        viewModel.userProfile.observe(viewLifecycleOwner) { user ->
            user?.let { profile ->
                binding.etName.setText(profile.name)
                if (profile.age > 0) binding.etAge.setText(profile.age.toString())
                if (profile.weight > 0) binding.etWeight.setText(profile.weight.toString())
                if (profile.height > 0) binding.etHeight.setText(profile.height.toString())
                binding.etGoal.setText(profile.goal)
                binding.btnSaveProfile.setOnClickListener {
                    saveProfile(profile)
                }
            }
        }
    }

    private fun saveProfile(currentUser: User) {
        val name = binding.etName.text?.toString()?.trim().orEmpty()
        val ageText = binding.etAge.text?.toString()?.trim().orEmpty()
        val weight = binding.etWeight.text?.toString()?.trim()?.replace(',', '.')?.toDoubleOrNull()
        val height = binding.etHeight.text?.toString()?.trim()?.toIntOrNull()
        val goal = binding.etGoal.text?.toString()?.trim().orEmpty()
        val age = ageText.toIntOrNull()

        binding.nameInputLayout.error = if (name.isBlank()) getString(R.string.required_field) else null
        binding.ageInputLayout.error = if (ageText.isNotBlank() && (age == null || age !in 1..120)) getString(R.string.invalid_age) else null
        binding.weightInputLayout.error = if (binding.etWeight.text?.isNotBlank() == true && (weight == null || weight !in 1.0..500.0)) getString(R.string.invalid_weight_value) else null
        binding.heightInputLayout.error = if (binding.etHeight.text?.isNotBlank() == true && (height == null || height !in 50..300)) getString(R.string.invalid_height_value) else null
        if (name.isBlank() || binding.ageInputLayout.error != null || binding.weightInputLayout.error != null || binding.heightInputLayout.error != null) return

        viewModel.saveUserProfile(currentUser.copy(
            name = name,
            age = age ?: 0,
            weight = weight ?: 0.0,
            height = height ?: 0,
            goal = goal
        ))
        findNavController().popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
