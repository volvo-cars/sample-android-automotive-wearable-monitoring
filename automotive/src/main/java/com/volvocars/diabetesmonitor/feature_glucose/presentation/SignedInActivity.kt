package com.volvocars.diabetesmonitor.feature_glucose.presentation

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.FragmentActivity
import com.android.car.ui.core.CarUi
import com.android.car.ui.toolbar.MenuItem
import com.android.car.ui.toolbar.Toolbar
import com.android.car.ui.toolbar.ToolbarController
import com.volvocars.diabetesmonitor.R
import com.volvocars.diabetesmonitor.core.util.showFragment
import com.volvocars.diabetesmonitor.core.util.tryRemoveFragment
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

        supportFragmentManager.showFragment(DiabetesMonitorFragment(), binding.frameLayoutS)
    }

    override fun onBackPressed() {
        Log.d(TAG, "onBackPressed: ${toolbarController.state}")
        when (toolbarController.state) {
            Toolbar.State.SUBPAGE -> {
                removeFragmentOrFinish()
            }
            else -> toolbarController.state = Toolbar.State.SUBPAGE
        }
    }

    private fun removeFragmentOrFinish() {
        if (!supportFragmentManager.tryRemoveFragment(binding.frameLayoutS)) {
            finish()
        }
    }


    private fun initToolbar() {
        toolbarController = CarUi.requireToolbar(this)
        toolbarController.setTitle(R.string.app_name)
        toolbarController.state = Toolbar.State.HOME
        toolbarController.setLogo(R.mipmap.diabetes_monitor_ic_launcher_round)
        toolbarController.setMenuItems(initMenuItems())
    }

    private fun initMenuItems() = listOf<MenuItem>(
        MenuItem.builder(this).setToSettings().setOnClickListener {
            supportFragmentManager.showFragment(DiabetesSettingsFragment(), binding.frameLayoutS)
            toolbarController.state = Toolbar.State.SUBPAGE
            toolbarController.setMenuItems(listOf())
        }.build()
    )

    companion object {
        val TAG = SignedInActivity::class.simpleName
    }
}