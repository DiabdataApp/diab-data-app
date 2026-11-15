package com.diabdata.feature.settings

import android.content.Context
import android.util.Log
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.diabdata.core.database.DataRepository
import com.diabdata.core.database.DiabDataDatabase
import com.diabdata.core.model.UserPreferences
import com.diabdata.feature.dataMatrixScanner.utils.MedicalDevicesInitializer
import com.diabdata.feature.dataMatrixScanner.utils.MedicationInitializer
import com.diabdata.workers.reminders.scheduleAppointmentReminders
import com.diabdata.workers.reminders.scheduleMedicationExpirationReminders
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val workManager: WorkManager,
    private val repository: DataRepository,
    private val db: DiabDataDatabase
) : ViewModel() {
    val preferences: StateFlow<UserPreferences?> = repository
        .getUserPreferences()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun forceRebuildMedicalDevices() {
        viewModelScope.launch(Dispatchers.IO) {
            MedicalDevicesInitializer(context, db).initialize()
        }
    }

    fun forceRebuildMedications() {
        viewModelScope.launch(Dispatchers.IO) {
            MedicationInitializer(context, db).initialize()
        }
    }

    fun onExpirationReminderSwitch(enabled: Boolean) {
        viewModelScope.launch {
            repository.enableExpirationReminder(enabled)
            if (enabled) {
                scheduleMedicationExpirationReminders(context, repository)
            } else {
                workManager.cancelAllWorkByTag("treatments")
            }
        }
    }

    fun onAppointmentSwitch(enabled: Boolean) {
        viewModelScope.launch {
            repository.enableAppointmentReminder(enabled)
            if (enabled) {
                scheduleAppointmentReminders(context, repository)
            } else {
                workManager.cancelAllWorkByTag("appointments")
            }
        }
    }

    init {
        migrateSharedPrefsToRoom()
    }

    private fun migrateSharedPrefsToRoom() {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        if (prefs.contains("expiration_reminder") || prefs.contains("appointment_reminder")) {
            viewModelScope.launch {
                val expirationEnabled = prefs.getBoolean("expiration_reminder", false)
                val appointmentEnabled = prefs.getBoolean("appointment_reminder", false)

                repository.enableExpirationReminder(expirationEnabled)
                repository.enableAppointmentReminder(appointmentEnabled)

                prefs.edit {
                    remove("expiration_reminder")
                    remove("appointment_reminder")
                }

                Log.d(
                    "Settings",
                    "Migrated SharedPrefs to Room: expiration=$expirationEnabled, appointment=$appointmentEnabled"
                )
            }
        }
    }
}
