package com.diabdata.feature.graphs.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.Wallpapers
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.ColorUtils
import com.diabdata.core.utils.linearRegression
import com.diabdata.feature.graphs.classes.PlotPoint
import com.diabdata.shared.R as shared
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.dashed
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.component.shapeComponent
import com.patrykandpatrick.vico.core.cartesian.Zoom
import com.patrykandpatrick.vico.core.cartesian.axis.Axis
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.common.Fill
import com.patrykandpatrick.vico.core.common.Insets
import com.patrykandpatrick.vico.core.common.Position
import com.patrykandpatrick.vico.core.common.component.TextComponent
import com.patrykandpatrick.vico.core.common.shader.ShaderProvider
import com.patrykandpatrick.vico.core.common.shape.CorneredShape
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun LineGraph(
    points: List<PlotPoint>,
    modifier: Modifier = Modifier,
    label: String,
    primaryColor: Int = MaterialTheme.colorScheme.primary.toArgb(),
    showTrendLine: Boolean = false
) {
    val hsv = FloatArray(3)
    ColorUtils.colorToHSL(primaryColor, hsv)
    hsv[0] = (hsv[0] + 80f) % 360f
    hsv[1] *= 0.8f
    val adjusted = ColorUtils.HSLToColor(hsv)
    val trendCurveColor = Color(adjusted).copy(alpha = 0.8f).toArgb()

    if (points.isEmpty()) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            Text(stringResource(shared.string.homescreen_no_data_text))
        }
        return
    }

    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd/MM/yy", Locale.getDefault()) }

    val pointLabelBackground =
        MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp).copy(alpha = 0.80f).toArgb()

    val xValues = points.mapIndexed { index, _ -> index.toDouble() }
    val yValues = points.map { it.value.toDouble() }

    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(points, showTrendLine) {
        modelProducer.runTransaction {
            lineSeries {
                series(x = xValues, y = yValues)
                if (showTrendLine && points.size > 1) {
                    val regression = linearRegression(xValues, yValues)
                    val trendY = xValues.map { regression(it) }
                    series(x = xValues, y = trendY)
                }
            }
        }
    }

    val zoomState = rememberVicoZoomState(zoomEnabled = true, initialZoom = Zoom.x(-1.0))
    val scrollState = rememberVicoScrollState(scrollEnabled = true)

    val pointValueLabel: TextComponent = rememberTextComponent(
        color = Color(primaryColor),
        textSize = 11.sp,
        background = shapeComponent(
            shape = CorneredShape.Pill,
            fill = Fill(pointLabelBackground)
        ),
        padding = Insets(startDp = 6f, topDp = 4f, endDp = 6f, bottomDp = 4f),
        margins = Insets(startDp = 0f, topDp = 8f, endDp = 0f, bottomDp = 0f)
    )

    val pointValueFormatter =
        CartesianValueFormatter { _, value, _ ->
            "%.1f".format(Locale.getDefault(), value)
        }

    val lines = mutableListOf(
        LineCartesianLayer.Line(
            fill = LineCartesianLayer.LineFill.single(Fill(primaryColor)),
            areaFill = LineCartesianLayer.AreaFill.single(
                Fill(
                    ShaderProvider.verticalGradient(
                        ColorUtils.setAlphaComponent(primaryColor, 60),
                        Color.Transparent.toArgb()
                    )
                )
            ),
            pointConnector = LineCartesianLayer.PointConnector.cubic(0.5f),
            pointProvider = LineCartesianLayer.PointProvider.single(
                point = LineCartesianLayer.Point(
                    component = shapeComponent(
                        shape = CorneredShape.Pill,
                        fill = Fill(primaryColor)
                    ),
                    sizeDp = 8f
                )
            ),
            dataLabel = pointValueLabel,
            dataLabelPosition = Position.Vertical.Bottom, // or Bottom if you prefer under the point
            dataLabelValueFormatter = pointValueFormatter,
            dataLabelRotationDegrees = 0f
        )
    )

    if (showTrendLine) {
        lines += LineCartesianLayer.Line(
            fill = LineCartesianLayer.LineFill.single(Fill(trendCurveColor)),
            pointProvider = null,
            pointConnector = LineCartesianLayer.PointConnector.Sharp,
            stroke = LineCartesianLayer.LineStroke.dashed(
                cap = StrokeCap.Butt,
                dashLength = 12.dp,
                gapLength = 5.dp
            )
        )
    }

    val layer = rememberLineCartesianLayer(
        verticalAxisPosition = Axis.Position.Vertical.Start,
        lineProvider = LineCartesianLayer.LineProvider.series(*lines.toTypedArray())
    )

    Column(modifier = modifier) {
        Text(
            text = label.uppercase(LocalLocale.current.platformLocale),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.surfaceTint
        )
        Spacer(Modifier.height(12.dp))
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            CartesianChartHost(
                chart = rememberCartesianChart(
                    layer,
                    startAxis = VerticalAxis.rememberStart(),
                    bottomAxis = HorizontalAxis.rememberBottom(
                        valueFormatter = CartesianValueFormatter { _, x, _ ->
                            points.getOrNull(x.toInt())?.date?.format(dateFormatter) ?: " "
                        },
                        itemPlacer = HorizontalAxis.ItemPlacer.aligned()
                    )
                ),
                modelProducer = modelProducer,
                zoomState = zoomState,
                scrollState = scrollState,
                modifier = modifier
                    .fillMaxSize()
                    .padding(start = 16.dp, top = 20.dp, bottom = 20.dp, end = 30.dp)
            )
        }
    }
}

@Preview(
    showBackground = true,
    wallpaper = Wallpapers.NONE,
    uiMode = Configuration.UI_MODE_TYPE_NORMAL
)
@Composable
fun ImportantDatesListPreview() {
    val fakePoints: List<PlotPoint> =
        listOf(
            PlotPoint(value = 82.5f, date = LocalDate.of(2025, 4, 2)),
            PlotPoint(value = 83f, date = LocalDate.of(2025, 5, 14)),
            PlotPoint(value = 80f, date = LocalDate.of(2025, 6, 30)),
            PlotPoint(value = 79.9f, date = LocalDate.of(2025, 7, 17))
        )

    LineGraph(
        points = fakePoints,
        label = "Poids"
    )
}
