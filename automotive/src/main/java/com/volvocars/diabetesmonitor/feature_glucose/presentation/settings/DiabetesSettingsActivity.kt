package com.volvocars.diabetesmonitor.feature_glucose.presentation.settings

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.volvocars.diabetesmonitor.databinding.ActivitySettingsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DiabetesSettingsActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(binding.settingsFragmentContainer.id, DiabetesSettingsFragment())
                .commitNow()
        }
    }

    companion object {
        val TAG = DiabetesSettingsActivity::class.simpleName
    }
}