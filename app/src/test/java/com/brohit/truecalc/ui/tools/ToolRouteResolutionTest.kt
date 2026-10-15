package com.brohit.truecalc.ui.tools

import com.brohit.truecalc.ui.navigation.Route
import com.brohit.truecalc.ui.navigation.withRestorationJson
import com.brohit.truecalc.ui.screens.compund_interest.CalculationType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlinx.serialization.json.Json

/** Route → tool resolution, including the route JSON already stored in users' Room databases. */
class ToolRouteResolutionTest {

    @Test
    fun forRoute_resolvesEachCompoundInterestVariantToItsOwnTool() {
        assertEquals(
            "Compound Interest",
            ToolRegistry.forRoute(Route.Calculator.CompoundInterest(CalculationType.COMPOUND))?.name
        )
        assertEquals(
            "Simple Interest",
            ToolRegistry.forRoute(Route.Calculator.CompoundInterest(CalculationType.SIMPLE))?.name
        )
        assertEquals(
            "Fixed Deposit",
            ToolRegistry.forRoute(Route.Calculator.CompoundInterest(CalculationType.FIXED))?.name
        )
    }

    @Test
    fun forRoute_ignoresRestorationJson() {
        assertEquals(
            "Simple Interest",
            ToolRegistry.forRoute(Route.Calculator.CompoundInterest(CalculationType.SIMPLE, "{}"))?.name
        )
        assertEquals("EMI Calculator", ToolRegistry.forRoute(Route.Calculator.Emi("{}"))?.name)
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

    @Test
    fun routeKey_keepsTheStoredFavouriteAndUsageFormat() {
        assertEquals("{\"type\":\"EmiCalculator\"}", ToolRegistry.byId("emi")?.routeKey)
        assertEquals(
            "{\"type\":\"CompoundInterestCalculator\",\"calculationType\":\"Simple Interest\"}",
            ToolRegistry.byId("simple_interest")?.routeKey
        )
        assertEquals("{\"type\":\"BmiCalculator\"}", ToolRegistry.byId("bmi")?.routeKey)
    }

    @Test
    fun everydayRoutes_haveStableKeys() {
        assertEquals("{\"type\":\"BasicCalculator\"}", ToolRegistry.byId("calculator")?.routeKey)
        assertEquals("{\"type\":\"PercentageCalculator\"}", ToolRegistry.byId("percentage")?.routeKey)
        assertEquals("{\"type\":\"DiscountCalculator\"}", ToolRegistry.byId("discount")?.routeKey)
        assertEquals("{\"type\":\"GstCalculator\"}", ToolRegistry.byId("gst")?.routeKey)
        assertEquals("{\"type\":\"SplitBillCalculator\"}", ToolRegistry.byId("split_bill")?.routeKey)
        assertEquals("{\"type\":\"UnitConverter\"}", ToolRegistry.byId("unit_converter")?.routeKey)
        assertEquals("{\"type\":\"AgeCalculator\"}", ToolRegistry.byId("age")?.routeKey)
        assertEquals("{\"type\":\"DateCalculator\"}", ToolRegistry.byId("date_calc")?.routeKey)
    }

    @Test
    fun withRestorationJson_coversEveryRegisteredTool() {
        val json = "{\"k\":\"v\"}"
        ToolRegistry.all.forEach { tool ->
            val restored = tool.route.withRestorationJson(json)
            assertTrue("${tool.id} keeps its tool", restored.isSameTool(tool.route))
            assertTrue(
                "${tool.id} carries restorationJson",
                Json.encodeToString(Route.serializer(), restored).contains("restorationJson"),
            )
        }
    }

    @Test
    fun forRouteKey_decodesStoredJsonAndRejectsGarbage() {
        assertEquals("emi", ToolRegistry.forRouteKey("{\"type\":\"EmiCalculator\"}")?.id)
        assertEquals(
            "fixed_deposit",
            ToolRegistry.forRouteKey(
                "{\"type\":\"CompoundInterestCalculator\",\"calculationType\":\"Fixed Deposit\",\"restorationJson\":\"{}\"}"
            )?.id
        )
        assertNull(ToolRegistry.forRouteKey("not json"))
        assertNull(ToolRegistry.forRouteKey("{\"type\":\"RemovedCalculator\"}"))
        ToolRegistry.all.forEach { assertEquals(it.id, ToolRegistry.forRouteKey(it.routeKey)?.id) }
    }

    @Test
    fun forRouteKeys_dropsUnknownAndDuplicateToolsAndLimits() {
        val keys = listOf(
            "{\"type\":\"BmiCalculator\"}",
            "garbage",
            "{\"type\":\"BmiCalculator\",\"restorationJson\":\"{}\"}",
            "{\"type\":\"EmiCalculator\"}",
            "{\"type\":\"CalorieCalculator\"}",
        )
        assertEquals(listOf("bmi", "emi", "calories"), ToolRegistry.forRouteKeys(keys).map { it.id })
        assertEquals(listOf("bmi", "emi"), ToolRegistry.forRouteKeys(keys, limit = 2).map { it.id })
    }

    @Test
    fun categoryForKey_usesEnumNames() {
        assertEquals(ToolCategory.Finance, ToolRegistry.categoryForKey("Finance"))
        assertEquals(ToolCategory.Health, ToolRegistry.categoryForKey("Health"))
        assertNull(ToolRegistry.categoryForKey("Financial"))
        assertEquals(5, ToolRegistry.inCategory(ToolCategory.Finance).size)
    }
}
