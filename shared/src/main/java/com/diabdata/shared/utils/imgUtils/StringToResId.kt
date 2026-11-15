package com.diabdata.shared.utils.imgUtils

import com.diabdata.shared.utils.dataTypes.AppointmentType
import com.diabdata.shared.utils.dataTypes.MedicalDeviceInfoType
import com.diabdata.shared.utils.dataTypes.TreatmentType

/**
 * Safely converts a string to its corresponding icon resource ID.
 *
 * @receiver String representation of the enum value
 * @param type The type of resource to look up
 * @param variant The icon variant (outlined or filled)
 * @return The icon resource ID, or the appropriate fallback if not found
 */
fun String.toIconRes(type: ResourceType, variant: IconVariant = IconVariant.OUTLINED): Int =
    type.getIconRes(this, variant)

/**
 * Sealed class representing different types of resources.
 *
 * @property Device Resource type for devices.
 * @property Appointment Resource type for appointments.
 * @property Treatment Resource type for treatments.
 */
sealed class ResourceType {
    abstract fun getIconRes(value: String, variant: IconVariant): Int

    object Device : ResourceType() {
        override fun getIconRes(value: String, variant: IconVariant): Int = try {
            val deviceType = MedicalDeviceInfoType.valueOf(value)
            when (variant) {
                IconVariant.OUTLINED -> deviceType.iconRes
                IconVariant.FILLED -> deviceType.iconFilledRes
            }
        } catch (e: IllegalArgumentException) {
            when (variant) {
                IconVariant.OUTLINED -> MedicalDeviceInfoType.UNKNOWN.iconRes
                IconVariant.FILLED -> MedicalDeviceInfoType.UNKNOWN.iconFilledRes
            }
        }
    }

    object Appointment : ResourceType() {
        override fun getIconRes(value: String, variant: IconVariant): Int = try {
            val appointmentType = AppointmentType.valueOf(value)
            when (variant) {
                IconVariant.OUTLINED -> appointmentType.iconRes
                IconVariant.FILLED -> appointmentType.iconFilledRes
            }
        } catch (e: IllegalArgumentException) {
            when (variant) {
                IconVariant.OUTLINED -> AppointmentType.APPOINTMENT.iconRes
                IconVariant.FILLED -> AppointmentType.APPOINTMENT.iconFilledRes
            }
        }
    }

    object Treatment : ResourceType() {
        override fun getIconRes(value: String, variant: IconVariant): Int = try {
            val treatmentType = TreatmentType.valueOf(value)
            when (variant) {
                IconVariant.OUTLINED -> treatmentType.iconRes
                IconVariant.FILLED -> treatmentType.iconFilledRes
            }
        } catch (e: IllegalArgumentException) {
            when (variant) {
                IconVariant.OUTLINED -> TreatmentType.UNKNOWN.iconRes
                IconVariant.FILLED -> TreatmentType.UNKNOWN.iconFilledRes
            }
        }
    }
}

/**
 * Icon variants available
 */
enum class IconVariant {
    OUTLINED,
    FILLED
}

/**
 * Extension function to convert a string representation of a devices type to its corresponding icon resource ID.
 */
fun String.toDeviceIcon(filled: Boolean = false): Int =
    toIconRes(ResourceType.Device, if (filled) IconVariant.FILLED else IconVariant.OUTLINED)

/**
 * Extension function to convert a string representation of an appointment type to its corresponding icon resource ID.
 */
fun String.toAppointmentIcon(filled: Boolean = false): Int =
    toIconRes(ResourceType.Appointment, if (filled) IconVariant.FILLED else IconVariant.OUTLINED)

/**
 * Extension function to convert a string representation of a treatments type to its corresponding icon resource ID.
 */
fun String.toTreatmentIcon(filled: Boolean = false): Int =
    toIconRes(ResourceType.Treatment, if (filled) IconVariant.FILLED else IconVariant.OUTLINED)
