package com.brohit.truecalc.domain.calc

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HealthCalcTest {

    @Test
    fun bmi_knownValue() {
        val result = BmiCalc.calculate(70.0, 175.0)!!
        assertEquals(70 / (1.75 * 1.75), result.bmi, 1e-9)
        assertEquals(22.857, result.bmi, 1e-3)
        assertEquals(BmiCategory.Normal, result.category)
        assertEquals("Normal weight", result.category.label)
    }

    @Test
    fun bmi_categoryThresholds_matchOriginal() {
        assertEquals(BmiCategory.Underweight, BmiCalc.categoryOf(18.49))
        assertEquals(BmiCategory.Normal, BmiCalc.categoryOf(18.5))
        assertEquals(BmiCategory.Normal, BmiCalc.categoryOf(24.89))
        assertEquals(BmiCategory.Overweight, BmiCalc.categoryOf(24.9))
        assertEquals(BmiCategory.Obesity, BmiCalc.categoryOf(29.9))
    }

    @Test
    fun bmi_invalidInput_returnsNull() {
        assertNull(BmiCalc.calculate("70", "0"))
        assertNull(BmiCalc.calculate("", "170"))
        assertNull(BmiCalc.calculate("70", ""))
    }

    @Test
    fun bmi_healthyWeightRange() {
        val range = BmiCalc.healthyWeightRange(175.0)!!
        assertEquals(18.5 * 1.75 * 1.75, range.start, 1e-9)
        assertEquals(24.9 * 1.75 * 1.75, range.endInclusive, 1e-9)
        assertEquals(BmiCategory.Normal, BmiCalc.calculate(range.start + 0.01, 175.0)!!.category)
        assertEquals(BmiCategory.Normal, BmiCalc.calculate(range.endInclusive - 0.01, 175.0)!!.category)
        assertNull(BmiCalc.healthyWeightRange(0.0))
    }

    @Test
    fun calorie_mifflinStJeor() {
        // Male: 10*70 + 6.25*175 - 5*30 + 5 = 1648.75
        val male = CalorieCalc.calculate(30, 70.0, 175.0, Gender.Male, ActivityLevel.Sedentary)
        assertEquals(1_648.75, male.bmr, 1e-9)
        assertEquals(1_978.5, male.calories, 1e-9)
        // Female: same minus 166
        val female = CalorieCalc.calculate("30", "70", "175", Gender.Female, ActivityLevel.ModeratelyActive)!!
        assertEquals(1_482.75, female.bmr, 1e-9)
        assertEquals(1_482.75 * 1.55, female.calories, 1e-9)
    }

    @Test
    fun calorie_invalidInput_returnsNull() {
        assertNull(CalorieCalc.calculate("30.5", "70", "175", Gender.Male, ActivityLevel.Sedentary))
        assertNull(CalorieCalc.calculate("30", "", "175", Gender.Male, ActivityLevel.Sedentary))
    }

    @Test
    fun labels_roundTripPersistedValues() {
        assertEquals(Gender.Male, Gender.fromLabel("Male"))
        assertEquals(Gender.Female, Gender.fromLabel("Female"))
        assertEquals(Gender.Female, Gender.fromLabel("anything"))
        ActivityLevel.entries.forEach { assertEquals(it, ActivityLevel.fromLabel(it.label)) }
        assertEquals(ActivityLevel.Sedentary, ActivityLevel.fromLabel("unknown"))
        assertEquals(listOf(1.2, 1.375, 1.55, 1.725, 1.9), ActivityLevel.entries.map { it.factor })
    }

    @Test
    fun idealWeight_devine() {
        // 180 cm = 70.866 in -> 10.866 in over 5 ft
        val expectedExtra = 180 / 2.54 - 60
        assertEquals(50 + 2.3 * expectedExtra, IdealWeightCalc.calculate(180.0, Gender.Male), 1e-9)
        assertEquals(45.5 + 2.3 * expectedExtra, IdealWeightCalc.calculate(180.0, Gender.Female), 1e-9)
        // Below 5 ft is clamped to the base weight.
        assertEquals(50.0, IdealWeightCalc.calculate(150.0, Gender.Male), 0.0)
        assertEquals(45.5, IdealWeightCalc.calculate("150", Gender.Female)!!, 0.0)
        assertNull(IdealWeightCalc.calculate("", Gender.Male))
    }
}
