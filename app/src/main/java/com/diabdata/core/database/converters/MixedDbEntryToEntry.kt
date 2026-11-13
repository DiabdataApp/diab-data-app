package com.diabdata.core.database.converters

import com.diabdata.core.database.DataViewModel
import com.diabdata.core.model.Appointment
import com.diabdata.core.model.Hba1c
import com.diabdata.core.model.ImportantDate
import com.diabdata.core.model.MedicalDevice
import com.diabdata.core.model.Treatment
import com.diabdata.core.model.Weight

fun DataViewModel.MixedDbEntry.toEntity(): Any = when (this) {
    is DataViewModel.MixedDbEntry.AppointmentEntry -> Appointment(
        id = id,
        date = date,
        doctor = doctor,
        type = type,
        createdAt = createdAt,
        isArchived = isArchived,
        notes = notes,
        updatedAt = updatedAt,
        entryUuid = entryUuid
    )

    is DataViewModel.MixedDbEntry.ImportantDateEntry -> ImportantDate(
        id = id,
        date = date.toLocalDate(),
        createdAt = createdAt,
        isArchived = isArchived,
        importantDate = importantDate,
        updatedAt = updatedAt,
        entryUuid = entryUuid
    )

    is DataViewModel.MixedDbEntry.Hba1cEntry -> Hba1c(
        id = id,
        date = date.toLocalDate(),
        createdAt = createdAt,
        isArchived = isArchived,
        value = value,
        updatedAt = updatedAt,
        entryUuid = entryUuid
    )

    is DataViewModel.MixedDbEntry.TreatmentEntry -> Treatment(
        id = id,
        expirationDate = date.toLocalDate(),
        name = name,
        createdAt = createdAt,
        isArchived = isArchived,
        type = treatmentType,
        updatedAt = updatedAt,
        entryUuid = entryUuid
    )

    is DataViewModel.MixedDbEntry.WeightEntry -> Weight(
        id = id,
        date = date.toLocalDate(),
        createdAt = createdAt,
        isArchived = isArchived,
        value = value,
        updatedAt = updatedAt,
        entryUuid = entryUuid
    )

    is DataViewModel.MixedDbEntry.DeviceEntry -> MedicalDevice(
        id = id,
        date = date.toLocalDate(),
        lifeSpanEndDate = lifeSpanEndDate,
        name = name,
        batchNumber = batchNumber,
        serialNumber = serialNumber,
        referenceNumber = referenceNumber,
        manufacturer = manufacturer,
        deviceType = deviceType,
        createdAt = createdAt,
        isArchived = isArchived,
        updatedAt = updatedAt,
        lifeSpan = lifeSpan,
        isFaulty = isFaulty,
        isReported = isReported,
        isLifeSpanOver = isLifeSpanOver,
        entryUuid = entryUuid
    )
}