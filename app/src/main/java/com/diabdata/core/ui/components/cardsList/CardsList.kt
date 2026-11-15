package com.diabdata.core.ui.components.cardsList

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.diabdata.core.utils.ui.getItemShape

/**
 * A composable that displays a Material 3 / Google-style stacked card list
 * with a header, contextual rounded corners, and optional pagination.
 *
 * ## Rounded corners behavior
 * - **Single card**: all corners are rounded (20.dp).
 * - **Multiple cards**: the first card has its top corners rounded,
 *   the last card has its bottom corners rounded, and intermediate cards
 *   have minimal rounding (3.dp).
 *
 * ## Pagination
 * - If [pageSize] is `null` or greater than the number of cards, all cards are displayed at once.
 * - If [pageSize] is set and less than the number of cards, cards are displayed
 *   in groups of [pageSize] with navigation chevrons (previous / next).
 * - Rounded corners are dynamically recalculated based on the currently visible group.
 *
 * ## Usage example
 * ```kotlin
 * // Simple stack without pagination
 * CardsList(
 *     header = "Settings",
 *     cards = listOf(
 *         CardItem(
 *             leadingIcon = Icons.Default.Notifications,
 *             content = { Text("Notifications") },
 *             switchState = true,
 *             onSwitchChange = { /* ... */ }
 *         ),
 *         CardItem(
 *             leadingIcon = Icons.Default.Info,
 *             content = { Text("Version 1.0.0") }
 *         )
 *     )
 * )
 *
 * // Paginated stack with groups of 3
 * CardsList(
 *     header = "HBA1C History",
 *     pageSize = 3,
 *     cards = listOf(
 *         CardItem(content = { Text("January 2025 — 6.8%") }),
 *         CardItem(content = { Text("October 2024 — 7.1%") }),
 *         CardItem(content = { Text("July 2024 — 7.3%") }),
 *         CardItem(content = { Text("April 2024 — 6.9%") }),
 *         CardItem(content = { Text("January 2024 — 7.0%") })
 *     )
 * )
 * ```
 *
 * @param header    Title displayed above the card stack,
 *                  rendered using [MaterialTheme.typography.titleLarge].
 * @param cards     List of [CardItem] representing the content of each card.
 *                  Each [CardItem] can contain a leading icon, a composable content,
 *                  a trailing icon, and/or a switch. See [CardItem] for details.
 * @param pageSize  Maximum number of cards visible per page.
 *                  - `null` (default): all cards are displayed without pagination.
 *                  - Positive value: enables pagination with navigation controls.
 * @param modifier  [Modifier] applied to the root container of the component.
 *
 * @see CardItem      Data model describing the content of a single card.
 * @see CardListItem  Composable responsible for rendering an individual card.
 * @see getItemShape  Utility that computes corner shapes based on position within the stack.
 */
@Composable
fun CardsList(
    modifier: Modifier = Modifier,
    header: String? = null,
    cards: List<CardItem>,
    pageSize: Int? = null
) {
    var currentPage by remember { mutableIntStateOf(0) }
    val isPaginated = pageSize != null && pageSize > 0 && cards.size > pageSize
    val totalPages = if (isPaginated) {
        (cards.size + pageSize - 1) / pageSize
    } else {
        1
    }

    val visibleCards = if (isPaginated) {
        val start = currentPage * pageSize
        val end = (start + pageSize).coerceAtMost(cards.size)
        cards.subList(start, end)
    } else {
        cards
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        if (header != null) {
            Text(
                text = header,
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.surfaceTint,
                modifier = Modifier.padding(start = 0.dp, bottom = 8.dp, top = 8.dp)
            )
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            visibleCards.forEachIndexed { index, cardItem ->
                CardListItem(
                    cardItem = cardItem,
                    shape = getItemShape(index, visibleCards.size)
                )
            }
        }

        if (isPaginated) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { currentPage-- },
                    enabled = currentPage > 0
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Page précédente"
                    )
                }

                Text(
                    text = "${currentPage + 1} / $totalPages",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                IconButton(
                    onClick = { currentPage++ },
                    enabled = currentPage < totalPages - 1
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Page suivante"
                    )
                }
            }
        }
    }
}
