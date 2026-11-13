package com.diabdata.workers.wearOs.tilesWorkers

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.diabdata.core.database.DiabDataDatabase
import com.diabdata.core.model.toLocalDate
import com.diabdata.shared.utils.dateUtils.getNumberOfDaysUntil
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import com.google.gson.Gson
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime

@HiltWorker
class GlanceTileUpdateWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val database: DiabDataDatabase
) : CoroutineWorker(context, params) {

    private val gson = Gson()

    override suspend fun doWork(): Result {
        return try {
            // Vérifier la connexion
            val nodeClient = Wearable.getNodeClient(applicationContext)
            val nodes = nodeClient.connectedNodes.await()
            if (nodes.isEmpty()) {
                return Result.failure()
            }

            val tileData = collectAllData()

            val jsonData = gson.toJson(tileData)

            sendToWatch(jsonData)

            Result.success()
        } catch (e: Exception) {
            Log.e("TileDataSync", "Erreur sync", e)
            Result.retry()
        }
    }

    private suspend fun collectAllData(): TileData = withContext(Dispatchers.IO) {
        val today = LocalDateTime.now()

        val deviceInfo = getDeviceInfo(today.toLocalDate())

        val treatmentInfo = getTreatmentInfo(today.toLocalDate())

        val appointmentInfo = getAppointmentInfo(today)

        TileData(
            device = deviceInfo,
            treatment = treatmentInfo,
            appointment = appointmentInfo
        )
    }

    private suspend fun getDeviceInfo(today: LocalDate): DeviceInfo? {
        val dao = database.medicalDevicesDao()
        val upcomingDevices = dao.getAllCurrentConsumableMedicalDevices(today).firstOrNull()
        val nextDevice = upcomingDevices?.minByOrNull { it.lifeSpanEndDate }

        return nextDevice?.let {
            DeviceInfo(
                type = it.deviceType.toString(),
                daysUntilExpiry = it.lifeSpanEndDate.getNumberOfDaysUntil(),
                lifespan = it.lifeSpan.toString()
            )
        }
    }

    private suspend fun getTreatmentInfo(today: LocalDate): TreatmentInfo? {
        val dao = database.treatmentDao()
        val upcomingTreatments = dao.getUpcomingExpirationDatesFlow(today).firstOrNull()
        val nextTreatment = upcomingTreatments?.minByOrNull { it.expirationDate }

        return nextTreatment?.let {
            TreatmentInfo(
                type = it.type.toString(),
                daysUntilExpiry = it.expirationDate.getNumberOfDaysUntil(),
                daysSinceCreation = it.createdAt.toLocalDate().getNumberOfDaysUntil()
            )
        }
    }

    private suspend fun getAppointmentInfo(today: LocalDateTime): AppointmentInfo? {
        val dao = database.appointmentDao()
        val upcomingAppointments = dao.getUpcomingAppointmentsFlow(today).firstOrNull()
        val nextAppointment = upcomingAppointments?.minByOrNull { it.date }

        return nextAppointment?.let {
            AppointmentInfo(
                type = it.type.toString(),
                doctor = it.doctor,
                daysUntil = it.date.getNumberOfDaysUntil()
            )
        }
    }

    private suspend fun sendToWatch(jsonData: String) {
        val dataMapRequest = PutDataMapRequest.create("/diabdata/tile/glance_tile").apply {
            dataMap.putString("json_data", jsonData)
            dataMap.putLong("timestamp", System.currentTimeMillis())
            dataMap.putInt("updateCount", (System.currentTimeMillis() % Int.MAX_VALUE).toInt())
        }

        val putDataRequest = dataMapRequest.asPutDataRequest().setUrgent()

        Wearable.getDataClient(applicationContext)
            .putDataItem(putDataRequest)
            .await()
    }
}

data class TileData(
    val device: DeviceInfo?,
    val treatment: TreatmentInfo?,
    val appointment: AppointmentInfo?,
    val timestamp: Long = System.currentTimeMillis()
)

data class DeviceInfo(
    val type: String,
    val daysUntilExpiry: Int,
    val lifespan: String
)

data class TreatmentInfo(
    val type: String,
    val daysUntilExpiry: Int,
    val daysSinceCreation: Int
)

data class AppointmentInfo(
    val type: String,
    val doctor: String,
    val daysUntil: Int
)