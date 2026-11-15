package com.diabdata.wear.complications.expiringTreatments

import android.graphics.drawable.Icon
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.MonochromaticImage
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.RangedValueComplicationData
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import com.diabdata.shared.R as shared
import com.diabdata.shared.utils.dataTypes.AddableType
import com.diabdata.shared.utils.imgUtils.toTreatmentIcon

class ExpiringTreatmentComplicationService : SuspendingComplicationDataSourceService() {

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData {
        val prefs = getSharedPreferences("complications_treatment_expiry", MODE_PRIVATE)
        val treatmentDaysBeforeExpiry = prefs.getString("nextTreatmentExpiry", "0") ?: "0"
        val daysBeforeExpiry = treatmentDaysBeforeExpiry.toInt()
        val treatmentElapsedLifespan = prefs.getString("elapsedTreatmentConservation", "0") ?: "0"
        val treatmentTypeStr = prefs.getString("treatmentIconRes", "UNKNOWN") ?: "UNKNOWN"

        val latestIconRes = treatmentTypeStr.toTreatmentIcon(filled = true)

        val daysCountText = if (daysBeforeExpiry > 0) {
            resources.getQuantityString(
                shared.plurals.date_abbr_days,
                daysBeforeExpiry,
                daysBeforeExpiry
            )
        } else {
            getString(shared.string.date_abbr_today)
        }

        val totalLifespan: Float = treatmentElapsedLifespan.toFloat() + daysBeforeExpiry.toFloat()

        val rangeValue: Float = treatmentElapsedLifespan.toFloat()

        return RangedValueComplicationData.Builder(
            value = rangeValue.coerceAtLeast(0f),
            min = 0f,
            max = if (totalLifespan > 0f) totalLifespan else 1f,
            contentDescription = PlainComplicationText.Builder(
                getString(
                    shared.string.medications_wear_complication_expiring_treatments_plain_text_builder
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
                    shared.string.medications_wear_complication_expiring_treatments_plain_text_builder
                )
            )
                .build()
        )
            .setTitle(PlainComplicationText.Builder("1D").build())
            .setMonochromaticImage(
                MonochromaticImage.Builder(
                    Icon.createWithResource(
                        applicationContext,
                        AddableType.TREATMENT.iconFilledRes
                    )
                ).build()
            )
            .build()
}
