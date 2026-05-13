package com.crosssafe.app

import android.content.Intent
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.sharedPreferencesName = "crosssafe_prefs"
        setPreferencesFromResource(R.xml.preferences, rootKey)
        setupPresets()
        setupHelp()
    }

    private fun setupPresets() {
        findPreference<Preference>("manage_pinned")?.setOnPreferenceClickListener {
            startActivity(Intent(requireContext(), ManagePinnedPresetsActivity::class.java))
            true
        }
        findPreference<Preference>("create_custom_preset")?.setOnPreferenceClickListener {
            true
        }
    }

    private fun setupHelp() {
        findPreference<Preference>("replay_onboarding")?.setOnPreferenceClickListener {
            startActivity(Intent(requireContext(), OnboardingActivity::class.java))
            true
        }
        findPreference<Preference>("epilepsy_warning")?.setOnPreferenceClickListener {
            startActivity(Intent(requireContext(), OnboardingActivity::class.java))
            true
        }
        findPreference<Preference>("app_version")?.summary = BuildConfig.VERSION_NAME
    }
}
