package com.diabdata.shared.utils.dateUtils

import android.content.Context
import com.diabdata.shared.R
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period
import kotlin.math.abs

/**
 * Extension to convert a Period to a localized string representation.
 * Handles both past and future periods.
 *
 * @param context Context to use for resource lookups
 * @param referenceDate The date to compare against. Defaults to today.
 * @return A localized string representation of the period. Examples:
 * - "1 year and 3 months ago"
 * - "In 1 year and 1 month"
 * - Today, Tomorrow, Yesterday, etc.
 */
fun Period.toLocalizedString(context: Context, referenceDate: LocalDate = LocalDate.now()): String {
    val years = this.years
    val months = this.months
    val days = this.days

    val isPast = years < 0 || months < 0 || days < 0

    val absYears = abs(years)
    val absDays = abs(days)
    val absMonths = abs(months)

    return when {
        years == 0 && months == 0 && days == 0 -> context.getString(R.string.date_today)

        !isPast -> {
            when {
                years == 0 && months == 0 -> {
                    when (days) {
                        1 -> context.getString(R.string.date_tomorrow)
                        else -> context.getString(R.string.date_in_days, days)
                    }
                }

                years == 0 && days == 0 -> {
                    when (months) {
                        1 -> context.getString(R.string.date_next_month)
                        else -> context.getString(R.string.date_in_months, months)
                    }
                }

                months == 0 && days == 0 -> {
                    when (years) {
                        1 -> context.getString(R.string.date_next_year)
                        else -> context.getString(R.string.date_in_years, years)
                    }
                }

                years > 0 && months > 0 -> {
                    val yearCount = if (years == 1) 1 else 2
                    context.resources.getQuantityString(
                        R.plurals.in_years_and_months,
                        yearCount,
                        years,
                        months
                    )
                }

                years > 0 -> context.resources.getQuantityString(R.plurals.in_years, years, years)
                months > 0 -> context.resources.getQuantityString(
                    R.plurals.in_months,
                    months,
                    months
                )

                else -> context.getString(R.string.date_in_days, days)
            }
        }

        else -> {
            when {
                absYears == 0 && absMonths == 0 -> {
                    when (absDays) {
                        1 -> context.getString(R.string.date_yesterday)
                        else -> context.getString(R.string.date_days_ago, absDays)
                    }
                }

                absYears == 0 && absDays == 0 -> {
                    when (absMonths) {
                        1 -> context.getString(R.string.date_last_month)
                        else -> context.getString(R.string.date_months_ago, absMonths)
                    }
                }

                absMonths == 0 && absDays == 0 -> {
                    when (absYears) {
                        1 -> context.getString(R.string.date_last_year)
                        else -> context.getString(R.string.date_years_ago, absYears)
                    }
                }

                absYears > 0 && absMonths > 0 -> {
                    context.resources.getQuantityString(
                        R.plurals.years_and_months_ago,
                        absYears,
                        absYears,
                        absMonths
                    )
                }

                absYears > 0 -> context.getString(R.string.date_years_ago, absYears)
                absMonths > 0 -> context.getString(R.string.date_months_ago, absMonths)
                else -> context.getString(R.string.date_days_ago, absDays)
            }
        }
    }
}

/**
 * Extension to compute and return a localized string representation of a date relative to today.
 * Handles both past and future dates.
 *
 * @param context Context to use for resource lookups
 * @return [Period.toLocalizedString]
 */
fun LocalDate.toRelativeString(context: Context): String {
    val today = LocalDate.now()
    val period = Period.between(today, this)
    return period.toLocalizedString(context, today)
}

/**
 * Extension to compute and return a localized string representation of a date relative to today.
 * Handles both past and future dates.
 *
 * @param context Context to use for resource lookups
 * @return [Period.toLocalizedString]
 */
fun LocalDateTime.toRelativeString(context: Context): String {
    val today = LocalDate.now()
    val period = Period.between(today, this.toLocalDate())
    return period.toLocalizedString(context, today)
}

/**
 * Extension to compute the time between two dates
 *
 * @param context Context to use for resource lookups
 * @param fromDate The date to compare against. Defaults to today.
 * @return [Period.toLocalizedString]
 */
fun LocalDate.getRelativeStringFrom(
    context: Context,
    fromDate: LocalDate = LocalDate.now()
): String {
    val period = Period.between(fromDate, this)
    return period.toLocalizedString(context, fromDate)
}

/**
 * Helper function to simplify relative-day-related strings
 *
 * @param context Context to use for resource lookups
 * @param daysFromNow Number of days from now. Positive for future, negative for past.
 * @return A localized string representation of the number of days. Examples:
 * - "Today"
 * - "Tomorrow"
 * - "Yesterday"
 */
fun getRelativeDayString(context: Context, daysFromNow: Int): String = when {
    daysFromNow == 0 -> context.getString(R.string.date_today)
    daysFromNow == 1 -> context.getString(R.string.date_tomorrow)
    daysFromNow == -1 -> context.getString(R.string.date_yesterday)
    daysFromNow > 1 -> context.getString(R.string.date_in_days, daysFromNow)
    daysFromNow < -1 -> context.getString(R.string.date_days_ago, abs(daysFromNow))
    else -> ""
}

/**
 * Helper function to simplify relative-month-related strings
 *
 * @param context Context to use for resource lookups
 * @param monthsFromNow Number of months from now. Positive for future, negative for past.
 * @return A localized string representation of the number of months. Examples:
 * - "This month"
 * - "Next month"
 */
fun getRelativeMonthString(context: Context, monthsFromNow: Int): String = when {
    monthsFromNow == 0 -> context.getString(R.string.date_this_month)
    monthsFromNow == 1 -> context.getString(R.string.date_next_month)
    monthsFromNow == -1 -> context.getString(R.string.date_last_month)
    monthsFromNow > 1 -> context.getString(R.string.date_in_months, monthsFromNow)
    monthsFromNow < -1 -> context.getString(R.string.date_months_ago, abs(monthsFromNow))
    else -> ""
}

/**
 * Helper function to simplify relative-year-related strings
 *
 * @param context Context to use for resource lookups
 * @param yearsFromNow Number of years from now. Positive for future, negative for past.
 * @return A localized string representation of the number of years. Examples:
 * - "This year"
 * - "Next year"
 */
fun getRelativeYearString(context: Context, yearsFromNow: Int): String = when {
    yearsFromNow == 0 -> context.getString(R.string.date_this_year)
    yearsFromNow == 1 -> context.getString(R.string.date_next_year)
    yearsFromNow == -1 -> context.getString(R.string.date_last_year)
    yearsFromNow > 1 -> context.getString(R.string.date_in_years, yearsFromNow)
    yearsFromNow < -1 -> context.getString(R.string.date_years_ago, abs(yearsFromNow))
    else -> ""
}

/**
 * Extension to compute the number of days, months and/or years between two dates
 *
 * @param context Context to use for resource lookups
 * @return A localized string representation of the number of days, months and/or years. Examples:
 * - "1 year, 2 months and 3 days"
 * - "2 months and 1 day"
 */
fun Period.toCountString(context: Context): String {
    val absYears = abs(years)
    val absMonths = abs(months)
    val absDays = abs(days)

    val parts = mutableListOf<String>()

    if (absYears > 0) {
        parts.add(
            context.resources.getQuantityString(
                R.plurals.plurals_years,
                absYears,
                absYears
            )
        )
    }

    if (absMonths > 0) {
        parts.add(
            context.resources.getQuantityString(
                R.plurals.plurals_months,
                absMonths,
                absMonths
            )
        )
    }

    if (absDays > 0) {
        parts.add(
            context.resources.getQuantityString(
                R.plurals.plurals_days,
                absDays,
                absDays
            )
        )
    }

    if (parts.isEmpty()) {
        return context.resources.getString(R.string.date_zero_days)
    }

    return when (parts.size) {
        1 -> parts[0]
        2 -> "${parts[0]} ${context.getString(R.string.string_fragment_and)} ${parts[1]}"
        3 -> "${parts[0]}, ${parts[1]} ${context.getString(
            R.string.string_fragment_and
        )} ${parts[2]}"
        else -> parts[0]
    }
}
