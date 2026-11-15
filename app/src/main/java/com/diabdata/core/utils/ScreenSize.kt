package com.diabdata.core.utils

import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.window.core.layout.WindowSizeClass

enum class ScreenSize {
    LARGE,
    MEDIUM,
    COMPACT
}

@Composable
fun getScreenSize(): ScreenSize {
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass

    val isExpanded = windowSizeClass.isWidthAtLeastBreakpoint(
        WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND
    )
    val isMedium = windowSizeClass.isWidthAtLeastBreakpoint(
        WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND
    )
    val isCompactHeight = !windowSizeClass.isHeightAtLeastBreakpoint(
        WindowSizeClass.HEIGHT_DP_MEDIUM_LOWER_BOUND
    )

    if (isExpanded && !isCompactHeight) {
        return ScreenSize.LARGE
    } else if (isMedium && !isCompactHeight) {
        return ScreenSize.MEDIUM
    } else {
        return ScreenSize.COMPACT
    }
}
