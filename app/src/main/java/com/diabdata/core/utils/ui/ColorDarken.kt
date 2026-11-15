package com.diabdata.core.utils.ui

import androidx.compose.ui.graphics.Color

fun Color.darken(factor: Float = 0.3f): Color = Color(
    red = (red * (1 - factor)).coerceIn(0f, 1f),
    green = (green * (1 - factor)).coerceIn(0f, 1f),
    blue = (blue * (1 - factor)).coerceIn(0f, 1f),
    alpha = alpha
)
