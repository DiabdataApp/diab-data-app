package com.diabdata.core.utils.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.diabdata.core.ui.components.cardsList.CardItem
import com.diabdata.core.ui.components.cardsList.CardListItem

/**
 * A composable that renders an icon inside a tinted circular background.
 *
 * The circle's background color is a translucent version of [baseColor] (20% opacity),
 * while the icon itself is rendered in a darkened variant of [baseColor] (darkened by 20%).
 * This creates a soft, cohesive color pairing commonly seen in Material 3 design patterns.
 *
 * ## Visual representation
 * ```
 *     ╭───────────╮
 *     │  ┌─────┐  │  ← Circular background: baseColor at 20% opacity
 *     │  │ SVG │  │  ← Icon: baseColor darkened by 20%
 *     │  └─────┘  │
 *     ╰───────────╯
 * ```
 *
 * ## Default sizes
 * - Circle diameter: **40.dp** (if [size] is `null`)
 * - Icon size: **24.dp** (if [iconSize] is `null`)
 *
 * ## Usage example
 * ```kotlin
 * // With default sizes
 * ColoredIconCircle(
 *     iconRes = R.drawable.ic_sensor,
 *     baseColor = Color(0xFF4CAF50)
 * )
 *
 * // With custom sizes
 * ColoredIconCircle(
 *     iconRes = R.drawable.ic_insulin,
 *     baseColor = Color(0xFFFF9800),
 *     size = 48.dp,
 *     iconSize = 28.dp
 * )
 * ```
 *
 * @param iconRes   Drawable resource ID of the SVG icon to display, rendered via [SvgIcon].
 * @param baseColor The base [Color] used to derive both the circle background (20% opacity)
 *                  and the icon tint (darkened by 20% via [Color.darken]).
 * @param size      Diameter of the circular background. Defaults to **40.dp** if `null`.
 * @param iconSize  Size of the inner icon. Defaults to **24.dp** if `null`.
 *
 * @see ColoredIconCircleProps  Data model used to pass configuration to this composable
 *                              from a [CardItem].
 * @see SvgIcon                 Composable responsible for rendering the SVG resource.
 * @see Color.darken            Extension function used to darken the base color for the icon tint.
 */
@Composable
fun ColoredIconCircle(iconRes: Int, baseColor: Color, size: Dp?, iconSize: Dp?) {
    val size = size ?: 40.dp
    val iconSize = iconSize ?: 24.dp

    Box(
        modifier = Modifier
            .size(size)
            .background(
                color = baseColor.copy(alpha = 0.2f),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        SvgIcon(
            resId = iconRes,
            modifier = Modifier.size(iconSize),
            color = baseColor.darken(0.2f)
        )
    }
}

/**
 * Data model holding the configuration for a [ColoredIconCircle] composable.
 *
 * This class is used as an optional parameter in [CardItem.leadingColoredCircleIcon]
 * to configure a colored circular icon in the leading section of a [CardListItem].
 *
 * ## Usage example
 * ```kotlin
 * val props = ColoredIconCircleProps(
 *     iconRes = R.drawable.ic_sensor,
 *     baseColor = Color(0xFF4CAF50),
 *     size = 48.dp,
 *     iconSize = 28.dp
 * )
 *
 * // Used directly
 * ColoredIconCircle(
 *     iconRes = props.iconRes,
 *     baseColor = props.baseColor,
 *     size = props.size,
 *     iconSize = props.iconSize
 * )
 *
 * // Or passed through a CardItem
 * CardItem(
 *     leadingColoredCircleIcon = props,
 *     content = { Text("Glucose sensor") }
 * )
 * ```
 *
 * @property iconRes   Drawable resource ID of the SVG icon to display.
 * @property baseColor The base [Color] used for the circle background and icon tint.
 * @property size      Diameter of the circular background. `null` defaults to **40.dp**.
 * @property iconSize  Size of the inner icon. `null` defaults to **24.dp**.
 *
 * @see ColoredIconCircle  Composable that consumes this configuration.
 * @see CardItem           Data model where this is used as [CardItem.leadingColoredCircleIcon].
 */
data class ColoredIconCircleProps(
    val iconRes: Int,
    val baseColor: Color,
    val size: Dp?,
    val iconSize: Dp?
)
