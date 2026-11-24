package com.maksimowiczm.foodyou.app.ui.home.goals

import kotlin.test.Test
import kotlin.test.assertEquals

class MacroBarFractionsTest {
    @Test
    fun overflowRepresentsShareOfConsumedAmountAcrossAllMacros() {
        for ((amount, target) in listOf(178 to 100, 297 to 250, 159 to 67)) {
            val fractions = macroBarFractions(amount.toFloat() / target)
            assertEquals(target.toFloat() / amount, fractions.withinTarget, .0001f)
            assertEquals((amount - target).toFloat() / amount, fractions.aboveTarget, .0001f)
        }
    }

    @Test
    fun underTargetShowsKnownProgressAndNoOverflow() {
        assertEquals(MacroBarFractions(.5f, 0f), macroBarFractions(.5f))
        assertEquals(MacroBarFractions(1f, 0f), macroBarFractions(1f))
        assertEquals(MacroBarFractions(0f, 0f), macroBarFractions(Float.NaN))
    }
}
