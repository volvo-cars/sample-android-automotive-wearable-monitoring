package com.volvocars.wearable_monitor.feature_glucose.presentation.util

import org.junit.Test
import volvocars.wearable_monitor.BaseInstantTestCase
import kotlin.test.expect

internal class GlucoseUtilKtTest : BaseInstantTestCase() {

    @Test
    fun `sgvToUnit should return sgv when isUnitMmol is false`() {
        val sgv = sgvToUnit(180, false)

        expect(180f) { sgv }
    }

    @Test
    fun `sgvToUnit should return mmol when isUnitMmol is true`() {
        val sgv = sgvToUnit(180, true)

        expect(10.0f) { sgv }
    }
}