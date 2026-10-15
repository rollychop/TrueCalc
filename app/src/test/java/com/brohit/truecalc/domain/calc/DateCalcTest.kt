package com.brohit.truecalc.domain.calc

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate

class DateCalcTest {

    private fun d(text: String): LocalDate = LocalDate.parse(text)

    // ---- Age ------------------------------------------------------------------------------

    @Test
    fun age_simple() {
        val r = AgeCalc.calculate(d("1990-05-15"), d("2026-10-15"))!!
        assertEquals(36, r.years)
        assertEquals(5, r.months)
        assertEquals(0, r.days)
        assertEquals(36 * 12L + 5, r.totalMonths)
        assertEquals(java.time.temporal.ChronoUnit.DAYS.between(d("1990-05-15"), d("2026-10-15")), r.totalDays)
        assertEquals(r.totalDays / 7, r.totalWeeks)
        assertEquals(r.totalDays % 7, r.remainderDays)
        assertEquals(DayOfWeek.TUESDAY, r.bornOn)
        assertEquals(d("2027-05-15"), r.nextBirthday)
        assertEquals(212, r.daysToNextBirthday)
        assertEquals(37, r.nextAge)
        assertFalse(r.isBirthdayToday)
    }

    @Test
    fun age_dayBeforeAndOnBirthday() {
        val before = AgeCalc.calculate(d("2000-03-10"), d("2026-03-09"))!!
        assertEquals(25, before.years)
        assertEquals(11, before.months)
        assertEquals(27, before.days) // 10 Feb → 9 Mar 2026 (28-day Feb)
        assertEquals(1, before.daysToNextBirthday)

        val on = AgeCalc.calculate(d("2000-03-10"), d("2026-03-10"))!!
        assertEquals(26, on.years)
        assertEquals(0, on.months)
        assertEquals(0, on.days)
        assertTrue(on.isBirthdayToday)
        assertEquals(0, on.daysToNextBirthday)
        assertEquals(26, on.nextAge)
    }

    @Test
    fun age_leapDayBirthday_inNonLeapYear_turnsOnMarch1() {
        val dob = d("2000-02-29")
        val feb28 = AgeCalc.calculate(dob, d("2001-02-28"))!!
        assertEquals(0, feb28.years)
        assertEquals(11, feb28.months)
        assertEquals(30, feb28.days)
        assertEquals(d("2001-03-01"), feb28.nextBirthday)
        assertEquals(1, feb28.daysToNextBirthday)

        val mar1 = AgeCalc.calculate(dob, d("2001-03-01"))!!
        assertEquals(1, mar1.years)
        assertEquals(0, mar1.months)
        assertEquals(0, mar1.days)
        assertTrue(mar1.isBirthdayToday)
    }

    @Test
    fun age_leapDayBirthday_inLeapYear() {
        val dob = d("2000-02-29")
        val r = AgeCalc.calculate(dob, d("2024-02-29"))!!
        assertEquals(24, r.years)
        assertEquals(0, r.months)
        assertEquals(0, r.days)
        assertTrue(r.isBirthdayToday)

        val dayBefore = AgeCalc.calculate(dob, d("2024-02-28"))!!
        assertEquals(23, dayBefore.years)
        assertEquals(11, dayBefore.months)
        assertEquals(27, dayBefore.days) // from 1 Mar 2023
        assertEquals(d("2024-02-29"), dayBefore.nextBirthday)

        // From a leap year into the next (non-leap) year: next birthday is 1 March.
        val after = AgeCalc.calculate(dob, d("2024-06-01"))!!
        assertEquals(d("2025-03-01"), after.nextBirthday)
        assertEquals(25, after.nextAge)
    }

    @Test
    fun age_monthsNeverReach12() {
        val dob = d("2000-02-29")
        var day = d("2000-02-29")
        while (day.isBefore(d("2009-01-01"))) {
            val r = AgeCalc.calculate(dob, day)!!
            assertTrue("months ${r.months} on $day", r.months in 0..11)
            assertTrue("days ${r.days} on $day", r.days in 0..30)
            day = day.plusDays(1)
        }
    }

    @Test
    fun age_bornToday() {
        val r = AgeCalc.calculate(d("2026-10-15"), d("2026-10-15"))!!
        assertEquals(0, r.years)
        assertEquals(0L, r.totalDays)
        assertEquals(d("2027-10-15"), r.nextBirthday)
        assertFalse(r.isBirthdayToday)
        assertEquals(1, r.nextAge)
    }

    @Test
    fun age_futureDob_isNull() {
        assertNull(AgeCalc.calculate(d("2030-01-01"), d("2026-10-15")))
    }

    // ---- Date difference ------------------------------------------------------------------

    @Test
    fun between_basic() {
        val r = DateCalc.between(d("2026-01-01"), d("2026-12-31"))
        assertEquals(364L, r.totalDays)
        assertEquals(52L, r.weeks)
        assertEquals(0L, r.weekRemainderDays)
        assertEquals(11L, r.months)
        assertEquals(30, r.monthRemainderDays)
        assertFalse(r.reversed)
    }

    @Test
    fun between_includeEndDate_addsOneDay() {
        val r = DateCalc.between(d("2026-01-01"), d("2026-12-31"), includeEndDate = true)
        assertEquals(365L, r.totalDays)
        assertEquals(1, r.years)
        assertEquals(0, r.yearRemainderMonths)
        assertEquals(12L, r.months)
        assertEquals(0, r.monthRemainderDays)
    }

    @Test
    fun between_reversedDates() {
        val r = DateCalc.between(d("2026-03-10"), d("2026-03-01"))
        assertTrue(r.reversed)
        assertEquals(9L, r.totalDays)
        assertEquals(d("2026-03-01"), r.start)
    }

    @Test
    fun between_sameDay() {
        assertEquals(0L, DateCalc.between(d("2026-03-10"), d("2026-03-10")).totalDays)
        assertEquals(1L, DateCalc.between(d("2026-03-10"), d("2026-03-10"), includeEndDate = true).totalDays)
    }

    @Test
    fun workingDays() {
        // Mon 5 Oct 2026 → Mon 12 Oct 2026: 5 working days, 2 weekend days.
        val week = DateCalc.between(d("2026-10-05"), d("2026-10-12"))
        assertEquals(5L, week.workingDays)
        assertEquals(2L, week.weekendDays)
        // Fri → Mon (exclusive): only Friday.
        assertEquals(1L, DateCalc.between(d("2026-10-09"), d("2026-10-12")).workingDays)
        // Including the end Monday.
        assertEquals(2L, DateCalc.between(d("2026-10-09"), d("2026-10-12"), includeEndDate = true).workingDays)
        // Sat → Sun inclusive: none.
        assertEquals(0L, DateCalc.between(d("2026-10-10"), d("2026-10-11"), includeEndDate = true).workingDays)
        // Whole of 2026 has 261 weekdays.
        assertEquals(261L, DateCalc.between(d("2026-01-01"), d("2026-12-31"), includeEndDate = true).workingDays)
    }

    @Test
    fun workingDays_matchesBruteForce() {
        val start = d("2026-01-01")
        for (len in 0L..60L) {
            val end = start.plusDays(len)
            var brute = 0L
            var day = start
            while (day.isBefore(end)) {
                if (day.dayOfWeek != DayOfWeek.SATURDAY && day.dayOfWeek != DayOfWeek.SUNDAY) brute++
                day = day.plusDays(1)
            }
            assertEquals(brute, DateCalc.workingDays(start, end))
        }
    }

    // ---- Add / subtract -------------------------------------------------------------------

    @Test
    fun add_daysWeeksMonthsYears() {
        assertEquals(d("2026-11-14"), DateCalc.add(d("2026-10-15"), 30, DateUnit.Days))
        assertEquals(d("2026-09-15"), DateCalc.add(d("2026-10-15"), 30, DateUnit.Days, subtract = true))
        assertEquals(d("2026-10-29"), DateCalc.add(d("2026-10-15"), 2, DateUnit.Weeks))
        assertEquals(d("2027-01-15"), DateCalc.add(d("2026-10-15"), 3, DateUnit.Months))
        assertEquals(d("2031-10-15"), DateCalc.add(d("2026-10-15"), 5, DateUnit.Years))
    }

    @Test
    fun add_monthsClampToMonthEnd() {
        assertEquals(d("2026-02-28"), DateCalc.add(d("2026-01-31"), 1, DateUnit.Months))
        assertEquals(d("2028-02-29"), DateCalc.add(d("2028-01-31"), 1, DateUnit.Months))
        assertEquals(d("2027-02-28"), DateCalc.add(d("2028-02-29"), 1, DateUnit.Years, subtract = true))
    }

    @Test
    fun modeKeys_roundTrip() {
        DateCalcMode.entries.forEach { assertEquals(it, DateCalcMode.fromKey(it.key)) }
        DateUnit.entries.forEach { assertEquals(it, DateUnit.fromKey(it.key)) }
        assertEquals(DateUnit.Days, DateUnit.fromKey(null))
    }
}
