package com.brohit.truecalc.ui.tools

import com.brohit.truecalc.ui.navigation.Route
import com.brohit.truecalc.ui.screens.compund_interest.CalculationType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ToolRegistryTest {

    @Test
    fun registersAllToolsWithStableIds() {
        assertEquals(
            listOf(
                "emi", "compound_interest", "simple_interest", "fixed_deposit",
                "recurring_deposit", "bmi", "calories", "ideal_weight",
                "calculator", "percentage", "discount", "gst", "split_bill", "age", "date_calc",
                "unit_converter",
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
        assertEquals(
            listOf(ToolCategory.Finance, ToolCategory.Health, ToolCategory.Everyday, ToolCategory.Convert),
            grouped.keys.toList(),
        )
        assertEquals(5, grouped.getValue(ToolCategory.Finance).size)
        assertEquals(3, grouped.getValue(ToolCategory.Health).size)
        assertEquals(7, grouped.getValue(ToolCategory.Everyday).size)
        assertEquals(listOf("unit_converter"), grouped.getValue(ToolCategory.Convert).map { it.id })
    }

    @Test
    fun everydayPack_isMarkedNew() {
        val newIds = ToolRegistry.all.filter { it.isNew }.map { it.id }.toSet()
        assertEquals(
            setOf("calculator", "percentage", "discount", "gst", "split_bill", "age", "date_calc", "unit_converter"),
            newIds,
        )
    }

    @Test
    fun search_findsEverydayTools() {
        assertEquals("calculator", ToolRegistry.search("calculator").first().id)
        assertEquals("calculator", ToolRegistry.search("scientific").first().id)
        assertEquals("percentage", ToolRegistry.search("percent").first().id)
        assertEquals("discount", ToolRegistry.search("sale").first().id)
        assertEquals("gst", ToolRegistry.search("cgst").first().id)
        assertEquals("gst", ToolRegistry.search("gst").first().id)
        assertEquals("split_bill", ToolRegistry.search("tip").first().id)
        assertEquals("unit_converter", ToolRegistry.search("km to miles").first().id)
        assertEquals("unit_converter", ToolRegistry.search("bigha").first().id)
        assertEquals("age", ToolRegistry.search("birthday").first().id)
        assertEquals("date_calc", ToolRegistry.search("working days").first().id)
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
