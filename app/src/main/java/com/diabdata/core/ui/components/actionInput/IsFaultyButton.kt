package com.diabdata.core.ui.components.actionInput

import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.diabdata.core.utils.ui.SvgIcon
import com.diabdata.shared.R as shared
import kotlinx.coroutines.delay

data class ButtonSize(val circle: Int, val icon: Int)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FaultyToggleButton(
    modifier: Modifier = Modifier,
    isFaulty: Boolean,
    isReported: Boolean,
    type: ButtonType = ButtonType.FAULTY,
    onClick: () -> Unit,
    animatedContainerColor: Color,
    animatedIconColor: Color,
    sizing: ButtonSize = ButtonSize(30, 20)
) {
    var clicked by remember { mutableStateOf(false) }

    LaunchedEffect(clicked) {
        if (clicked) {
            delay(200)
            clicked = false
        }
    }

    IconButton(
        onClick = {
            clicked = true
            onClick()
        },
        shapes = IconButtonDefaults.shapes(
            shape = MaterialShapes.Square.toShape(),
            pressedShape = MaterialShapes.Circle.toShape()
        ),
        modifier = modifier
            .size(sizing.circle.dp),
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = animatedContainerColor,
            contentColor = animatedIconColor
        )
    ) {
        SvgIcon(
            resId = when (type) {
                ButtonType.FAULTY -> if (isFaulty) {
                    shared.drawable.information_filled_icon_vector
                } else {
                    shared.drawable.information_icon_vector
                }

                ButtonType.REPORT -> if (isReported) {
                    shared.drawable.megaphone_filled_icon_vector
                } else {
                    shared.drawable.megaphone_icon_vector
                }
            },
            modifier = Modifier.size(sizing.icon.dp),
            color = animatedIconColor
        )
    }
}

enum class ButtonType {
    FAULTY,
    REPORT
}
