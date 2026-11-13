package com.diabdata.core.model

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.time.toJavaInstant

val UNKNOWN_TIME_DEFAULT: Instant = Instant.fromEpochSeconds(0)

fun nowMillis(): Instant {
    val now = Clock.System.now()
    return Instant.fromEpochMilliseconds(now.toEpochMilliseconds())
}

fun nowMillisInLong(): Long {
    val now = Clock.System.now()
    return now.toEpochMilliseconds()
}

fun Instant.toLocalDate(): LocalDate {
    return LocalDate.from(this.toJavaInstant().atZone(ZoneId.systemDefault()))
}