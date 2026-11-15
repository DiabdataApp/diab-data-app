package com.diabdata.core.database.converters

import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.time.Instant

class DateConverters {

    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    // LocalDate
    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? = date?.format(formatter)

    @TypeConverter
    fun toLocalDate(dateString: String?): LocalDate? = if (dateString.isNullOrBlank()) {
        LocalDate.now()
    } else {
        LocalDate.parse(dateString, formatter)
    }

    // LocalDateTime
    @TypeConverter
    fun fromLocalDateTime(dateTime: LocalDateTime?): String? = dateTime?.format(dateTimeFormatter)

    @TypeConverter
    fun toLocalDateTime(dateTimeString: String?): LocalDateTime? =
        if (dateTimeString.isNullOrBlank()) {
            LocalDateTime.now()
        } else {
            LocalDateTime.parse(dateTimeString, dateTimeFormatter)
        }

    // Instant
    @TypeConverter
    fun fromInstant(dateInstant: Instant?): Long? = dateInstant?.toEpochMilliseconds()

    @TypeConverter
    fun toInstant(longInstant: Long?): Instant? = longInstant?.let {
        Instant.fromEpochMilliseconds(it)
    }
}
