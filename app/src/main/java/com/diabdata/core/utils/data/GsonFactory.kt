package com.diabdata.core.utils.data

import com.diabdata.shared.utils.dataTypes.AppointmentType
import com.diabdata.shared.utils.dataTypes.BackupFrequency
import com.diabdata.shared.utils.dataTypes.BloodType
import com.diabdata.shared.utils.dataTypes.DiabetesType
import com.diabdata.shared.utils.dataTypes.Gender
import com.diabdata.shared.utils.dataTypes.GlucoseUnit
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.time.Instant

object GsonFactory {
    fun create(prettyPrint: Boolean = false): Gson {
        return GsonBuilder()
            .registerTypeAdapter(LocalDate::class.java, LocalDateAdapter())
            .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeAdapter())
            .registerTypeAdapter(Instant::class.java, InstantAdapter())
            .registerTypeAdapter(AppointmentType::class.java, AppointmentTypeAdapter())
            .registerTypeAdapter(DiabetesType::class.java, EnumTypeAdapter(DiabetesType::class.java))
            .registerTypeAdapter(Gender::class.java, EnumTypeAdapter(Gender::class.java))
            .registerTypeAdapter(BloodType::class.java, EnumTypeAdapter(BloodType::class.java))
            .registerTypeAdapter(GlucoseUnit::class.java, EnumTypeAdapter(GlucoseUnit::class.java))
            .registerTypeAdapter(BackupFrequency::class.java, EnumTypeAdapter(BackupFrequency::class.java))
            .apply { if (prettyPrint) setPrettyPrinting() }
            .create()
    }
}