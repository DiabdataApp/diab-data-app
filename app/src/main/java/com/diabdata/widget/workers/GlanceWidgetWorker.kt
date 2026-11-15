package com.diabdata.widget.workers

import android.content.Context
import android.util.Log
import androidx.glance.appwidget.updateAll
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.diabdata.core.database.DiabDataDatabase
import com.diabdata.widget.WidgetAppointment
import com.diabdata.widget.WidgetDevice
import com.diabdata.widget.glanceWidget.GlanceWidget
import com.diabdata.widget.widgetDataStore
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.firstOrNull

@HiltWorker
class GlanceWidgetWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParameters: WorkerParameters,
    private val db: DiabDataDatabase
) : CoroutineWorker(context, workerParameters) {

    override suspend fun doWork(): Result {
        val today = LocalDateTime.now()
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE

        val devicesFlow =
            db.medicalDevicesDao().getAllCurrentConsumableMedicalDevices(today.toLocalDate())
        val devices = devicesFlow.firstOrNull()?.map {
            Log.i(
                "GlanceWidgetWorker",
                "Processing devices: ${it.name}, end=${it.lifeSpanEndDate}, today=$today, daysLeft=${
                    getDaysLeft(
                        it.lifeSpanEndDate
                    )
                }"
            )

            WidgetDevice(
                name = it.name,
                type = it.deviceType.toString(),
                daysLeft = getDaysLeft(it.lifeSpanEndDate).toInt(),
                lifespanProgression = getLifeSpanProgress(it.date, it.lifeSpanEndDate),
                lifeSpanEndDate = it.lifeSpanEndDate.format(formatter)
            )
        } ?: emptyList()

        Log.i("GlanceWidgetWorker", "Updating DataStore with devices: $devices")

        val apptsFlow = db.appointmentDao().getUpcomingAppointmentsFlow(today)
        val nextAppt = apptsFlow.firstOrNull()?.firstOrNull()

        context.widgetDataStore.updateData { current ->
            current.copy(
                devices = devices,
                nextAppointment = WidgetAppointment(
                    date = nextAppt?.date?.toLocalDate()?.format(formatter) ?: "",
                    doctor = nextAppt?.doctor ?: "",
                    type = nextAppt?.type?.toString() ?: ""
                ),
                lastUpdated = System.currentTimeMillis()
            )
        }

        GlanceWidget().updateAll(context)

        return Result.success()
    }
}

fun getLifeSpanProgress(startDate: LocalDate, endDate: LocalDate): Int {
    require(startDate < endDate) { "Start date cannot be set after endDate" }

    val today = LocalDate.now()

    val totalDays = ChronoUnit.DAYS.between(startDate, endDate).toDouble().coerceAtLeast(1.0)
    val elapsedDays = ChronoUnit.DAYS.between(startDate, today).toDouble().coerceAtLeast(0.0)

    val progress = (elapsedDays / totalDays * 100).coerceIn(0.0, 100.0)

    return progress.roundToInt()
}

fun getDaysLeft(endDate: LocalDate): Long {
    val today = LocalDate.now()

    val daysLeft = ChronoUnit.DAYS.between(today, endDate)

    return daysLeft
}
