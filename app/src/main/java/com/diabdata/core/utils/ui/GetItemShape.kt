package com.diabdata.core.utils.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.diabdata.core.ui.components.cardsList.CardListItem
import com.diabdata.core.ui.components.cardsList.CardsList

/**
 * Computes the [Shape] for a card based on its position within a stacked card list.
 *
 * This utility produces a Google / Material 3 style stacking effect where the first
 * and last cards have pronounced rounded corners, while intermediate cards have
 * minimal rounding to create a visually cohesive group.
 *
 * ## Corner rounding rules
 * ```
 * Single card (size == 1):
 * ╭────────────────╮  ← all corners 15.dp
 * │                │
 * ╰────────────────╯
 *
 * First card (index == 0):
 * ╭────────────────╮  ← top corners 15.dp
 * │                │
 * ┗────────────────┛  ← bottom corners 3.dp
 *
 * Middle card:
 * ┏────────────────┓  ← top corners 3.dp
 * │                │
 * ┗────────────────┛  ← bottom corners 3.dp
 *
 * Last card (index == size - 1):
 * ┏────────────────┓  ← top corners 3.dp
 * │                │
 * ╰────────────────╯  ← bottom corners 15.dp
 * ```
 *
 * ## Usage example
 * ```kotlin
 * val cards = listOf("Card A", "Card B", "Card C")
 *
 * cards.forEachIndexed { index, label ->
 *     Card(shape = getItemShape(index, cards.size)) {
 *         Text(label)
 *     }
 * }
 * ```
 *
 * @param index  Zero-based position of the card within the list.
 * @param size   Total number of cards in the list. Must be greater than 0.
 * @return A [RoundedCornerShape] with corners adjusted to the card's position.
 *
 * @see CardListItem  Composable that uses this function to shape individual cards.
 * @see CardsList     Parent composable orchestrating the full card stack.
 */
fun getItemShape(index: Int, size: Int): Shape {
    if (size == 1) {
        return RoundedCornerShape(15.dp)
    }
    return when (index) {
        0 -> RoundedCornerShape(
            topStart = 15.dp,
            topEnd = 15.dp,
            bottomStart = 3.dp,
            bottomEnd = 3.dp
        )
        size - 1 -> RoundedCornerShape(
            topStart = 3.dp,
            topEnd = 3.dp,
            bottomStart = 15.dp,
            bottomEnd = 15.dp
        )

        else -> RoundedCornerShape(3.dp)
    }
}
