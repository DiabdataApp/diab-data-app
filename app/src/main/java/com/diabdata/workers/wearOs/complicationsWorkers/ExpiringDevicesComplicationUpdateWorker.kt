package com.diabdata.workers.wearOs.complicationsWorkers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.diabdata.feature.devices.data.MedicalDeviceDao
import com.diabdata.shared.utils.dateUtils.getNumberOfDaysUntil
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.LocalDate
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.tasks.await

@HiltWorker
class ExpiringDevicesComplicationUpdateWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val dao: MedicalDeviceDao
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        try {
            val nodeClient = Wearable.getNodeClient(applicationContext)
            nodeClient.connectedNodes.await()

            val today = LocalDate.now()

            val upcomingDevices = dao.getAllCurrentConsumableMedicalDevices(today).firstOrNull()
            val nextDevice = upcomingDevices?.minByOrNull { it.lifeSpanEndDate }

            val nextDeviceIcon = nextDevice?.deviceType.toString()

            val nextDeviceLifespan = nextDevice?.lifeSpan

            val numberOfDays = nextDevice?.lifeSpanEndDate?.getNumberOfDaysUntil()

            val dataMapRequest = PutDataMapRequest.create("/complications/medical_device_expiry")
            dataMapRequest.dataMap.putString("nextDeviceExpiry", numberOfDays.toString())
            dataMapRequest.dataMap.putString("nextDeviceLifespan", nextDeviceLifespan.toString())
            dataMapRequest.dataMap.putString("deviceIconRes", nextDeviceIcon)
            dataMapRequest.dataMap.putLong("timestamp", System.currentTimeMillis())

            val putDataRequest = dataMapRequest.asPutDataRequest()
            putDataRequest.setUrgent()

            Wearable.getDataClient(applicationContext)
                .putDataItem(putDataRequest)
                .addOnSuccessListener {
                }
                .addOnFailureListener { e ->
                }
                .await()

            return Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            return Result.retry()
        }
    }
}
