package com.brohit.truecalc.ui.tools

import com.brohit.truecalc.ui.navigation.Route
import com.brohit.truecalc.ui.screens.compund_interest.CalculationType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ToolRegistryTest {

    @Test
    fun registersTheEightExistingToolsWithStableIds() {
        assertEquals(
            listOf(
                "emi", "compound_interest", "simple_interest", "fixed_deposit",
                "recurring_deposit", "bmi", "calories", "ideal_weight",
            ),
            ToolRegistry.all.map { it.id },
        )
        assertEquals("BMI Calculator", ToolRegistry.byId("bmi")?.name)
        assertNull(ToolRegistry.byId("nope"))
    }

    @Test
    fun forRoute_distinguishesInterestVariantsAndIgnoresRestorationState() {
        assertEquals("compound_interest", ToolRegistry.forRoute(Route.Calculator.CompoundInterest(CalculationType.COMPOUND))?.id)
        assertEquals("simple_interest", ToolRegistry.forRoute(Route.Calculator.CompoundInterest(CalculationType.SIMPLE, "{}"))?.id)
        assertEquals("fixed_deposit", ToolRegistry.forRoute(Route.Calculator.CompoundInterest(CalculationType.FIXED))?.id)
        assertEquals("emi", ToolRegistry.forRoute(Route.Calculator.Emi("{\"principal\":\"1\"}"))?.id)
        assertNull(ToolRegistry.forRoute(Route.SettingsScreen))
    }

    @Test
    fun byCategory_keepsCategoryOrderAndDropsEmptyOnes() {
        val grouped = ToolRegistry.byCategory()
        assertEquals(listOf(ToolCategory.Finance, ToolCategory.Health), grouped.keys.toList())
        assertEquals(5, grouped.getValue(ToolCategory.Finance).size)
        assertEquals(3, grouped.getValue(ToolCategory.Health).size)
    }

    @Test
    fun search_usesKeywords() {
        assertEquals("emi", ToolRegistry.search("home loan").first().id)
        assertEquals("emi", ToolRegistry.search("Mortgage").first().id)
        assertEquals("fixed_deposit", ToolRegistry.search("fd").first().id)
        assertEquals("calories", ToolRegistry.search("kcal").first().id)
        assertEquals("ideal_weight", ToolRegistry.search("weight").first().id)
        assertEquals(listOf("fixed_deposit", "recurring_deposit"), ToolRegistry.search("deposit").map { it.id })
        assertEquals(emptyList<ToolSpec>(), ToolRegistry.search(""))
    }
}
