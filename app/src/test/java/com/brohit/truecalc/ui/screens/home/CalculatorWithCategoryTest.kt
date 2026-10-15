package com.brohit.truecalc.ui.screens.home

import com.brohit.truecalc.ui.navigation.Route
import com.brohit.truecalc.ui.screens.compund_interest.CalculationType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculatorWithCategoryTest {

    @Test
    fun toCalculator_resolvesEachCompoundInterestVariantToItsOwnTool() {
        assertEquals(
            "Compound Interest",
            Route.Calculator.CompoundInterest(CalculationType.COMPOUND).toCalculator()?.name
        )
        assertEquals(
            "Simple Interest",
            Route.Calculator.CompoundInterest(CalculationType.SIMPLE).toCalculator()?.name
        )
        assertEquals(
            "Fixed Deposit",
            Route.Calculator.CompoundInterest(CalculationType.FIXED).toCalculator()?.name
        )
    }

    @Test
    fun toCalculator_ignoresRestorationJson() {
        assertEquals(
            "Simple Interest",
            Route.Calculator.CompoundInterest(CalculationType.SIMPLE, "{}").toCalculator()?.name
        )
        assertEquals("EMI Calculator", Route.Calculator.Emi("{}").toCalculator()?.name)
    }

    @Test
    fun isSameTool_distinguishesRouteClassesAndCalculationTypes() {
        assertTrue(Route.Calculator.Bmi().isSameTool(Route.Calculator.Bmi("{}")))
        assertFalse(Route.Calculator.Bmi().isSameTool(Route.Calculator.Calorie()))
        assertFalse(
            Route.Calculator.CompoundInterest(CalculationType.SIMPLE)
                .isSameTool(Route.Calculator.CompoundInterest(CalculationType.FIXED))
        )
    }
}
