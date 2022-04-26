package com.volvocars.diabetesmonitor.feature_glucose.presentation.diabetes_monitor

import android.content.Context
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import androidx.work.Configuration
import androidx.work.impl.utils.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import com.volvocars.diabetesmonitor.R
import com.volvocars.diabetesmonitor.launchFragmentInHiltContainer
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.Matchers
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@SmallTest
@RunWith(AndroidJUnit4::class)
class DiabetesMonitorFragmentTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Before
    fun setUp() {
        hiltRule.inject()
        val context = ApplicationProvider.getApplicationContext<Context>()
        val config = Configuration.Builder()
            .setMinimumLoggingLevel(Log.DEBUG)
            .setExecutor(SynchronousExecutor())
            .build()

        // Initialize WorkManager for instrumentation tests.
        WorkManagerTestInitHelper.initializeTestWorkManager(context, config)
    }


    @Test
    fun testLaunchFragmentInHiltContainer() {
        launchFragmentInHiltContainer<DiabetesMonitorFragment> {
            onView(withId(R.id.glucoseChart)).check(
                ViewAssertions.matches(
                    Matchers.allOf(
                        isDisplayed(),
                    )
                )
            )
        }
    }
}