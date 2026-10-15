package com.brohit.truecalc.domain.calc

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UnitConverterTest {

    private fun convert(value: Double, category: UnitCategory, from: String, to: String): Double =
        UnitConverter.convert(value, category.unit(from)!!, category.unit(to)!!)

    private fun assertClose(expected: Double, actual: Double, relative: Double = 1e-9) {
        val tolerance = maxOf(Math.abs(expected) * relative, 1e-12)
        assertEquals(expected, actual, tolerance)
    }

    @Test
    fun length_knownValues() {
        assertClose(0.621371192, convert(1.0, UnitCategory.Length, "km", "mi"), 1e-8)
        assertClose(30.48, convert(1.0, UnitCategory.Length, "ft", "cm"))
        assertClose(12.0, convert(1.0, UnitCategory.Length, "ft", "in"))
        assertClose(1760.0, convert(1.0, UnitCategory.Length, "mi", "yd"))
        assertClose(1852.0, convert(1.0, UnitCategory.Length, "nmi", "m"))
    }

    @Test
    fun weight_knownValues() {
        assertClose(2.20462262, convert(1.0, UnitCategory.Weight, "kg", "lb"), 1e-8)
        assertClose(16.0, convert(1.0, UnitCategory.Weight, "lb", "oz"))
        assertClose(14.0, convert(1.0, UnitCategory.Weight, "st", "lb"))
        assertClose(100.0, convert(1.0, UnitCategory.Weight, "quintal", "kg"))
        assertClose(11.6638125, convert(1.0, UnitCategory.Weight, "tola", "g"))
    }

    @Test
    fun temperature_usesOffsets() {
        assertClose(212.0, convert(100.0, UnitCategory.Temperature, "c", "f"))
        assertClose(32.0, convert(0.0, UnitCategory.Temperature, "c", "f"))
        assertClose(37.0, convert(98.6, UnitCategory.Temperature, "f", "c"), 1e-12)
        assertClose(273.15, convert(0.0, UnitCategory.Temperature, "c", "k"))
        assertClose(-40.0, convert(-40.0, UnitCategory.Temperature, "f", "c"))
        assertClose(-459.67, convert(0.0, UnitCategory.Temperature, "k", "f"))
    }

    @Test
    fun area_includesIndianUnits() {
        assertClose(9.0, convert(1.0, UnitCategory.Area, "yd2", "ft2"))
        assertClose(43_560.0, convert(1.0, UnitCategory.Area, "acre", "ft2"))
        assertClose(2.4710538, convert(1.0, UnitCategory.Area, "ha", "acre"), 1e-7)
        assertClose(100.0, convert(1.0, UnitCategory.Area, "acre", "cent"))
        assertClose(40.0, convert(1.0, UnitCategory.Area, "acre", "guntha"))
        assertClose(UnitConverter.BIGHA_SQ_FT, convert(1.0, UnitCategory.Area, "bigha", "ft2"))
        assertClose(3_000.0, convert(1.0, UnitCategory.Area, "bigha", "yd2"))
        assertClose(640.0, convert(1.0, UnitCategory.Area, "mi2", "acre"))
    }

    @Test
    fun volume_speed_time() {
        assertClose(3.785411784, convert(1.0, UnitCategory.Volume, "gal", "l"))
        assertClose(16.0, convert(1.0, UnitCategory.Volume, "cup", "tbsp"))
        assertClose(3.0, convert(1.0, UnitCategory.Volume, "tbsp", "tsp"))
        assertClose(1000.0, convert(1.0, UnitCategory.Volume, "m3", "l"))
        assertClose(27.7777777778, convert(100.0, UnitCategory.Speed, "kmph", "mps"), 1e-10)
        assertClose(1.609344, convert(1.0, UnitCategory.Speed, "mph", "kmph"))
        assertClose(1.852, convert(1.0, UnitCategory.Speed, "knot", "kmph"))
        assertClose(1440.0, convert(1.0, UnitCategory.Time, "day", "min"))
        assertClose(365.2425, convert(1.0, UnitCategory.Time, "year", "day"))
        assertClose(12.0, convert(1.0, UnitCategory.Time, "year", "month"))
    }

    @Test
    fun data_decimalAndBinary() {
        assertClose(8.0, convert(1.0, UnitCategory.Data, "B", "bit"))
        assertClose(1000.0, convert(1.0, UnitCategory.Data, "GB", "MB"))
        assertClose(1024.0, convert(1.0, UnitCategory.Data, "GiB", "MiB"))
        assertClose(0.931322575, convert(1.0, UnitCategory.Data, "GB", "GiB"), 1e-9)
        assertClose(1e6, convert(1.0, UnitCategory.Data, "TB", "MB"))
    }

    @Test
    fun everyPair_roundTrips() {
        for (category in UnitCategory.entries) {
            for (from in category.units) for (to in category.units) {
                for (value in listOf(0.0, 1.0, 123.456, -7.5, 1e6)) {
                    val there = UnitConverter.convert(value, from, to)
                    val back = UnitConverter.convert(there, to, from)
                    val tolerance = maxOf(Math.abs(value) * 1e-9, 1e-9)
                    assertEquals("${category.key} ${from.id}→${to.id}→${from.id} for $value", value, back, tolerance)
                }
            }
        }
    }

    @Test
    fun sameUnit_isIdentity() {
        val m = UnitCategory.Length.unit("m")!!
        assertEquals(42.0, UnitConverter.convert(42.0, m, m), 0.0)
    }

    @Test
    fun convertToAll_coversEveryUnitInOrder() {
        val all = UnitConverter.convertToAll(1.0, UnitCategory.Length.unit("m")!!, UnitCategory.Length)
        assertEquals(UnitCategory.Length.units, all.map { it.first })
        assertClose(100.0, all.first { it.first.id == "cm" }.second)
    }

    @Test
    fun idsAreUniqueAndDefaultsExist() {
        for (category in UnitCategory.entries) {
            assertEquals(category.units.size, category.units.map { it.id }.toSet().size)
            assertNotNull(category.defaultFrom)
            assertNotNull(category.defaultTo)
            assertTrue(category.defaultFrom != category.defaultTo)
            assertEquals(category, UnitCategory.fromKey(category.key))
        }
        assertEquals(UnitCategory.Length, UnitCategory.fromKey("nope"))
    }

    @Test
    fun format() {
        assertEquals("0.62137119", UnitConverter.format(0.621371192237))
        assertEquals("1,00,000", UnitConverter.format(100_000.0))
        assertEquals("1E15", UnitConverter.format(1e15))
        assertEquals("212", UnitConverter.format(212.00000000000003))
    }
}
