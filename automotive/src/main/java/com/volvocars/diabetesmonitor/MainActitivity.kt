package com.volvocars.diabetesmonitor

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.volvocars.diabetesmonitor.databinding.ActivityMainBinding
import com.volvocars.diabetesmonitor.feature_glucose.presentation.login.LoginFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportFragmentManager.beginTransaction().add(R.id.frame_layout, LoginFragment())
            .commitNow()
    }
}