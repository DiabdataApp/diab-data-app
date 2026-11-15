package com.diabdata.shared.utils.dateUtils

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period
import java.time.temporal.ChronoUnit

fun Period.getNumberOfDays(): Int = this.days

fun Period.getNumberOfMonths(): Int = this.months

fun Period.getNumberOfYears(): Int = this.years

fun LocalDate.getNumberOfDaysUntil(date: LocalDate = LocalDate.now()): Int =
    ChronoUnit.DAYS.between(date, this).toInt()

fun LocalDateTime.getNumberOfDaysUntil(date: LocalDateTime = LocalDateTime.now()): Int =
    ChronoUnit.DAYS.between(date.toLocalDate(), this.toLocalDate()).toInt()

fun LocalDate.getNumberOfMonths(date: LocalDate = LocalDate.now()): Int =
    ChronoUnit.MONTHS.between(date, this).toInt()

fun LocalDate.getNumberOfYears(date: LocalDate = LocalDate.now()): Int =
    ChronoUnit.YEARS.between(date, this).toInt()
