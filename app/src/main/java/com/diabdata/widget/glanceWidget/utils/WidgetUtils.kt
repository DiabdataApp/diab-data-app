package com.diabdata.widget.glanceWidget.utils

import androidx.compose.ui.graphics.Color
import androidx.glance.color.ColorProvider
import androidx.glance.unit.ColorProvider
import com.diabdata.core.utils.ui.darken
import com.diabdata.shared.utils.dataTypes.AppointmentType
import com.diabdata.shared.utils.dataTypes.MedicalDeviceInfoType

fun getAppointmentTypeOrNull(typeName: String?): AppointmentType? = typeName?.let {
    runCatching { AppointmentType.valueOf(it) }.getOrNull()
}

fun getDeviceTypeOrNull(typeName: String?): MedicalDeviceInfoType? = typeName?.let {
    runCatching { MedicalDeviceInfoType.valueOf(it) }.getOrNull()
}

/**
 * Converts a Compose [Color] into a [ColorProvider] for use in Glance widgets.
 *
 * This helper allows consistent color behavior across light and dark modes,
 * while supporting specific variants used in the app's design system.
 *
 * @param variant Defines how the color should be adapted:
 * - [ColorVariant.DEFAULT] — uses the original color for day mode and a darkened version for night mode.
 * - [ColorVariant.CIRCLE_ICON_ICON] — uses a darkened version for both day and night, matching the circular icon foreground style.
 * - [ColorVariant.CIRCLE_ICON_BACKGROUND] — applies an alpha of 0.2f for both day and night, matching the circular icon background style.
 *
 * @return A [ColorProvider] configured according to the given [variant].
 */
fun Color.toColorProvider(variant: ColorVariant): ColorProvider {
    val color = Color(
        red = red,
        green = green,
        blue = blue,
        alpha = alpha
    )

    when (variant) {
        ColorVariant.DEFAULT -> {
            val darkenedColor = color.darken()
            return ColorProvider(day = color, night = darkenedColor)
        }

        ColorVariant.CIRCLE_ICON_ICON -> {
            val darkenedColor = color.darken()
            return ColorProvider(day = darkenedColor, night = darkenedColor)
        }

        ColorVariant.CIRCLE_ICON_BACKGROUND -> {
            return ColorProvider(day = color.copy(alpha = 0.2f), night = color.copy(alpha = 0.2f))
        }
    }
}

fun Int.intToColorProvider(): ColorProvider {
    val composeColor = Color(this)
    return ColorProvider(day = composeColor, night = composeColor)
}

enum class ColorVariant {
    DEFAULT,
    CIRCLE_ICON_ICON,
    CIRCLE_ICON_BACKGROUND
}
