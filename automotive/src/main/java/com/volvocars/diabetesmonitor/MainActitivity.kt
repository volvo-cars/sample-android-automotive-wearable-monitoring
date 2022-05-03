package com.volvocars.diabetesmonitor

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.android.car.ui.core.CarUi
import com.android.car.ui.toolbar.MenuItem
import com.android.car.ui.toolbar.ToolbarController
import com.volvocars.diabetesmonitor.databinding.ActivityMainBinding
import com.volvocars.diabetesmonitor.feature_glucose.presentation.login.LoginFragment
import com.volvocars.diabetesmonitor.feature_glucose.presentation.settings.DiabetesSettingsActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    private lateinit var toolbarController: ToolbarController

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        toolbarController = CarUi.requireToolbar(this)
        toolbarController.setLogo(R.mipmap.diabetes_monitor_ic_launcher_round)
        toolbarController.setTitle(R.string.app_name)
        toolbarController.setMenuItems(
            listOf(
                MenuItem.builder(this).setToSettings().setOnClickListener {
                    startActivity(Intent(this, DiabetesSettingsActivity::class.java))
                }.build()
            )
        )
        setContentView(binding.root)

        supportFragmentManager.beginTransaction().add(R.id.frame_layout, LoginFragment())
            .commitNow()
    }
}