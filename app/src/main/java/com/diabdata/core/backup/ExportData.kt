package com.diabdata.core.backup

import com.diabdata.core.model.Appointment
import com.diabdata.core.model.Hba1c
import com.diabdata.core.model.ImportantDate
import com.diabdata.core.model.MedicalDevice
import com.diabdata.core.model.Treatment
import com.diabdata.core.model.UserDetails
import com.diabdata.core.model.UserPreferences
import com.diabdata.core.model.Weight

data class ExportData(
    val weights: List<Weight> = emptyList(),
    val hba1c: List<Hba1c> = emptyList(),
    val appointments: List<Appointment> = emptyList(),
    val treatments: List<Treatment> = emptyList(),
    val importantDates: List<ImportantDate> = emptyList(),
    val devices: List<MedicalDevice> = emptyList(),
    val userDetails: UserDetails? = null,
    val userPreferences: UserPreferences? = null
)
