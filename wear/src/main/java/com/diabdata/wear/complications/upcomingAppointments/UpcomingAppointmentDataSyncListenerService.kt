package com.diabdata.wear.complications.upcomingAppointments

import android.content.ComponentName
import androidx.core.content.edit
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceUpdateRequester
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService

class UpcomingAppointmentDataSyncListenerService : WearableListenerService() {

    override fun onCreate() {
        super.onCreate()
    }

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        dataEvents.forEach { event ->
            if (event.type == DataEvent.TYPE_CHANGED &&
                event.dataItem.uri.path == "/complications/upcoming_appointment"
            ) {
                val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap

                val hasAppointment = dataMap.getBoolean("hasAppointment", false)
                dataMap.getLong("timestamp")

                if (hasAppointment) {
                    val daysUntil = dataMap.getInt("nextAppointment", -1)
                    val doctor = dataMap.getString("doctor")
                    val appointmentType = dataMap.getString("appointmentType")

                    if (daysUntil >= 0 && appointmentType != null && doctor != null) {
                        storeDataLocally(true, daysUntil, doctor, appointmentType)
                    }
                } else {
                    storeDataLocally(false, -1, "", "APPOINTMENT")
                }

                requestComplicationUpdate()
            }
        }
    }

    private fun storeDataLocally(
        hasAppointment: Boolean,
        daysUntil: Int,
        doctor: String,
        appointmentType: String
    ) {
        val prefs = getSharedPreferences("complications_upcoming_appointment", MODE_PRIVATE)
        prefs.edit {
            putBoolean("hasAppointment", hasAppointment)
            if (hasAppointment) {
                putInt("nextAppointment", daysUntil)
                putString("doctor", doctor)
                putString("appointmentType", appointmentType)
            }
            putLong("timestamp", System.currentTimeMillis())
        }
    }

    private fun requestComplicationUpdate() {
        val componentName = ComponentName(this, UpcomingAppointmentComplicationService::class.java)
        val requester = ComplicationDataSourceUpdateRequester.create(this, componentName)
        requester.requestUpdateAll()
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
