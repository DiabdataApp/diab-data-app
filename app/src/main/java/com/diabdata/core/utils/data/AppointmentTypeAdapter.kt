package com.diabdata.core.utils.data

import com.diabdata.shared.utils.dataTypes.AppointmentType
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type

class AppointmentTypeAdapter :
    JsonSerializer<AppointmentType>,
    JsonDeserializer<AppointmentType> {

    override fun serialize(
        src: AppointmentType?,
        typeOfSrc: Type?,
        context: JsonSerializationContext?
    ): JsonElement = JsonPrimitive(src?.name?.lowercase())

    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): AppointmentType? = json?.asString?.let {
        when (it.lowercase()) {
            "annual_checkup" -> AppointmentType.ANNUAL_CHECKUP
            "appointment" -> AppointmentType.APPOINTMENT
            else -> null
        }
    }
}
