package com.diabdata.widget

data class WidgetDevice(
    val name: String = "",
    val type: String = "",
    val daysLeft: Int = 0,
    val lifespanProgression: Int = 0,
    val lifeSpanEndDate: String = ""
)

data class WidgetAppointment(val date: String = "", val doctor: String = "", val type: String = "")

data class WidgetState(
    val nextAppointment: WidgetAppointment = WidgetAppointment(),
    val devices: List<WidgetDevice> = emptyList(),
    val lastUpdated: Long = 0
)
