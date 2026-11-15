package com.diabdata.widget

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.diabdata.core.utils.data.GsonFactory
import java.io.InputStream
import java.io.OutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object WidgetStateSerializer : Serializer<WidgetState> {
    override val defaultValue: WidgetState = WidgetState()
    val gson = GsonFactory.create()

    override suspend fun readFrom(input: InputStream): WidgetState {
        val json = input.bufferedReader().use { it.readText() }

        try {
            return gson.fromJson(json, WidgetState::class.java)
        } catch (exception: Exception) {
            throw CorruptionException("Cannot read widget state JSON.", exception)
        }
    }

    override suspend fun writeTo(t: WidgetState, output: OutputStream) {
        val data = gson.toJson(t, WidgetState::class.java).toByteArray()
        withContext(Dispatchers.IO) {
            output.write(data)
        }
    }
}
