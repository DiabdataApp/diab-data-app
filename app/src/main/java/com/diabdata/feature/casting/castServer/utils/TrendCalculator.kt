package com.diabdata.feature.casting.castServer.utils

import com.diabdata.core.utils.linearRegression
import java.time.LocalDate
import java.time.temporal.ChronoUnit

fun <T> computeTrend(
    entries: List<T>,
    valueExtractor: (T) -> Number,
    dateExtractor: (T) -> LocalDate
): String? {
    if (entries.size < 2) return null

    val sorted = entries.sortedBy { dateExtractor(it) }
    val referenceDate = dateExtractor(sorted.first())

    val x = sorted.map { ChronoUnit.DAYS.between(referenceDate, dateExtractor(it)).toDouble() }
    val y = sorted.map { valueExtractor(it).toDouble() }

    val regression = linearRegression(x, y)
    val predictedFirst = regression(x.first())
    val predictedLast = regression(x.last())

    val threshold = 0.02 * predictedFirst

    return when {
        predictedLast > predictedFirst + threshold -> "up"
        predictedLast < predictedFirst - threshold -> "down"
        else -> "stable"
    }
}
