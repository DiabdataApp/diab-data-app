package com.diabdata.core.utils.data

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlin.time.Instant

class LocalDateAdapter : JsonSerializer<LocalDate>, JsonDeserializer<LocalDate> {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE

    override fun serialize(
        src: LocalDate?, typeOfSrc: Type?, context: JsonSerializationContext?
    ): JsonElement {
        return JsonPrimitive(src?.format(formatter))
    }

    override fun deserialize(
        json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?
    ): LocalDate? {
        return json?.asString?.let { LocalDate.parse(it, formatter) }
    }
}

class LocalDateTimeAdapter : JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    override fun serialize(
        src: LocalDateTime?, typeOfSrc: Type?, context: JsonSerializationContext?
    ): JsonElement {
        return JsonPrimitive(src?.format(formatter))
    }

    override fun deserialize(
        json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?
    ): LocalDateTime? {
        return json?.asString?.let { LocalDateTime.parse(it, formatter) }
    }
}

class InstantAdapter : JsonSerializer<Instant>, JsonDeserializer<Instant> {
    /**
     * Converts a legacy backup date ("2026-01-15") to an [Instant] at midnight, local time.
     *
     * Must follow the same rule as [com.diabdata.core.database.migrations.MIGRATION_24_25]:
     * otherwise an entry migrated in the database and the same entry in an old backup would get
     * different `updatedAt` values, and the import conflict resolver would pick the wrong one.
     */
    private fun legacyDateToInstant(str: String): Instant {
        val millis = LocalDate.parse(str)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant().toEpochMilli()

        return Instant.fromEpochMilliseconds(millis)
    }

    override fun serialize(src: Instant?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        return JsonPrimitive(src?.toEpochMilliseconds())
    }

    override fun deserialize(
        json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?
    ): Instant? {
        val primitive = json as? JsonPrimitive

        return when {
            primitive?.isNumber == true -> Instant.fromEpochMilliseconds(primitive.asLong)
            primitive?.isString == true -> legacyDateToInstant(primitive.asString)
            else -> throw JsonParseException("Unsupported date format: $json")
        }
    }
}