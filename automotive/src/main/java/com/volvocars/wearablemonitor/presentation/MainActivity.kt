package com.volvocars.wearablemonitor.presentation

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.volvocars.wearablemonitor.R
import com.volvocars.wearablemonitor.databinding.ActivityMainBinding
import com.volvocars.wearablemonitor.domain.storage.Storage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    @Inject
    lateinit var storage: Storage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        navController = navHostFragment.navController
    }

    override fun onResume() {
        super.onResume()

        if (storage.userSignedIn()) {
            navController.navigate(R.id.diabetesMonitorFragment)
        } else {
            navController.navigate(R.id.loginFragment)
        }
    }

    companion object {
        val TAG = MainActivity::class.simpleName
    }
}