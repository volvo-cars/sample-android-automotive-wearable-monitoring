package com.volvocars.wearable_monitor.feature_glucose.presentation

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.android.car.ui.core.CarUi
import com.android.car.ui.toolbar.MenuItem
import com.android.car.ui.toolbar.ToolbarController
import com.volvocars.wearable_monitor.R
import com.volvocars.wearable_monitor.databinding.ActivityMainBinding
import com.volvocars.wearable_monitor.feature_glucose.domain.storage.Storage
import com.volvocars.wearable_monitor.feature_glucose.presentation.settings.WearableSettingsActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var toolbarController: ToolbarController

    private lateinit var navController: NavController

    @Inject
    lateinit var storage: Storage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initToolbar()

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        navController = navHostFragment.navController
    }

    override fun onResume() {
        super.onResume()

        initMenuItems()

        if (storage.userSignedIn()) {
            navController.navigate(R.id.diabetesMonitorFragment)
        } else {
            navController.navigate(R.id.loginFragment)
        }
    }

    private fun initMenuItems(): List<MenuItem> {
        return when (storage.userSignedIn()) {
            false -> listOf(MenuItem.builder(this).build())
            true -> listOf(MenuItem.builder(this).setToSettings().setOnClickListener {
                Intent(this, WearableSettingsActivity::class.java).also {
                    startActivity(it)
                }
            }.build())
        }
    }

    private fun initToolbar() {
        toolbarController = CarUi.requireToolbar(this)
        toolbarController.setLogo(R.mipmap.wearable_monitor_ic_launcher_round)
        toolbarController.setTitle(R.string.app_name)
    }

    companion object {
        val TAG = MainActivity::class.simpleName
    }
}