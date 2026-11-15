package com.diabdata.core.ui.components.actionInput

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.diabdata.core.utils.ui.SvgIcon
import com.diabdata.shared.R as shared

@Composable
fun FlippableSelectionIcon(isSelected: Boolean) {
    val animatedRotation by animateFloatAsState(
        targetValue = if (isSelected) 180f else 0f,
        animationSpec = tween(durationMillis = 300)
    )

    val showingSelectedIcon = animatedRotation < 90f || animatedRotation > 270f

    SvgIcon(
        resId = if (showingSelectedIcon) {
            shared.drawable.radio_button_unchecked_icon_vector
        } else {
            shared.drawable.radio_button_checked_icon_vector
        },
        modifier = Modifier
            .size(15.dp)
            .graphicsLayer {
                rotationY = animatedRotation
                cameraDistance = 12f * density
                scaleX = if (animatedRotation in 90f..270f) {
                    -1f
                } else {
                    1f
                }
            },
        color = MaterialTheme.colorScheme.primary
    )
}
