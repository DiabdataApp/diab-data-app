package com.diabdata.wear.complications.expiringDevices

import android.content.ComponentName
import androidx.core.content.edit
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceUpdateRequester
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService

class ExpiringDevicesDataSyncListenerService : WearableListenerService() {

    override fun onCreate() {
        super.onCreate()
    }

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        dataEvents.forEach { event ->
            if (event.type == DataEvent.TYPE_CHANGED &&
                event.dataItem.uri.path == "/complications/medical_device_expiry"
            ) {
                val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                val date = dataMap.getString("nextDeviceExpiry")
                val lifespan = dataMap.getString("nextDeviceLifespan")
                val iconRes = dataMap.getString("deviceIconRes")
                dataMap.getLong("timestamp")

                if (date != null && iconRes != null && lifespan != null) {
                    storeDataLocally(date, lifespan, iconRes)
                    requestComplicationUpdate()
                }
            }
        }
    }

    private fun storeDataLocally(date: String, lifespan: String, iconRes: String) {
        val prefs = getSharedPreferences("complications_medical_device_expiry", MODE_PRIVATE)
        prefs.edit {
            putString("nextDeviceExpiry", date)
            putString("deviceIconRes", iconRes)
            putString("nextDeviceLifespan", lifespan)
            putLong("timestamp", System.currentTimeMillis())
        }
    }

    private fun requestComplicationUpdate() {
        val componentName =
            ComponentName(this, ExpiringMedicalDevicesComplicationService::class.java)
        val requester = ComplicationDataSourceUpdateRequester.create(this, componentName)
        requester.requestUpdateAll()
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
