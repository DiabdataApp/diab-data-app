package com.diabdata.wear.complications.expiringTreatments

import android.content.ComponentName
import androidx.core.content.edit
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceUpdateRequester
import com.diabdata.wear.complications.expiringDevices.ExpiringMedicalDevicesComplicationService
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService

class ExpiringTreatmentsDataSyncListenerService : WearableListenerService() {
    override fun onCreate() {
        super.onCreate()
    }

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        dataEvents.forEach { event ->
            if (event.type == DataEvent.TYPE_CHANGED &&
                event.dataItem.uri.path == "/complications/treatment_expiry"
            ) {
                val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                val nextTreatmentExpiryDays = dataMap.getInt("nextTreatmentExpiryDays", -1)
                val elapsedTreatmentConservation =
                    dataMap.getInt("elapsedTreatmentConservation", -1)
                val treatmentIconRes = dataMap.getString("treatmentIconRes")

                if (treatmentIconRes != null) {
                    storeDataLocally(
                        nextTreatmentExpiryDays,
                        elapsedTreatmentConservation,
                        treatmentIconRes
                    )
                    requestComplicationUpdate()
                }
            }
        }
    }

    private fun storeDataLocally(expiryDays: Int, elapsedConservation: Int, iconRes: String) {
        val prefs = getSharedPreferences("complications_treatment_expiry", MODE_PRIVATE)
        prefs.edit {
            putString("nextTreatmentExpiry", expiryDays.toString())
            putString("elapsedTreatmentConservation", elapsedConservation.toString())
            putString("treatmentIconRes", iconRes)
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
