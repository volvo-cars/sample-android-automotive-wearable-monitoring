package com.volvocars.wearable_monitor.feature_glucose.presentation.settings

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.android.car.ui.core.CarUi
import com.android.car.ui.toolbar.ToolbarController
import com.volvocars.wearable_monitor.databinding.ActivitySettingsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WearableSettingsActivity : FragmentActivity() {

    lateinit var toolbarController: ToolbarController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        toolbarController = CarUi.requireToolbar(this)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(binding.settingsFragmentContainer.id, WearableSettingsFragment())
                .commitNow()
        }
    }

    companion object {
        val TAG = WearableSettingsActivity::class.simpleName
    }
}