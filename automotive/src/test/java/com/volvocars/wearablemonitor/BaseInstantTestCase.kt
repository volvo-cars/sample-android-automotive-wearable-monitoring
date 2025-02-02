package com.volvocars.wearablemonitor

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import junit.framework.TestCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
abstract class BaseInstantTestCase : TestCase() {

    @get:Rule
    var rule: TestRule = InstantTaskExecutorRule()

    @get:Rule
    val dispatcherRule = ViewModelRule()

    @Before
    public override fun setUp() {
        super.setUp()
        MockKAnnotations.init(this)
    }

    @After
    public override fun tearDown() {
        super.tearDown()
        clearAllMocks()
    }
}