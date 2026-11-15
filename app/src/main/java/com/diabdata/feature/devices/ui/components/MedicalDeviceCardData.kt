package com.diabdata.feature.devices.ui.components

import androidx.compose.ui.graphics.Color
import com.diabdata.core.model.MedicalDevice
import com.diabdata.shared.utils.dataTypes.AddableType
import java.time.LocalDate

data class MedicalDeviceCardData(
    val device: MedicalDevice,
    val titleText: String,
    val textColor: Color,
    val addableType: AddableType,
    val isLifeSpanOver: Boolean,
    val iconRes: Int,
    val date: LocalDate,
    val lifeSpanEndDate: LocalDate,
    val lifeSpan: Int,
    val batchNumber: String
)
