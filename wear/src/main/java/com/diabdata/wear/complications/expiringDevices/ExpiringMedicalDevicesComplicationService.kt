package com.diabdata.wear.complications.expiringDevices

import android.graphics.drawable.Icon
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.MonochromaticImage
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.RangedValueComplicationData
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import com.diabdata.shared.R as shared
import com.diabdata.shared.utils.dataTypes.MedicalDeviceInfoType
import com.diabdata.shared.utils.imgUtils.toDeviceIcon

class ExpiringMedicalDevicesComplicationService : SuspendingComplicationDataSourceService() {

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData {
        val prefs = getSharedPreferences("complications_medical_device_expiry", MODE_PRIVATE)
        val devicesDaysBeforeExpiry = prefs.getString("nextDeviceExpiry", "0") ?: "0"
        val daysBeforeExpiry = devicesDaysBeforeExpiry.toInt()
        val deviceLifespanValue = prefs.getString("nextDeviceLifespan", "0") ?: "0"
        val deviceTypeStr = prefs.getString("deviceIconRes", "UNKNOWN") ?: "UNKNOWN"

        val latestIconRes = deviceTypeStr.toDeviceIcon(filled = true)

        val daysCountText = if (daysBeforeExpiry > 0) {
            resources.getQuantityString(
                shared.plurals.date_abbr_days,
                daysBeforeExpiry,
                daysBeforeExpiry
            )
        } else {
            getString(shared.string.date_abbr_today)
        }

        val rangeValue: Float = deviceLifespanValue.toFloat() - devicesDaysBeforeExpiry.toFloat()

        return RangedValueComplicationData.Builder(
            value = rangeValue.coerceAtLeast(0f),
            min = 0f,
            max = if (deviceLifespanValue.toFloat() > 0f) deviceLifespanValue.toFloat() else 1f,
            contentDescription = PlainComplicationText.Builder(
                getString(
                    shared.string.devices_wear_complication_expiring_devices_plain_text_builder
                )
            )
                .build()
        )
            .setTitle(PlainComplicationText.Builder(daysCountText).build())
            .setMonochromaticImage(
                MonochromaticImage.Builder(
                    Icon.createWithResource(applicationContext, latestIconRes)
                ).build()
            )
            .build()
    }

    override fun getPreviewData(type: ComplicationType): ComplicationData =
        RangedValueComplicationData.Builder(
            value = 2f,
            min = 0f,
            max = 3f,
            contentDescription = PlainComplicationText.Builder(
                getString(
                    shared.string.devices_wear_complication_expiring_devices_plain_text_builder
                )
            )
                .build()
        )
            .setTitle(PlainComplicationText.Builder("2D").build())
            .setMonochromaticImage(
                MonochromaticImage.Builder(
                    Icon.createWithResource(
                        applicationContext,
                        MedicalDeviceInfoType.UNKNOWN.iconFilledRes
                    )
                ).build()
            )
            .build()
}
