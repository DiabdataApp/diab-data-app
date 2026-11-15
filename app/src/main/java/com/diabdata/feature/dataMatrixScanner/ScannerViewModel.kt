package com.diabdata.feature.dataMatrixScanner

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.diabdata.core.database.DataRepository
import com.diabdata.core.model.MedicalDevice
import com.diabdata.core.model.MedicalDeviceInfoEntity
import com.diabdata.core.model.Medication
import com.diabdata.core.model.Treatment
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ScannerViewModel @Inject constructor(val repository: DataRepository) : ViewModel() {
    // Section for scanned medication
    var prefilledTreatment: Treatment? by mutableStateOf(null)
    fun updatePrefilledTreatment(t: Treatment?) {
        prefilledTreatment = t
    }

    suspend fun getMedicationByGtin(gtin: String): Medication? =
        repository.findMedicationByCode(gtin)

    // Section for scanned medical devices
    var prefilledMedicalDevice: MedicalDevice? by mutableStateOf(null)

    fun updatePrefilledMedicalDevice(m: MedicalDevice?) {
        prefilledMedicalDevice = m
    }

    suspend fun getMedicalDeviceByCode(code: String): MedicalDeviceInfoEntity? =
        repository.findMedicalDeviceByCode(code)
}
