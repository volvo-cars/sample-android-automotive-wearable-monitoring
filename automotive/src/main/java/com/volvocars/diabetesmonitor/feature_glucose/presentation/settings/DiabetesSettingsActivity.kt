package com.volvocars.diabetesmonitor.feature_glucose.presentation.settings

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.android.car.ui.core.CarUi
import com.android.car.ui.toolbar.ToolbarController
import com.volvocars.diabetesmonitor.databinding.ActivitySettingsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DiabetesSettingsActivity : FragmentActivity() {

    lateinit var toolbarController: ToolbarController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        toolbarController = CarUi.requireToolbar(this)

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