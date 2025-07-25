package com.example.arfitness.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.Toast
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

    // ProfileViewModel'i kullanarak veritabanına kayıt yapacağız.
    private val viewModel: ProfileViewModel by viewModels()

    // Seçilen hedefi tutmak için bir değişken
    private var selectedGoal: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOnboardingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupGoalSelection()
        setupStartButton()
    }

    /**
     * Hedef seçimi için RadioButton ve CardView'ların tıklama mantığını ayarlar.
     * Bu, kullanıcı bir karta tıkladığında ilgili radio butonun seçilmesini sağlar.
     */
    private fun setupGoalSelection() {
        // RadioButton'ları bir listeye alarak kolayca yönetiyoruz.
        val radioButtons = listOf(
            binding.rbWeightLoss,
            binding.rbMuscleGain,
            binding.rbGeneralFitness,
            binding.rbStrengthTraining
        )

        // Her bir CardView'a tıklandığında, ilgili RadioButton'u seçen bir listener atıyoruz.
        binding.cardWeightLoss.setOnClickListener { selectRadioButton(radioButtons, binding.rbWeightLoss) }
        binding.cardMuscleGain.setOnClickListener { selectRadioButton(radioButtons, binding.rbMuscleGain) }
        binding.cardGeneralFitness.setOnClickListener { selectRadioButton(radioButtons, binding.rbGeneralFitness) }
        binding.cardStrengthTraining.setOnClickListener { selectRadioButton(radioButtons, binding.rbStrengthTraining) }
    }

    /**
     * Sadece bir RadioButton'un seçili kalmasını sağlayan yardımcı fonksiyon.
     */
    private fun selectRadioButton(allButtons: List<RadioButton>, selectedButton: RadioButton) {
        // Önce listedeki tüm butonların seçimini kaldır.
        allButtons.forEach { it.isChecked = false }
        // Sonra tıklanan butonu seçili hale getir.
        selectedButton.isChecked = true
    }

    /**
     * "Yolculuğa Başla" butonunun tıklama olayını ayarlar.
     * Verileri doğrular, kaydeder ve ana ekrana yönlendirir.
     */
    private fun setupStartButton() {
        binding.btnStartJourney.setOnClickListener {
            val name = binding.etUserName.text.toString().trim()
            val email = binding.etUserEmail.text.toString().trim()

            // O an seçili olan RadioButton'a göre hedef metnini al.
            updateSelectedGoal()

            // Veri Doğrulama (Validation)
            if (!isDataValid(name, email, selectedGoal)) {
                return@setOnClickListener // Eğer veri geçersizse, işlemi durdur.
            }

            // Yeni kullanıcı nesnesi oluştur.
            val newUser = User(
                name = name,
                email = email,
                goal = selectedGoal!!, // Null olamayacağını doğruladığımız için '!!' kullanabiliriz.
                weight = 0.0, // Diğer bilgiler daha sonra profilden güncellenebilir.
                height = 0
            )

            // ViewModel aracılığıyla profili veritabanına kaydet.
            viewModel.saveUserProfile(newUser)

            // Onboarding'in tamamlandığını bir yere kaydet.
            markOnboardingAsCompleted()

            // Ana ekrana git.
            // Bu action'ın nav_graph.xml'de tanımlı olduğundan emin ol.
            val action = OnboardingFragmentDirections.actionOnboardingFragmentToAnasayfaFragment()
            findNavController().navigate(action)
        }
    }

    /**
     * Hangi hedefin seçildiğini RadioButton'lara bakarak günceller.
     */
    private fun updateSelectedGoal() {
        selectedGoal = when {
            binding.rbWeightLoss.isChecked -> getString(R.string.weight_loss)
            binding.rbMuscleGain.isChecked -> getString(R.string.muscle_gain)
            binding.rbGeneralFitness.isChecked -> getString(R.string.general_fitness)
            binding.rbStrengthTraining.isChecked -> getString(R.string.strength_training)
            else -> null
        }
    }

    /**
     * Girilen verilerin geçerli olup olmadığını kontrol eder ve gerekirse uyarı gösterir.
     * @return Veri geçerliyse true, değilse false döner.
     */
    private fun isDataValid(name: String, email: String, goal: String?): Boolean {
        if (name.isBlank()) {
            Toast.makeText(requireContext(), "Lütfen adınızı girin.", Toast.LENGTH_SHORT).show()
            return false
        }
        if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(requireContext(), "Lütfen geçerli bir e-posta adresi girin.", Toast.LENGTH_SHORT).show()
            return false
        }
        if (goal == null) {
            Toast.makeText(requireContext(), "Lütfen bir hedef seçin.", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    /**
     * Onboarding sürecinin tamamlandığını SharedPreferences'a kaydeder.
     * Böylece bu ekran bir daha gösterilmez.
     */
    private fun markOnboardingAsCompleted() {
        val sharedPref = requireActivity().getSharedPreferences("arfitness_prefs", Context.MODE_PRIVATE)
        with (sharedPref.edit()) {
            putBoolean("onboarding_completed", true)
            apply()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}