package com.volvocars.diabetesmonitor.feature_glucose.presentation.settings

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.volvocars.diabetesmonitor.launchFragmentInHiltContainer
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@SmallTest
@RunWith(AndroidJUnit4::class)
class DiabetesSettingsFragmentTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun testLaunchFragment() {
        launchFragmentInHiltContainer<DiabetesSettingsFragment> {

        }
    }
}