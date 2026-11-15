package com.diabdata.shared.utils.dataTypes

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import com.diabdata.shared.R as shared

/**
 * Represents a type of medical devices.
 * @param displayNameRes The string resource ID for the devices's display name.
 * @param baseColor The base color of the devices's icon.
 * @param iconRes The drawable resource ID for the devices's icon.
 */
enum class MedicalDeviceInfoType(
    @param:StringRes
    val displayNameRes: Int,
    val baseColor: Color,
    @param:DrawableRes val iconRes: Int,
    @param:DrawableRes val iconFilledRes: Int
) {
    WIRELESS_PATCH(
        displayNameRes = shared.string.device_type_wireless_patch,
        iconRes = shared.drawable.wireless_patch_icon_vector,
        iconFilledRes = shared.drawable.wireless_patch_filled_icon_vector,
        baseColor = Color(0xFFDEA32D)
    ),
    WIRED_PATCH(
        displayNameRes = shared.string.devices_device_type_wired_patch,
        iconRes = shared.drawable.wired_patch_icon_vector,
        iconFilledRes = shared.drawable.wired_patch_filled_icon_vector,
        baseColor = Color(0xFFBEDE2D)
    ),
    CONTINUOUS_GLUCOSE_MONITORING_SYSTEM_SENSOR(
        displayNameRes = shared.string.devices_device_type_cgm_sensor,
        iconRes = shared.drawable.continuous_glucose_monitoring_system_sensor_icon_vector,
        iconFilledRes = shared.drawable.continuous_glucose_monitoring_system_sensor_filled_icon_vector,
        baseColor = Color(0xFFFA7450)
    ),
    CONTINUOUS_GLUCOSE_MONITORING_SYSTEM_TRANSMITTER(
        displayNameRes = shared.string.devices_device_type_cgm_transmitter,
        iconRes = shared.drawable.continuous_glucose_monitoring_system_transmitter_icon_vector,
        iconFilledRes = shared.drawable.continuous_glucose_monitoring_system_transmitter_filled_icon_vector,
        baseColor = Color(0xFF458DEA)
    ),
    WIRELESS_PATCH_REMOTE(
        displayNameRes = shared.string.devices_device_type_wireless_patch_remote,
        iconRes = shared.drawable.wireless_patch_remote_icon_vector,
        iconFilledRes = shared.drawable.wireless_patch_remote_filled_icon_vector,
        baseColor = Color(0xFF2DDE7D)
    ),
    WIRED_PUMP(
        displayNameRes = shared.string.devices_device_type_wired_pump,
        iconRes = shared.drawable.wired_pump_icon_vector,
        iconFilledRes = shared.drawable.wired_pump_filled_icon_vector,
        baseColor = Color(0xFF62DE2D)
    ),
    UNKNOWN(
        displayNameRes = shared.string.devices_device_type_unknown,
        iconRes = shared.drawable.no_devices_icon_vector,
        iconFilledRes = shared.drawable.no_devices_filled_icon_vector,
        baseColor = Color(0xFF62DE2D)
    );

    fun displayName(context: Context): String = context.getString(displayNameRes)
}
