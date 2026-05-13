package com.crosssafe.app

import android.content.Intent
import android.os.Bundle
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.crosssafe.app.util.ThemeManager

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.sharedPreferencesName = "crosssafe_prefs"
        setPreferencesFromResource(R.xml.preferences, rootKey)
        setupPresets()
        setupAppearance()
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

    private fun setupAppearance() {
        findPreference<ListPreference>("app_theme")?.apply {
            value = ThemeManager.getCurrent(requireContext())
            summary = entry
            setOnPreferenceChangeListener { _, newValue ->
                ThemeManager.save(requireContext(), newValue as String)
                ThemeManager.apply(requireContext())
                requireActivity().recreate()
                true
            }
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
