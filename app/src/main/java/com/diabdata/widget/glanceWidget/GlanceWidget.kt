package com.diabdata.widget.glanceWidget

import android.appwidget.AppWidgetManager
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.diabdata.shared.R as shared
import com.diabdata.shared.utils.dataTypes.AddableType
import com.diabdata.shared.utils.dateUtils.toRelativeString
import com.diabdata.shared.utils.dateUtils.toShortenedFormatLocalDate
import com.diabdata.widget.WidgetState
import com.diabdata.widget.glanceWidget.utils.ColorVariant
import com.diabdata.widget.glanceWidget.utils.getAppointmentTypeOrNull
import com.diabdata.widget.glanceWidget.utils.getDeviceTypeOrNull
import com.diabdata.widget.glanceWidget.utils.toColorProvider
import com.diabdata.widget.workers.GlanceWidgetWorker
import java.time.LocalDate
import java.util.concurrent.TimeUnit

class GlanceWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = GlanceWidget()

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        enqueueWidgetUpdateWorker(context)
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        enqueueWidgetUpdateWorker(context)
    }

    private fun enqueueWidgetUpdateWorker(context: Context) {
        val workManager = WorkManager.getInstance(context)

        val workRequest = PeriodicWorkRequestBuilder<GlanceWidgetWorker>(
            30,
            TimeUnit.MINUTES
        )
            .setInitialDelay(1, timeUnit = TimeUnit.MINUTES)
            .build()

        workManager.enqueueUniquePeriodicWork(
            "glance_widget_update",
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }
}

/**
 * Simple widget to display basic infos
 *
 * This widget displays the next upcoming appointment as well as the list of active devices.
 * To add data or udpate its data structure make sure to update the WidgetState proto file found
 * in `app/src/main/proto/widget_state.proto` as well as its related file for update when data is
 * updated in the local database `app/src/main/java/com/diabdata/DiabDataApp.kt`
 */
class GlanceWidget : GlanceAppWidget() {

    override val stateDefinition = WidgetGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme {
                WidgetContent(context)
            }
        }
    }

    @Composable
    private fun WidgetContent(context: Context) {
        val state = currentState<WidgetState>()

        val devices = state.devices
        val appointment = state.nextAppointment

        Row(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(
                    horizontal = 10.dp,
                    vertical = 6.dp
                )
                .background(GlanceTheme.colors.widgetBackground)
                .cornerRadius(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (appointment.date.isNotEmpty()) {
                val typeEnum = getAppointmentTypeOrNull(appointment.type)
                typeEnum?.let {
                    Column(
                        modifier = GlanceModifier.defaultWeight(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val baseBgColor = AddableType.APPOINTMENT.baseColor.copy(alpha = 0.2f)

                        Text(
                            text = appointment.doctor,
                            style = TextStyle(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = AddableType.APPOINTMENT.baseColor.toColorProvider(
                                    ColorVariant.CIRCLE_ICON_ICON
                                )
                            ),
                            maxLines = 1
                        )

                        Spacer(modifier = GlanceModifier.height(4.dp))

                        Image(
                            provider = ImageProvider(it.iconRes),
                            contentDescription = it.displayName(context),
                            modifier = GlanceModifier
                                .size(40.dp)
                                .background(baseBgColor)
                                .padding(8.dp)
                                .cornerRadius(100.dp),
                            colorFilter = ColorFilter.tint(
                                AddableType.APPOINTMENT.baseColor.toColorProvider(
                                    ColorVariant.CIRCLE_ICON_ICON
                                )
                            )
                        )

                        Spacer(modifier = GlanceModifier.height(4.dp))

                        Text(
                            text = appointment.date.toShortenedFormatLocalDate(),
                            style = TextStyle(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = GlanceTheme.colors.onSurface
                            ),
                            maxLines = 1
                        )
                    }
                }
            }

            devices.forEach { device ->
                Spacer(modifier = GlanceModifier.width(8.dp))

                val deviceTypeEnum = getDeviceTypeOrNull(device.type)
                deviceTypeEnum?.let {
                    Column(
                        modifier = GlanceModifier.defaultWeight(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val baseBgColor = it.baseColor.copy(alpha = 0.2f)

                        Text(
                            text = device.name,
                            style = TextStyle(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = it.baseColor.toColorProvider(ColorVariant.CIRCLE_ICON_ICON)
                            ),
                            maxLines = 1
                        )

                        Spacer(modifier = GlanceModifier.height(4.dp))

                        Image(
                            provider = ImageProvider(it.iconRes),
                            contentDescription = it.displayName(context),
                            modifier = GlanceModifier
                                .size(40.dp)
                                .background(baseBgColor)
                                .padding(8.dp)
                                .cornerRadius(100.dp),
                            colorFilter = ColorFilter.tint(
                                it.baseColor.toColorProvider(ColorVariant.CIRCLE_ICON_ICON)
                            )
                        )

                        Spacer(modifier = GlanceModifier.height(4.dp))

                        val endDate = LocalDate.parse(device.lifeSpanEndDate)

                        val timeLeftString = endDate.toRelativeString(context)

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (device.daysLeft == 0) {
                                Image(
                                    provider = ImageProvider(shared.drawable.warning_icon_vector),
                                    contentDescription = "Warning",
                                    modifier = GlanceModifier
                                        .size(16.dp)
                                        .padding(end = 4.dp),
                                    colorFilter = ColorFilter.tint(
                                        GlanceTheme.colors.error
                                    )
                                )

                                Spacer(modifier = GlanceModifier.width(2.dp))
                            }

                            Text(
                                text = timeLeftString,
                                style = TextStyle(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GlanceTheme.colors.onSurface
                                ),
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            if (appointment.date.isEmpty() && devices.isEmpty()) {
                Text(
                    text = context.getString(shared.string.widget_no_data),
                    modifier = GlanceModifier.defaultWeight(),
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = GlanceTheme.colors.onSurface
                    )
                )
            }
        }
    }
}
