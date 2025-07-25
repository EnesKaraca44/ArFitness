package com.example.arfitness.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.arfitness.data.User
import com.example.arfitness.databinding.FragmentEditProfileBinding
import com.example.arfitness.viewmodel.ProfileViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EditProfileFragment : Fragment() {
private lateinit var binding : FragmentEditProfileBinding
    private val viewModel: ProfileViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Mevcut kullanıcı verilerini forma doldur
        viewModel.userProfile.observe(viewLifecycleOwner) { user ->
            user?.let {
                binding.etName.setText(it.name)
                binding.etWeight.setText(it.weight.toString())
                binding.etHeight.setText(it.height.toString())
                binding.etGoal.setText(it.goal)
            }
        }

        // Kaydet butonuna basıldığında
        binding.btnSaveProfile.setOnClickListener {
            // Formdan yeni verileri al
            val name = binding.etName.text.toString()
            val weight = binding.etWeight.text.toString().toDoubleOrNull() ?: 0.0
            val height = binding.etHeight.text.toString().toIntOrNull() ?: 0
            val goal = binding.etGoal.text.toString()

            // Yeni User nesnesini oluştur
            val updatedUser = User(
                name = name,
                weight = weight,
                height = height,
                goal = goal
            )
            binding.btnSaveProfile.setOnClickListener {
                // ...
                viewModel.saveUserProfile(updatedUser)

                // --- DOĞRU KULLANIM ---
                // popBackStack() fonksiyonu, NavController nesnesi üzerinden çağrılır.
                findNavController().popBackStack()
            }
        }
    }
}