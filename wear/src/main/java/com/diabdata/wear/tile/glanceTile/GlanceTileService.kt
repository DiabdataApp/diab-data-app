package com.diabdata.wear.tile.glanceTile

import android.util.Log
import androidx.core.graphics.toColorInt
import androidx.core.net.toUri
import androidx.wear.protolayout.DeviceParametersBuilders
import androidx.wear.protolayout.DimensionBuilders.dp
import androidx.wear.protolayout.DimensionBuilders.expand
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.LayoutElementBuilders.Column
import androidx.wear.protolayout.LayoutElementBuilders.LayoutElement
import androidx.wear.protolayout.LayoutElementBuilders.Spacer
import androidx.wear.protolayout.ResourceBuilders
import androidx.wear.protolayout.TimelineBuilders
import androidx.wear.protolayout.material3.CardColors
import androidx.wear.protolayout.material3.ColorScheme
import androidx.wear.protolayout.material3.MaterialScope
import androidx.wear.protolayout.material3.Typography
import androidx.wear.protolayout.material3.icon
import androidx.wear.protolayout.material3.iconDataCard
import androidx.wear.protolayout.material3.materialScope
import androidx.wear.protolayout.material3.primaryLayout
import androidx.wear.protolayout.material3.text
import androidx.wear.protolayout.modifiers.clickable
import androidx.wear.protolayout.types.LayoutColor
import androidx.wear.protolayout.types.layoutString
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.TileBuilders
import androidx.wear.tiles.TileService
import com.diabdata.shared.R
import com.diabdata.shared.utils.dataTypes.AppointmentType
import com.diabdata.shared.utils.dataTypes.MedicalDeviceInfoType
import com.diabdata.shared.utils.dataTypes.TreatmentType
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.Wearable
import com.google.common.util.concurrent.ListenableFuture
import com.google.gson.Gson
import java.util.concurrent.Executors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.cancel
import kotlinx.coroutines.guava.future
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

data class TileData(
    val device: DeviceInfo?,
    val treatment: TreatmentInfo?,
    val appointment: AppointmentInfo?,
    val timestamp: Long = System.currentTimeMillis()
)

data class DeviceInfo(
    val type: String,
    val name: String,
    val daysUntilExpiry: Int,
    val lifespan: String
)

data class TreatmentInfo(
    val type: String,
    val name: String,
    val daysUntilExpiry: Int,
    val daysSinceCreation: Int
)

data class AppointmentInfo(
    val type: String,
    val name: String,
    val doctor: String,
    val daysUntil: Int
)

class GlanceTileService : TileService() {
    companion object {
        private const val CACHE_DURATION = 5000L
        private const val FETCH_TIMEOUT = 3000L
        const val TILE_VERSION = "4"
        private const val DATA_PATH = "wear://*/diabdata/tile/glance_tile"
    }

    private val gson = Gson()
    private val serviceScope = CoroutineScope(
        Executors.newSingleThreadExecutor().asCoroutineDispatcher() + SupervisorJob()
    )
    private var cachedData: TileData? = null
    private var cacheTime: Long = 0

    override fun onTileResourcesRequest(
        requestParams: RequestBuilders.ResourcesRequest
    ): ListenableFuture<ResourceBuilders.Resources> = serviceScope.future {
        ResourceBuilders.Resources.Builder()
            .setVersion(TILE_VERSION)
            .apply {
                registerAllImages(this)
            }
            .build()
    }

    private fun registerAllImages(builder: ResourceBuilders.Resources.Builder) {
        MedicalDeviceInfoType.entries.forEach { device ->
            builder.addIdToImageMapping(
                device.name,
                ResourceBuilders.ImageResource.Builder()
                    .setAndroidResourceByResId(
                        ResourceBuilders.AndroidImageResourceByResId.Builder()
                            .setResourceId(device.iconFilledRes)
                            .build()
                    )
                    .build()
            )
        }

        TreatmentType.entries.forEach { treatment ->
            builder.addIdToImageMapping(
                treatment.name,
                ResourceBuilders.ImageResource.Builder()
                    .setAndroidResourceByResId(
                        ResourceBuilders.AndroidImageResourceByResId.Builder()
                            .setResourceId(treatment.iconFilledRes)
                            .build()
                    )
                    .build()
            )
        }

        AppointmentType.entries.forEach { appointment ->
            builder.addIdToImageMapping(
                appointment.name,
                ResourceBuilders.ImageResource.Builder()
                    .setAndroidResourceByResId(
                        ResourceBuilders.AndroidImageResourceByResId.Builder()
                            .setResourceId(appointment.iconFilledRes)
                            .build()
                    )
                    .build()
            )
        }

        builder.addIdToImageMapping(
            "sync_icon",
            ResourceBuilders.ImageResource.Builder()
                .setAndroidResourceByResId(
                    ResourceBuilders.AndroidImageResourceByResId.Builder()
                        .setResourceId(R.drawable.hourglass_icon_vector)
                        .build()
                )
                .build()
        )
    }

    override fun onTileRequest(
        requestParams: RequestBuilders.TileRequest
    ): ListenableFuture<TileBuilders.Tile> = serviceScope.future {
        val tileData = getCachedOrFreshData()

        TileBuilders.Tile.Builder()
            .setResourcesVersion(TILE_VERSION)
            .setTileTimeline(
                TimelineBuilders.Timeline.fromLayoutElement(
                    createLayout(tileData, requestParams.deviceConfiguration)
                )
            )
            .build()
    }

    private fun createLayout(
        data: TileData?,
        deviceConfiguration: DeviceParametersBuilders.DeviceParameters
    ): LayoutElement = materialScope(
        context = this,
        deviceConfiguration = deviceConfiguration,
        allowDynamicTheme = true
    ) {
        if (data != null) {
            buildDataLayout(data)
        } else {
            buildEmptyLayout()
        }
    }

    // Extension function pour MaterialScope pour construire la mise en page avec données
    private fun MaterialScope.buildDataLayout(data: TileData): LayoutElement = primaryLayout(
        mainSlot = {
            column {
                setWidth(expand())

                data.device?.let { device ->
                    addContent(
                        createDataCard(
                            device.type,
                            device.name,
                            "Expire dans ${device.daysUntilExpiry}j",
                            getCardColors(device.daysUntilExpiry)
                        )
                    )
                    addContent(Spacer.Builder().setHeight(dp(8f)).build())
                }

                data.treatment?.let { treatment ->
                    addContent(
                        createDataCard(
                            treatment.type,
                            treatment.name,
                            "Expire dans ${treatment.daysUntilExpiry}j",
                            getCardColors(treatment.daysUntilExpiry)
                        )
                    )
                    addContent(Spacer.Builder().setHeight(dp(8f)).build())
                }

                data.appointment?.let { appointment ->
                    addContent(
                        createDataCard(
                            appointment.type,
                            appointment.name,
                            "Dans ${appointment.daysUntil}j - ${appointment.doctor}",
                            colors = CardColors(ColorScheme().primary)
                        )
                    )
                }
            }
        }
    )

    private fun MaterialScope.createDataCard(
        iconId: String,
        primaryText: String,
        secondaryText: String,
        colors: CardColors
    ): LayoutElement = iconDataCard(
        secondaryIcon = {
            icon(
                protoLayoutResourceId = iconId,
                width = dp(24f),
                height = dp(24f),
                tintColor = colorScheme.onSurfaceVariant
            )
        },
        title = {
            text(
                text = primaryText.layoutString,
                typography = Typography.BODY_MEDIUM,
                color = colors.titleColor
            )
        },
        content = {
            text(
                text = secondaryText.layoutString,
                typography = Typography.LABEL_SMALL,
                color = colors.contentColor
            )
        },
        shape = shapes.medium,
        colors = colors,
        onClick = clickable()
    )

    private fun MaterialScope.buildEmptyLayout(): LayoutElement = primaryLayout(
        mainSlot = {
            Column.Builder()
                .setHorizontalAlignment(LayoutElementBuilders.HORIZONTAL_ALIGN_CENTER)
                .setWidth(expand())
                .setHeight(expand())
                .addContent(
                    icon(
                        protoLayoutResourceId = "sync_icon",
                        width = dp(48f),
                        height = dp(48f),
                        tintColor = colorScheme.onSurfaceVariant
                    )
                )
                .addContent(Spacer.Builder().setHeight(dp(12f)).build())
                .addContent(
                    text(
                        text = "En attente de synchronisation".layoutString,
                        typography = Typography.BODY_MEDIUM,
                        color = colorScheme.onSurface
                    )
                )
                .addContent(Spacer.Builder().setHeight(dp(4f)).build())
                .addContent(
                    text(
                        text = "Ouvrir l'application".layoutString,
                        typography = Typography.LABEL_SMALL,
                        color = colorScheme.onSurfaceVariant
                    )
                )
                .build()
        }
    )

    private fun MaterialScope.getCardColors(daysUntilExpiry: Int): CardColors = when {
        daysUntilExpiry <= 3 -> {
            // Rouge pour urgence
            CardColors(
                backgroundColor = LayoutColor("#B71C1C".toColorInt()),
                contentColor = colorScheme.onError
            )
        }
        daysUntilExpiry <= 7 -> {
            // Orange pour attention
            CardColors(
                backgroundColor = LayoutColor("#E65100".toColorInt()),
                contentColor = colorScheme.onPrimary
            )
        }
        else -> {
            // Vert pour OK
            CardColors(
                backgroundColor = LayoutColor("#1B5E20".toColorInt()),
                contentColor = colorScheme.onPrimary
            )
        }
    }

    private fun column(builder: Column.Builder.() -> Unit): Column =
        Column.Builder().apply(builder).build()

    private suspend fun getCachedOrFreshData(): TileData? {
        val currentTime = System.currentTimeMillis()
        return if (cachedData != null && (currentTime - cacheTime) < CACHE_DURATION) {
            cachedData
        } else {
            val freshData = withTimeoutOrNull(FETCH_TIMEOUT) { fetchTileData() }
            cachedData = freshData
            cacheTime = currentTime
            freshData
        }
    }

    private suspend fun fetchTileData(): TileData? = try {
        withContext(Dispatchers.IO) {
            val dataClient = Wearable.getDataClient(applicationContext)
            val dataItems = dataClient.getDataItems(DATA_PATH.toUri()).await()

            val result = dataItems.firstOrNull()?.let { item ->
                val dataMap = DataMapItem.fromDataItem(item).dataMap
                val jsonData = dataMap.getString("json_data")
                jsonData?.let {
                    try {
                        gson.fromJson(it, TileData::class.java)
                    } catch (e: Exception) {
                        Log.e("GlanceTileService", "Error parsing JSON", e)
                        null
                    }
                }
            }

            dataItems.release()
            result
        }
    } catch (e: Exception) {
        Log.e("GlanceTileService", "Error fetching data", e)
        null
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
