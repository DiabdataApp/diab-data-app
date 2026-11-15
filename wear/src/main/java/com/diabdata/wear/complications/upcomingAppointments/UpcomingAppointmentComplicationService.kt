package com.diabdata.wear.complications.upcomingAppointments

import android.graphics.drawable.Icon
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.MonochromaticImage
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import com.diabdata.shared.R as shared
import com.diabdata.shared.utils.dataTypes.AppointmentType
import com.diabdata.shared.utils.imgUtils.toAppointmentIcon

class UpcomingAppointmentComplicationService : SuspendingComplicationDataSourceService() {

    private fun appointmentInXDaysLabel(days: Int): String = resources.getString(
        shared
            .string
            .appointments_wear_complication_upcoming_appointment_in_days_text_description,
        days.toString()
    )

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData {
        val prefs = getSharedPreferences("complications_upcoming_appointment", MODE_PRIVATE)
        val hasAppointment = prefs.getBoolean("hasAppointment", false)

        val appointmentTodayLabel =
            resources.getString(
                shared
                    .string
                    .appointments_wear_complication_upcoming_appointment_today_text_description
            )

        val appointmentTomorrowLabel =
            resources.getString(
                shared
                    .string
                    .appointments_wear_complication_upcoming_appointment_tomorrow_text_description
            )

        val noAppointmentsLabel =
            resources.getString(
                shared
                    .string
                    .appointments_wear_complication_no_upcoming_appointment_text_description
            )

        val daysCountText: String
        val doctor: String
        val contentDescription: String
        val iconRes: Int

        if (hasAppointment) {
            val daysBeforeAppointment = prefs.getInt("nextAppointment", 0)
            val appointmentTypeStr =
                prefs.getString("appointmentType", "APPOINTMENT") ?: "APPOINTMENT"
            doctor = prefs.getString("doctor", "") ?: ""

            iconRes = appointmentTypeStr.toAppointmentIcon(filled = true)

            daysCountText = when (daysBeforeAppointment) {
                0 -> getString(shared.string.date_abbr_today)
                1 -> getString(shared.string.date_abbr_tomorrow)
                else -> resources.getQuantityString(
                    shared.plurals.date_abbr_days,
                    daysBeforeAppointment,
                    daysBeforeAppointment
                )
            }

            contentDescription = when (daysBeforeAppointment) {
                0 -> appointmentTodayLabel
                1 -> appointmentTomorrowLabel
                else -> appointmentInXDaysLabel(daysBeforeAppointment)
            }
        } else {
            daysCountText = "--"
            doctor = ""
            contentDescription = noAppointmentsLabel
            iconRes = AppointmentType.APPOINTMENT.iconFilledRes
        }

        return ShortTextComplicationData.Builder(
            text = PlainComplicationText.Builder(daysCountText).build(),
            contentDescription = PlainComplicationText.Builder(contentDescription).build()
        )
            .setTitle(
                if (doctor.isNotEmpty()) {
                    PlainComplicationText.Builder(doctor).build()
                } else {
                    PlainComplicationText.Builder("").build()
                }
            )
            .setMonochromaticImage(
                MonochromaticImage.Builder(
                    Icon.createWithResource(applicationContext, iconRes)
                ).build()
            )
            .build()
    }

    override fun getPreviewData(type: ComplicationType): ComplicationData =
        ShortTextComplicationData.Builder(
            text = PlainComplicationText.Builder("3d").build(),
            contentDescription = PlainComplicationText.Builder(
                appointmentInXDaysLabel(3)
            ).build()
        )
            .setTitle(PlainComplicationText.Builder("Dr. Doe").build())
            .setMonochromaticImage(
                MonochromaticImage.Builder(
                    Icon.createWithResource(
                        applicationContext,
                        AppointmentType.APPOINTMENT.iconFilledRes
                    )
                ).build()
            )
            .build()
}
