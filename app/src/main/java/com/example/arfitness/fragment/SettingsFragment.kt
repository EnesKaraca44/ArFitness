package com.example.arfitness.fragment

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.arfitness.R
import com.example.arfitness.databinding.FragmentSettingsBinding
import com.example.arfitness.viewmodel.ProfileViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.btnBack.setOnClickListener { findNavController().popBackStack() }
        binding.btnLanguage.setOnClickListener { showLanguagePicker() }
        val packageName = requireContext().packageName
        val versionName = requireContext().packageManager
            .getPackageInfo(packageName, 0)
            .versionName.orEmpty()
        binding.tvAppVersion.text = getString(R.string.app_version, versionName)
        binding.btnClearHistory.setOnClickListener { confirmClearHistory() }
        binding.btnPrivacyPolicy.setOnClickListener {
            openUrl("https://github.com/EnesKaraca44/ArFitness/blob/master/privacy_policy.html")
        }
        binding.btnContactSupport.setOnClickListener { contactSupport() }
        binding.btnShareApp.setOnClickListener { shareApp() }
        binding.btnRateApp.setOnClickListener { openUrl(playStoreUrl(packageName)) }
    }

    private fun showLanguagePicker() {
        val options = resources.getStringArray(R.array.language_names)
        val systemLocale = resources.configuration.locales[0]?.language.orEmpty()
        val selectedLocale = AppCompatDelegate.getApplicationLocales()
            .get(0)?.language?.takeIf { it.isNotBlank() } ?: systemLocale
        val selectedIndex = if (selectedLocale.startsWith("en")) 1 else 0

        AlertDialog.Builder(requireContext())
            .setTitle(R.string.app_language)
            .setSingleChoiceItems(options, selectedIndex) { dialog, which ->
                val languageTag = if (which == 1) "en" else "tr"
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(languageTag))
                dialog.dismiss()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun confirmClearHistory() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.clear_history_title)
            .setMessage(R.string.clear_history_confirmation)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.clear) { _, _ ->
                viewModel.clearWorkoutHistory()
                Toast.makeText(requireContext(), R.string.history_cleared, Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun openUrl(url: String) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(requireContext(), R.string.no_browser_available, Toast.LENGTH_SHORT).show()
        }
    }

    private fun contactSupport() {
        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:eneskaraca44@gmail.com")).apply {
            putExtra(Intent.EXTRA_SUBJECT, getString(R.string.support_email_subject))
        }
        try {
            startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(requireContext(), R.string.no_email_app_available, Toast.LENGTH_SHORT).show()
        }
    }

    private fun shareApp() {
        val storeUrl = playStoreUrl(requireContext().packageName)
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, getString(R.string.share_app_message, storeUrl))
        }
        startActivity(Intent.createChooser(sendIntent, getString(R.string.share_app)))
    }

    private fun playStoreUrl(packageName: String): String =
        "https://play.google.com/store/apps/details?id=$packageName"

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
