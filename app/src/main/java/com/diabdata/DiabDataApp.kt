package com.diabdata

import android.app.Application
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.work.Configuration
import androidx.work.WorkManager
import com.diabdata.core.database.DataRepository
import com.diabdata.workers.main.WorkersInitializer
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@HiltAndroidApp
class DiabDataApp :
    Application(),
    Configuration.Provider {
    @Inject lateinit var repository: DataRepository

    @Inject lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()

        WorkManager.getInstance(this).pruneWork()
        WorkersInitializer.enqueuePeriodicWorkers(this)
        WorkersInitializer.enqueueOneTimeWorkers(this)

        ProcessLifecycleOwner.get().lifecycleScope.launch {
            combine(
                repository.getAllCurrentConsumableDevices(),
                repository.getUpcomingAppointments(),
                repository.getUpcomingExpDates()
            ) { devices, appointments, treatments ->
                Triple(devices, appointments, treatments)
            }
                .distinctUntilChanged()
                .collect {
                    Log.d("DiabDataApp", "Données modifiées, mise à jour des workers")
                    WorkersInitializer.enqueueOneTimeWorkers(this@DiabDataApp)
                }
        }
    }
}
