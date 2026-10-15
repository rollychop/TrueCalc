package com.brohit.truecalc.domain.calc

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.Period
import java.time.temporal.ChronoUnit

/** Date calculator modes. [key] is stored in history. */
enum class DateCalcMode(val key: String, val label: String) {
    Between("between", "Days between"),
    AddSubtract("add", "Add / subtract");

    companion object {
        fun fromKey(key: String?): DateCalcMode = entries.firstOrNull { it.key == key } ?: Between
    }
}

/** Unit for [DateCalc.add]. [key] is stored in history. */
enum class DateUnit(val key: String, val label: String) {
    Days("days", "Days"),
    Weeks("weeks", "Weeks"),
    Months("months", "Months"),
    Years("years", "Years");

    companion object {
        fun fromKey(key: String?): DateUnit = entries.firstOrNull { it.key == key } ?: Days
    }
}

data class DateDifference(
    /** Earlier of the two dates. */
    val start: LocalDate,
    /** Later of the two dates. */
    val end: LocalDate,
    /** True when the dates were entered later-first (the result is then "before"). */
    val reversed: Boolean,
    val totalDays: Long,
    /** totalDays as whole weeks plus [weekRemainderDays]. */
    val weeks: Long,
    val weekRemainderDays: Long,
    /** Calendar difference: whole months (years × 12 + months) plus [monthRemainderDays]. */
    val months: Long,
    val monthRemainderDays: Int,
    val years: Int,
    /** Months after [years] whole years (for "1 y 2 m 3 d"). */
    val yearRemainderMonths: Int,
    /** Monday–Friday days counted. */
    val workingDays: Long,
    val weekendDays: Long,
)

/** Date arithmetic for the Date calculator. Pure java.time. */
object DateCalc {

    /**
     * Difference between two dates. By default the end date is not counted (1 → 2 Jan is 1 day);
     * with [includeEndDate] it is (1 → 2 Jan is 2 days, both days counted). Working days are
     * Monday to Friday within the same counted span; public holidays are not considered.
     */
    fun between(first: LocalDate, second: LocalDate, includeEndDate: Boolean = false): DateDifference {
        val reversed = second.isBefore(first)
        val start = if (reversed) second else first
        val end = if (reversed) first else second
        // The exclusive end of the counted span.
        val spanEnd = if (includeEndDate) end.plusDays(1) else end

        val totalDays = ChronoUnit.DAYS.between(start, spanEnd)
        val period = Period.between(start, spanEnd)
        val working = workingDays(start, spanEnd)
        return DateDifference(
            start = start,
            end = end,
            reversed = reversed,
            totalDays = totalDays,
            weeks = totalDays / 7,
            weekRemainderDays = totalDays % 7,
            months = period.toTotalMonths(),
            monthRemainderDays = period.days,
            years = period.years,
            yearRemainderMonths = period.months,
            workingDays = working,
            weekendDays = totalDays - working,
        )
    }

    /** Monday–Friday days in [start, endExclusive). */
    fun workingDays(start: LocalDate, endExclusive: LocalDate): Long {
        val total = ChronoUnit.DAYS.between(start, endExclusive)
        if (total <= 0) return 0
        val fullWeeks = total / 7
        var count = fullWeeks * 5
        var day = start.plusDays(fullWeeks * 7)
        while (day.isBefore(endExclusive)) {
            if (day.dayOfWeek != DayOfWeek.SATURDAY && day.dayOfWeek != DayOfWeek.SUNDAY) count++
            day = day.plusDays(1)
        }
        return count
    }

    /**
     * [date] plus (or minus, when [subtract]) [amount] [unit]s. Adding months or years clamps to
     * the end of a shorter month (31 Jan + 1 month = 28/29 Feb).
     */
    fun add(date: LocalDate, amount: Long, unit: DateUnit, subtract: Boolean = false): LocalDate {
        val signed = if (subtract) -amount else amount
        return when (unit) {
            DateUnit.Days -> date.plusDays(signed)
            DateUnit.Weeks -> date.plusWeeks(signed)
            DateUnit.Months -> date.plusMonths(signed)
            DateUnit.Years -> date.plusYears(signed)
        }
    }
}
