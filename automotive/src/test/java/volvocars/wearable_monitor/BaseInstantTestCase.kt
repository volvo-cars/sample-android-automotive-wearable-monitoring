package volvocars.wearable_monitor

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.volvocars.drivingjournal.volvocars.wearable_monitor.ViewModelRule
import io.mockk.MockKAnnotations
import junit.framework.TestCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
@OptIn(ExperimentalCoroutinesApi::class)
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
    }
}