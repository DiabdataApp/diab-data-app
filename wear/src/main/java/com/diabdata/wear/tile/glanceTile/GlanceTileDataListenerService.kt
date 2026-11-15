package com.diabdata.wear.tile.glanceTile

import android.content.Context
import android.util.Log
import androidx.core.content.edit
import androidx.wear.tiles.TileService
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

class GlanceTileDataListenerService : WearableListenerService() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        Log.d("GlanceTileDataListener", "onDataChanged: ${dataEvents.count} events")

        for (event in dataEvents) {
            Log.d(
                "GlanceTileDataListener",
                "Event: type=${event.type}, path=${event.dataItem.uri.path}"
            )

            if (event.type == DataEvent.TYPE_CHANGED) {
                val path = event.dataItem.uri.path

                if (path == "/diabdata/tile/glance_tile") {
                    val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                    val jsonData = dataMap.getString("json_data")
                    val timestamp = dataMap.getLong("timestamp", 0)
                    val updateCount = dataMap.getInt("updateCount", 0)

                    Log.d(
                        "GlanceTileDataListener",
                        "Received update: timestamp=$timestamp, updateCount=$updateCount"
                    )

                    applicationContext.getSharedPreferences("tile_data", Context.MODE_PRIVATE)
                        .edit {
                            putString("latest_json", jsonData)
                            putLong("last_update", timestamp)
                        }

                    val updater = TileService.getUpdater(applicationContext)
                    updater.requestUpdate(GlanceTileService::class.java)

                    Log.d("GlanceTileDataListener", "Tile update requested.")
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
