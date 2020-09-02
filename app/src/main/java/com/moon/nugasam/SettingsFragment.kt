package com.moon.nugasam

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat

class SettingsFragment : PreferenceFragmentCompat(), Preference.OnPreferenceChangeListener {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Hide the divider
        setDivider(ColorDrawable(Color.TRANSPARENT))
        setDividerHeight(0)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_settings)


        findPreference<Preference>(getString(R.string.pref_key_version))?.run {
            title = "버전: " + getAppVersion(context)
        }
    }

    override fun onPreferenceTreeClick(preference: Preference?): Boolean {
        return when (preference?.key) {
            getString(R.string.pref_key_qna) -> {
                true
            }
            getString(R.string.pref_key_version) -> {
                true
            }
            else -> {
                super.onPreferenceTreeClick(preference)
            }
        }
    }

    override fun onPreferenceChange(preference: Preference?, newValue: Any?): Boolean {
        return true
    }

    private fun getAppVersion(context: Context): String? {
        var result = ""
        try {
            result = context.packageManager
                .getPackageInfo(context.packageName, 0).versionName
            result = result.replace("[a-zA-Z]|-".toRegex(), "")
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e("SettingsFragment", e.message)
        }
        return result
    }
}