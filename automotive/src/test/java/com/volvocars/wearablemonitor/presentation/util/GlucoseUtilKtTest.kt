package com.volvocars.wearablemonitor.presentation.util

import com.volvocars.wearablemonitor.BaseInstantTestCase
import kotlin.test.expect
import org.junit.Test

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
