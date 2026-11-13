package com.diabdata.core.database

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.diabdata.core.database.converters.toEntity
import com.diabdata.core.model.Appointment
import com.diabdata.core.model.Hba1c
import com.diabdata.core.model.ImportantDate
import com.diabdata.core.model.MedicalDevice
import com.diabdata.core.model.Treatment
import com.diabdata.core.model.UserDetails
import com.diabdata.core.model.Weight
import com.diabdata.core.model.nowMillis
import com.diabdata.feature.graphs.classes.PlotPoint
import com.diabdata.shared.R
import com.diabdata.shared.utils.dataTypes.AddableType
import com.diabdata.shared.utils.dataTypes.AppointmentType
import com.diabdata.shared.utils.dataTypes.MedicalDeviceInfoType
import com.diabdata.shared.utils.dataTypes.TreatmentType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject
import kotlin.time.Clock
import kotlin.time.Instant

@HiltViewModel
class DataViewModel @Inject constructor(
    val repository: DataRepository,
    private val workManager: WorkManager,
    application: Application
) : AndroidViewModel(application) {
    // Load all data
    val weights: StateFlow<List<Weight>> = repository.getAllWeights()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val hba1cEntries: StateFlow<List<Hba1c>> = repository.getAllHba1c()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val appointments: StateFlow<List<Appointment>> = repository.getAllAppointments()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val treatments: StateFlow<List<Treatment>> = repository.getAllTreatments()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val importantDates: StateFlow<List<ImportantDate>> = repository.getAllImportantDates()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val medicalDevices: StateFlow<List<MedicalDevice>> = repository.getAllDevices()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val userProfile: StateFlow<UserDetails?> = repository.getUserDetails()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Helpers to check if we have Data
    data class DataAvailability(
        val hasAnyData: Boolean,
        val hasWeights: Boolean,
        val hasAppointments: Boolean,
        val hasTreatments: Boolean,
        val hasImportantDates: Boolean,
        val hasHba1c: Boolean,
        val hasDevices: Boolean,
        val hasDiabetesDiagnosisDate: Boolean
    ) {
        companion object {
            val EMPTY = DataAvailability(
                hasAnyData = false,
                hasWeights = false,
                hasAppointments = false,
                hasTreatments = false,
                hasImportantDates = false,
                hasHba1c = false,
                hasDevices = false,
                hasDiabetesDiagnosisDate = false
            )
        }
    }

    val part1 = combine(weights, hba1cEntries, appointments) { w, h, a ->
        Triple(w, h, a)
    }
    val part2 = combine(treatments, importantDates, medicalDevices) { t, d, md ->
        Triple(t, d, md)
    }

    val dataAvailability: StateFlow<DataAvailability> =
        combine(part1, part2, userProfile) { (w, h, a), (t, d, md), profile ->

            val hasDiagnosisDate = profile?.diagnosisDate != null
                    && profile.diabetesType != null

            DataAvailability(
                hasAnyData = w.isNotEmpty() || h.isNotEmpty() || a.isNotEmpty()
                        || t.isNotEmpty() || d.isNotEmpty() || md.isNotEmpty()
                        || hasDiagnosisDate,
                hasWeights = w.isNotEmpty(),
                hasAppointments = a.isNotEmpty(),
                hasTreatments = t.isNotEmpty(),
                hasImportantDates = d.isNotEmpty() || hasDiagnosisDate,
                hasHba1c = h.isNotEmpty(),
                hasDevices = md.isNotEmpty(),
                hasDiabetesDiagnosisDate = hasDiagnosisDate
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DataAvailability.EMPTY)

    // Get recent and upcoming data or plot data
    // HBA1C
    val recentHba1c: StateFlow<List<Hba1c>> =
        repository.getRecentHba1c()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getHba1cPlotData(minDate: LocalDate, maxDate: LocalDate): Flow<List<PlotPoint>> =
        repository.getHba1cPlotData(minDate, maxDate)

    // Weights
    val recentWeights: StateFlow<List<Weight>> =
        repository.getRecentWeights()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getWeightPlotData(minDate: LocalDate, maxDate: LocalDate): Flow<List<PlotPoint>> =
        repository.getWeightPlotData(minDate, maxDate)

    // Appointments
    val upcomingAppointment: StateFlow<List<Appointment>> =
        repository.getUpcomingAppointments()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Expiration dates
    val upcomingExpiringTreatmentDates: StateFlow<List<Treatment>> =
        repository.getUpcomingExpDates()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Insertion functions
    fun addWeight(weight: Weight) {
        viewModelScope.launch {
            repository.insertWeight(weight)
        }
    }

    fun addHba1c(hba1C: Hba1c) {
        viewModelScope.launch {
            repository.insertHba1c(hba1C)
        }
    }

    fun addAppointment(appointment: Appointment) {
        viewModelScope.launch {
            repository.insertAppointment(appointment)
        }
    }

    fun addTreatment(treatment: Treatment) {
        viewModelScope.launch {
            repository.insertTreatment(treatment)
        }
    }

    fun addImportantDate(importantDate: ImportantDate) {
        viewModelScope.launch {
            repository.insertImportantDate(importantDate)
        }
    }

    // Archive function
    fun setArchived(entry: MixedDbEntry, archived: Boolean) {
        viewModelScope.launch {
            when (entry.addableType) {
                AddableType.WEIGHT -> repository.updateWeight(
                    (entry.toEntity() as Weight).copy(
                        isArchived = archived,
                        updatedAt = nowMillis()
                    )

                )

                AddableType.HBA1C -> repository.updateHBA1C(
                    (entry.toEntity() as Hba1c).copy(
                        isArchived = archived,
                        updatedAt = nowMillis()
                    )

                )

                AddableType.APPOINTMENT -> repository.updateAppointment(
                    (entry.toEntity() as Appointment).copy(
                        isArchived = archived,
                        updatedAt = nowMillis()
                    )

                )

                AddableType.TREATMENT -> repository.updateTreatment(
                    (entry.toEntity() as Treatment).copy(
                        isArchived = archived,
                        updatedAt = nowMillis()
                    )

                )

                AddableType.IMPORTANT_DATE -> repository.updateImportantDate(
                    (entry.toEntity() as ImportantDate).copy(
                        isArchived = archived,
                        updatedAt = nowMillis()
                    )

                )

                AddableType.DEVICE -> repository.updateDevice(
                    (entry.toEntity() as MedicalDevice).copy(
                        isArchived = archived,
                        updatedAt = nowMillis()
                    )
                )
            }
        }
    }

    // Deletion functions
    fun deleteEntry(entry: MixedDbEntry) = viewModelScope.launch {
        when (entry.addableType) {
            AddableType.WEIGHT -> repository.deleteWeight(entry.id)
            AddableType.HBA1C -> repository.deleteHba1c(entry.id)
            AddableType.APPOINTMENT -> repository.deleteAppointment(entry.id)
            AddableType.TREATMENT -> repository.deleteTreatment(entry.id)
            AddableType.IMPORTANT_DATE -> repository.deleteImportantDate(entry.id)
            AddableType.DEVICE -> repository.deleteDevice(entry.id)
        }
    }

    // Update function
    suspend fun updateEntry(entry: MixedDbEntry) {
        when (entry.addableType) {
            AddableType.WEIGHT -> repository.updateWeight(entry.toEntity() as Weight)
            AddableType.HBA1C -> repository.updateHBA1C(entry.toEntity() as Hba1c)
            AddableType.APPOINTMENT -> repository.updateAppointment(entry.toEntity() as Appointment)
            AddableType.TREATMENT -> repository.updateTreatment(entry.toEntity() as Treatment)
            AddableType.IMPORTANT_DATE -> repository.updateImportantDate(entry.toEntity() as ImportantDate)
            AddableType.DEVICE -> repository.updateDevice(entry.toEntity() as MedicalDevice)
        }
    }

    // DataViewModel

    fun clearDatabase(context: Context) = viewModelScope.launch {
        withContext(Dispatchers.IO) {
            repository.clearAllDataAndReset()

            // cancel all reminder workers
            workManager.cancelAllWorkByTag("treatments")
            workManager.cancelAllWorkByTag("appointments")

            // Reset user's reminders preferences
            repository.enableAppointmentReminder(false)
            repository.enableExpirationReminder(false)
        }
    }

    // Section for collecting full DB
    sealed class MixedDbEntry {
        abstract val id: Int
        abstract val addableType: AddableType
        abstract val date: LocalDateTime

        abstract val icon: Int
        abstract val isArchived: Boolean
        abstract val createdAt: Instant

        abstract val updatedAt: Instant
        abstract val entryUuid: String

        data class AppointmentEntry(
            override val id: Int,
            override val date: LocalDateTime,
            override val addableType: AddableType = AddableType.APPOINTMENT,
            val doctor: String,
            val type: AppointmentType,
            val notes: String?,
            override val icon: Int,
            override val isArchived: Boolean,
            override val createdAt: Instant,
            override val updatedAt: Instant,
            override val entryUuid: String
        ) : MixedDbEntry()

        data class ImportantDateEntry(
            override val id: Int,
            override val date: LocalDateTime,
            override val addableType: AddableType = AddableType.IMPORTANT_DATE,
            val importantDate: String,
            override val icon: Int,
            override val isArchived: Boolean,
            override val createdAt: Instant,
            override val updatedAt: Instant,
            override val entryUuid: String
        ) : MixedDbEntry()

        data class Hba1cEntry(
            override val id: Int,
            override val date: LocalDateTime,
            override val addableType: AddableType = AddableType.HBA1C,
            val value: Float,
            override val icon: Int,
            override val isArchived: Boolean,
            override val createdAt: Instant,
            override val updatedAt: Instant,
            override val entryUuid: String
        ) : MixedDbEntry()

        data class TreatmentEntry(
            override val id: Int,
            override val date: LocalDateTime,
            override val addableType: AddableType = AddableType.TREATMENT,
            val name: String,
            val treatmentType: TreatmentType,
            override val icon: Int,
            override val isArchived: Boolean,
            override val createdAt: Instant,
            override val updatedAt: Instant,
            override val entryUuid: String
        ) : MixedDbEntry()

        data class WeightEntry(
            override val id: Int,
            override val date: LocalDateTime,
            override val addableType: AddableType = AddableType.WEIGHT,
            val value: Float,
            override val icon: Int,
            override val isArchived: Boolean,
            override val createdAt: Instant,
            override val updatedAt: Instant,
            override val entryUuid: String
        ) : MixedDbEntry()

        data class DeviceEntry(
            override val id: Int,
            override val date: LocalDateTime,
            val lifeSpanEndDate: LocalDate,
            override val addableType: AddableType = AddableType.DEVICE,
            val name: String,
            val deviceType: MedicalDeviceInfoType,
            val batchNumber: String,
            val serialNumber: String,
            val referenceNumber: String,
            val manufacturer: String,
            val lifeSpan: Int,
            val isFaulty: Boolean,
            val isReported: Boolean,
            val isLifeSpanOver: Boolean,
            override val icon: Int,
            override val isArchived: Boolean,
            override val createdAt: Instant,
            override val updatedAt: Instant,
            override val entryUuid: String
        ) : MixedDbEntry()
    }

    fun getIconForMixedEntry(
        addableType: AddableType,
        appointmentType: AppointmentType? = null,
        treatmentType: TreatmentType? = null,
        deviceType: MedicalDeviceInfoType? = null
    ): Int {
        return when (addableType) {
            AddableType.WEIGHT -> AddableType.WEIGHT.iconRes
            AddableType.HBA1C -> AddableType.HBA1C.iconRes
            AddableType.APPOINTMENT -> when (appointmentType) {
                AppointmentType.ANNUAL_CHECKUP -> AppointmentType.ANNUAL_CHECKUP.iconRes
                AppointmentType.APPOINTMENT -> AppointmentType.APPOINTMENT.iconRes
                null -> R.drawable.event_icon_vector
            }

            AddableType.TREATMENT -> when (treatmentType) {
                TreatmentType.FAST_ACTING_INSULIN_CARTRIDGE -> TreatmentType.FAST_ACTING_INSULIN_CARTRIDGE.iconRes
                TreatmentType.FAST_ACTING_INSULIN_SYRINGE -> TreatmentType.FAST_ACTING_INSULIN_SYRINGE.iconRes
                TreatmentType.FAST_ACTING_INSULIN_VIAL -> TreatmentType.FAST_ACTING_INSULIN_VIAL.iconRes
                TreatmentType.SLOW_ACTING_INSULIN_CARTRIDGE -> TreatmentType.SLOW_ACTING_INSULIN_CARTRIDGE.iconRes
                TreatmentType.SLOW_ACTING_INSULIN_SYRINGE -> TreatmentType.SLOW_ACTING_INSULIN_SYRINGE.iconRes
                TreatmentType.SLOW_ACTING_INSULIN_VIAL -> TreatmentType.SLOW_ACTING_INSULIN_VIAL.iconRes
                TreatmentType.B_KETONE_TEST_STRIP -> TreatmentType.B_KETONE_TEST_STRIP.iconRes
                TreatmentType.BLOOD_GLUCOSE_TEST_STRIP -> TreatmentType.BLOOD_GLUCOSE_TEST_STRIP.iconRes
                TreatmentType.GLUCAGON_SYRINGE -> TreatmentType.GLUCAGON_SYRINGE.iconRes
                TreatmentType.GLUCAGON_SPRAY -> TreatmentType.GLUCAGON_SPRAY.iconRes
                TreatmentType.UNKNOWN -> TreatmentType.UNKNOWN.iconRes
                null -> R.drawable.medication_icon_vector
            }

            AddableType.IMPORTANT_DATE -> R.drawable.important_date_icon_vector

            AddableType.DEVICE -> when (deviceType) {
                MedicalDeviceInfoType.WIRELESS_PATCH -> MedicalDeviceInfoType.WIRELESS_PATCH.iconRes
                MedicalDeviceInfoType.WIRED_PATCH -> MedicalDeviceInfoType.WIRED_PATCH.iconRes
                MedicalDeviceInfoType.CONTINUOUS_GLUCOSE_MONITORING_SYSTEM_SENSOR -> MedicalDeviceInfoType.CONTINUOUS_GLUCOSE_MONITORING_SYSTEM_SENSOR.iconRes
                MedicalDeviceInfoType.CONTINUOUS_GLUCOSE_MONITORING_SYSTEM_TRANSMITTER -> MedicalDeviceInfoType.CONTINUOUS_GLUCOSE_MONITORING_SYSTEM_TRANSMITTER.iconRes
                MedicalDeviceInfoType.WIRED_PUMP -> MedicalDeviceInfoType.WIRED_PUMP.iconRes
                MedicalDeviceInfoType.WIRELESS_PATCH_REMOTE -> MedicalDeviceInfoType.WIRELESS_PATCH_REMOTE.iconRes
                else -> R.drawable.devices_icon_vector
            }
        }
    }

    val allMixedEntries: Flow<List<MixedDbEntry>> = combine(
        part1,
        part2
    ) { (w, h, a), (t, d, md) ->
        buildList<MixedDbEntry> {
            a.forEach {
                add(
                    MixedDbEntry.AppointmentEntry(
                        id = it.id,
                        date = it.date,
                        doctor = it.doctor,
                        type = it.type,
                        notes = it.notes,
                        icon = getIconForMixedEntry(
                            AddableType.APPOINTMENT,
                            appointmentType = it.type
                        ),
                        isArchived = it.isArchived,
                        createdAt = it.createdAt,
                        updatedAt = it.updatedAt,
                        entryUuid = it.entryUuid
                    )
                )
            }

            d.forEach {
                add(
                    MixedDbEntry.ImportantDateEntry(
                        id = it.id,
                        date = it.date.atStartOfDay(),
                        importantDate = it.importantDate,
                        icon = getIconForMixedEntry(AddableType.IMPORTANT_DATE),
                        isArchived = it.isArchived,
                        createdAt = it.createdAt,
                        updatedAt = it.updatedAt,
                        entryUuid = it.entryUuid
                    )
                )
            }

            h.forEach {
                add(
                    MixedDbEntry.Hba1cEntry(
                        id = it.id,
                        date = it.date.atStartOfDay(),
                        value = it.value,
                        icon = getIconForMixedEntry(AddableType.HBA1C),
                        isArchived = it.isArchived,
                        createdAt = it.createdAt,
                        updatedAt = it.updatedAt,
                        entryUuid = it.entryUuid
                    )
                )
            }

            t.forEach {
                add(
                    MixedDbEntry.TreatmentEntry(
                        id = it.id,
                        date = it.expirationDate.atStartOfDay(),
                        name = it.name,
                        treatmentType = it.type,
                        icon = getIconForMixedEntry(AddableType.TREATMENT, treatmentType = it.type),
                        isArchived = it.isArchived,
                        createdAt = it.createdAt,
                        updatedAt = it.updatedAt,
                        entryUuid = it.entryUuid
                    )
                )
            }

            w.forEach {
                add(
                    MixedDbEntry.WeightEntry(
                        id = it.id,
                        date = it.date.atStartOfDay(),
                        value = it.value,
                        icon = getIconForMixedEntry(AddableType.WEIGHT),
                        isArchived = it.isArchived,
                        createdAt = it.createdAt,
                        updatedAt = it.updatedAt,
                        entryUuid = it.entryUuid
                    )
                )
            }

            md.forEach {
                add(
                    MixedDbEntry.DeviceEntry(
                        id = it.id,
                        date = it.date.atStartOfDay(),
                        lifeSpanEndDate = it.lifeSpanEndDate,
                        addableType = AddableType.DEVICE,
                        name = it.name,
                        deviceType = it.deviceType,
                        batchNumber = it.batchNumber,
                        serialNumber = it.serialNumber ?: "",
                        referenceNumber = it.referenceNumber ?: "",
                        manufacturer = it.manufacturer ?: "",
                        lifeSpan = it.lifeSpan,
                        isFaulty = it.isFaulty,
                        isReported = it.isReported,
                        isLifeSpanOver = it.isLifeSpanOver,
                        icon = getIconForMixedEntry(AddableType.DEVICE, deviceType = it.deviceType),
                        isArchived = it.isArchived,
                        createdAt = it.createdAt,
                        updatedAt = it.updatedAt,
                        entryUuid = it.entryUuid
                    )
                )
            }
        }
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}