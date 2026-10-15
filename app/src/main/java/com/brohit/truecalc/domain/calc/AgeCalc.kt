package com.brohit.truecalc.domain.calc

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.Month
import java.time.Period
import java.time.temporal.ChronoUnit

data class AgeResult(
    val years: Int,
    val months: Int,
    val days: Int,
    /** Whole months lived (years × 12 + months). */
    val totalMonths: Long,
    val totalDays: Long,
    /** totalDays / 7, with [remainderDays] left over. */
    val totalWeeks: Long,
    val remainderDays: Long,
    val bornOn: DayOfWeek,
    /** The next birthday on or after the "as of" date. */
    val nextBirthday: LocalDate,
    val daysToNextBirthday: Long,
    /** True when the "as of" date is a birthday ([daysToNextBirthday] is then 0). */
    val isBirthdayToday: Boolean,
    /** Age reached on [nextBirthday]. */
    val nextAge: Int,
)

/**
 * Exact age between a date of birth and an "as of" date.
 *
 * Leap-day birthdays: in non-leap years the birthday is taken as **1 March** (the day after
 * 28 February), so someone born on 29 Feb 2000 turns 1 on 1 Mar 2001 and is 0 y 11 m 30 d on
 * 28 Feb 2001. Months and days are counted from the last birthday with [Period], so a month is a
 * calendar month (31 Jan → 28 Feb is 0 m 28 d; → 1 Mar is 1 m 1 d).
 */
object AgeCalc {

    /** The birthday of someone born on [dob] in [year], applying the leap-day rule. */
    fun birthdayIn(dob: LocalDate, year: Int): LocalDate =
        if (dob.month == Month.FEBRUARY && dob.dayOfMonth == 29 && !java.time.Year.isLeap(year.toLong())) {
            LocalDate.of(year, Month.MARCH, 1)
        } else {
            dob.withYear(year)
        }

    /** Null when [dob] is after [asOf]. */
    fun calculate(dob: LocalDate, asOf: LocalDate): AgeResult? {
        if (dob.isAfter(asOf)) return null

        var years = asOf.year - dob.year
        if (birthdayIn(dob, asOf.year).isAfter(asOf)) years -= 1
        val lastBirthday = if (years == 0) dob else birthdayIn(dob, dob.year + years)
        val sinceBirthday = Period.between(lastBirthday, asOf)

        val totalDays = ChronoUnit.DAYS.between(dob, asOf)

        val thisYears = birthdayIn(dob, asOf.year)
        val next = if (thisYears.isBefore(asOf)) birthdayIn(dob, asOf.year + 1) else thisYears
        // Born today: the "next" birthday is the first one, a year from now.
        val nextBirthday = if (next == dob) birthdayIn(dob, dob.year + 1) else next

        return AgeResult(
            years = years,
            months = sinceBirthday.months,
            days = sinceBirthday.days,
            totalMonths = years * 12L + sinceBirthday.months,
            totalDays = totalDays,
            totalWeeks = totalDays / 7,
            remainderDays = totalDays % 7,
            bornOn = dob.dayOfWeek,
            nextBirthday = nextBirthday,
            daysToNextBirthday = ChronoUnit.DAYS.between(asOf, nextBirthday),
            isBirthdayToday = nextBirthday == asOf,
            nextAge = nextBirthday.year - dob.year,
        )
    }
}
