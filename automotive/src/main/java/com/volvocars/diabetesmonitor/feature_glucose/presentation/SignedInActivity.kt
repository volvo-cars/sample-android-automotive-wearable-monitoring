package com.volvocars.diabetesmonitor.feature_glucose.presentation

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.android.car.ui.core.CarUi
import com.android.car.ui.toolbar.MenuItem
import com.android.car.ui.toolbar.ToolbarController
import com.volvocars.diabetesmonitor.R
import com.volvocars.diabetesmonitor.core.util.showFragment
import com.volvocars.diabetesmonitor.databinding.ActivitySignedInBinding
import com.volvocars.diabetesmonitor.feature_glucose.presentation.diabetes_monitor.DiabetesMonitorFragment
import com.volvocars.diabetesmonitor.feature_glucose.presentation.settings.DiabetesSettingsFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignedInActivity : FragmentActivity() {

    private lateinit var binding: ActivitySignedInBinding
    private lateinit var toolbarController: ToolbarController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignedInBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initToolbar()

        supportFragmentManager.showFragment(DiabetesMonitorFragment(), binding.frameLayout)
    }

    private fun initToolbar() {
        toolbarController = CarUi.requireToolbar(this)
        toolbarController.setLogo(R.mipmap.diabetes_monitor_ic_launcher_round)
        toolbarController.setTitle(R.string.app_name)
        toolbarController.setMenuItems(initMenuItems())
    }

    private fun initMenuItems() =
        listOf(MenuItem.builder(this).setToSettings().setOnClickListener {
            supportFragmentManager.beginTransaction()
                .replace(binding.frameLayout.id, DiabetesSettingsFragment()).commitNow()
        }.build())

}