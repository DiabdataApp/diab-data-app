package com.diabdata.feature.devices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diabdata.core.database.DataRepository
import com.diabdata.core.model.MedicalDevice
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class DevicesViewModel @Inject constructor(val repository: DataRepository) : ViewModel() {
    val currentConsumableDevices: StateFlow<List<MedicalDevice>> =
        repository.getAllCurrentConsumableDevices()
            .stateIn(viewModelScope, SharingStarted.Companion.WhileSubscribed(5000), emptyList())

    val nonConsumableDevices: StateFlow<List<MedicalDevice>> =
        repository.getAllNonConsumableDevices()
            .stateIn(viewModelScope, SharingStarted.Companion.WhileSubscribed(5000), emptyList())

    val consumableDevices: StateFlow<List<MedicalDevice>> =
        repository.getAllConsumableDevices()
            .stateIn(viewModelScope, SharingStarted.Companion.WhileSubscribed(5000), emptyList())

    val faultyDevices: StateFlow<List<MedicalDevice>> =
        repository.getAllUnreportedFaultyDevices()
            .stateIn(viewModelScope, SharingStarted.Companion.WhileSubscribed(5000), emptyList())

    val reportedFaultyDevices: StateFlow<List<MedicalDevice>> =
        repository.getAllReportedFaultyDevices()
            .stateIn(viewModelScope, SharingStarted.Companion.WhileSubscribed(5000), emptyList())

    val faultyBatchNumbersTableData: StateFlow<List<List<String>>> =
        repository.getAllFaultyBatchNumbers()
            .map { list ->
                list.map { entry ->
                    listOf(entry.batchNumber, entry.count.toString())
                }
            }
            .stateIn(viewModelScope, SharingStarted.Companion.WhileSubscribed(5000), emptyList())

    fun insertDevice(device: MedicalDevice) {
        viewModelScope.launch {
            repository.insertDevice(device)
        }
    }

    suspend fun updateDevice(device: MedicalDevice) = repository.updateDevice(device)

    fun getAllFaultyDevicesExpiredToday() = repository.getAllFaultyDevicesExpiredToday()

    suspend fun setDevicesLifespanOver(devices: List<MedicalDevice>, isOver: Boolean = true) {
        for (device in devices) {
            repository.updateDevice(device.copy(isLifeSpanOver = isOver))
        }
    }
}
