package com.diabdata.shared.utils.dateUtils

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

fun shortenedFormatLocalDate(date: LocalDate, locale: Locale = Locale.getDefault()): String {
    val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).withLocale(locale)
    return date.format(formatter)
}

fun String.toShortenedFormatLocalDate(locale: Locale = Locale.getDefault()): String {
    val date = LocalDate.parse(this)
    return shortenedFormatLocalDate(date, locale)
}

@Suppress("unused")
fun String.toShortenedFormatLocalDateTime(locale: Locale = Locale.getDefault()): String {
    val date = LocalDateTime.parse(this)
    return shortenedFormatLocalDate(date.toLocalDate())
}

fun formatLocalDate(
    date: LocalDate,
    pattern: String = "dd MMM yyyy",
    locale: Locale = Locale.getDefault()
): String {
    val formatter = DateTimeFormatter.ofPattern(pattern, locale)
    return date.format(formatter)
}

fun formatLocalDateTime(
    date: LocalDateTime,
    pattern: String = "dd MMM yyyy HH:mm",
    locale: Locale = Locale.getDefault()
): String {
    val formatter = DateTimeFormatter.ofPattern(pattern, locale)
    return date.format(formatter)
}

@Suppress("unused")
fun String.toFormatLocalDate(locale: Locale = Locale.getDefault()): String {
    val date = LocalDate.parse(this)
    return formatLocalDate(date, locale = locale)
}

fun String.formatDateToLocale(locale: Locale = Locale.getDefault()): String {
    val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
    val dateTime = LocalDateTime.parse(this, inputFormatter)

    val outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm", locale)
    return dateTime.format(outputFormatter)
}

fun Long.formatDateToLocale(locale: Locale = Locale.getDefault()): String {
    val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
    val dateTime = Instant.ofEpochMilli(this)
        .atZone(ZoneId.systemDefault())

    val outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm", locale)
    return dateTime.format(outputFormatter)
}
