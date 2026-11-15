package com.diabdata.shared.utils.dataTypes

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.diabdata.shared.R

enum class AppointmentType(
    @param:StringRes
    val displayNameRes: Int,
    @param:DrawableRes val iconRes: Int,
    @param:DrawableRes val iconFilledRes: Int
) {
    ANNUAL_CHECKUP(
        displayNameRes = R.string.appointments_annual_checkup_appointment_label,
        iconRes = R.drawable.recurring_event_icon_vector,
        iconFilledRes = R.drawable.recurring_event_filled_icon_vector

    ),
    APPOINTMENT(
        displayNameRes = R.string.appointments_appointment_one_time_appointment_label,
        iconRes = R.drawable.stethoscope_icon_vector,
        iconFilledRes = R.drawable.stethoscope_filled_icon_vector
    );

    fun displayName(context: Context): String = context.getString(displayNameRes)
}
