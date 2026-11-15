package com.diabdata.core.utils.data

import android.util.Log
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter

class EnumTypeAdapter<T : Enum<T>>(private val enumClass: Class<T>) : TypeAdapter<T?>() {

    override fun write(out: JsonWriter, value: T?) {
        if (value == null) {
            out.nullValue()
        } else {
            out.value(value.name)
        }
    }

    override fun read(input: JsonReader): T? {
        if (input.peek() == JsonToken.NULL) {
            input.nextNull()
            return null
        }
        val name = input.nextString()
        return try {
            java.lang.Enum.valueOf(enumClass, name)
        } catch (e: IllegalArgumentException) {
            Log.w("EnumTypeAdapter", "Unknown enum value '$name' for ${enumClass.simpleName}")
            null
        }
    }
}
