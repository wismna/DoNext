package com.wismna.geoffroy.donext.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.wismna.geoffroy.donext.R

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        if (savedInstanceState == null) {
            supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.settings, SettingsFragment())
                    .commit()
        }
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
            val themePreference = preferenceScreen.findPreference<Preference>("pref_conf_theme")
            if (themePreference != null) {
                themePreference.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference: Preference?, newValue: Any ->
                    val preferenceValue = (newValue as String).toInt()
                    AppCompatDelegate.setDefaultNightMode(preferenceValue)
                    true
                }
            }
        }
    }
}