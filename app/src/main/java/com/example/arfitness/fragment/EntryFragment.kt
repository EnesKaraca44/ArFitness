package com.example.arfitness.fragment



import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.arfitness.R

// Bu fragment için bir XML dosyasına gerek yok
class EntryFragment : Fragment(R.layout.fragment_entry_empty) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPref = requireActivity().getSharedPreferences("arfitness_prefs", Context.MODE_PRIVATE)
        val isOnboardingCompleted = sharedPref.getBoolean("onboarding_completed", false)

        if (isOnboardingCompleted) {
            // Onboarding tamamlanmış, direkt ana ekrana git
            val action = EntryFragmentDirections.actionEntryFragmentToAnasayfaFragment()
            findNavController().navigate(action)
        } else {
            // Onboarding tamamlanmamış, onboarding ekranına git
            val action = EntryFragmentDirections.actionEntryFragmentToOnboardingFragment()
            findNavController().navigate(action)
        }
    }
}
