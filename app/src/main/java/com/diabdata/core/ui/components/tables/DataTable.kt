package com.diabdata.core.ui.components.tables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * Data table companion
 *
 * Data table companion object to build decoration props to be used by
 * data table component DataTable.
 *
 * @param showInnerBorder A boolean to indicate whether to have the border between columns displayed
 * @param alternateRowBackground a boolean to indicate whether to have the rows have an alternating background color
 */
@Immutable
data class DataTableDecoration(
    val showInnerBorder: Boolean = false,
    val alternateRowBackground: Boolean = false
) {
    companion object {
        val Default = DataTableDecoration()

        fun build(builder: Builder.() -> Unit): DataTableDecoration =
            Builder().apply(builder).build()
    }

    class Builder {
        private var showInnerBorder: Boolean = false
        private var alternateRowBackground: Boolean = false

        fun showInnerBorder(value: Boolean) = apply { showInnerBorder = value }
        fun alternateRowBackground(value: Boolean) = apply { alternateRowBackground = value }

        fun build() = DataTableDecoration(
            showInnerBorder = showInnerBorder,
            alternateRowBackground = alternateRowBackground
        )
    }
}

/**
 * A Material 3 expressive inspired table component.
 *
 * This component accepts any type of data to display them in a Material 3
 * expressive inspired table.
 *
 * @param headerColor The color of the header row
 * @param headers The headers of the table
 * @param data The data to display in the table
 * @param modifier The modifier to apply to the table
 * @param decoration The decoration to apply to the table
 */

@Composable
fun DataTable(
    headerColor: Color,
    headers: List<String>,
    data: List<List<String>>,
    modifier: Modifier = Modifier,
    decoration: DataTableDecoration = DataTableDecoration.Default
) {
    val showColumnBorders = decoration.showInnerBorder
    val useAlternateRows = decoration.alternateRowBackground
    val headerTextColor = contentColorFor(backgroundColor = headerColor)

    Surface(
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 2.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(headerColor)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                headers.forEachIndexed { index, title ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(
                                start = if (index > 0 && showColumnBorders) 8.dp else 0.dp,
                                end = if (index < headers.lastIndex &&
                                    showColumnBorders
                                ) {
                                    8.dp
                                } else {
                                    0.dp
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = headerTextColor,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    if (showColumnBorders && index < headers.lastIndex) {
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(24.dp)
                                .background(Color.White.copy(alpha = 0.3f))
                        )
                    }
                }
            }

            data.forEachIndexed { rowIndex, rowValues ->
                val isEven = rowIndex % 2 == 0
                val backgroundColor =
                    if (useAlternateRows && !isEven) {
                        headerColor.copy(alpha = 0.08f)
                    } else {
                        Color.Transparent
                    }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(backgroundColor)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    rowValues.forEachIndexed { colIndex, cell ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(
                                    start = if (colIndex > 0 && showColumnBorders) 8.dp else 0.dp,
                                    end = if (colIndex < rowValues.lastIndex &&
                                        showColumnBorders
                                    ) {
                                        8.dp
                                    } else {
                                        0.dp
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = cell,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }

                        if (showColumnBorders && colIndex < rowValues.lastIndex) {
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(24.dp)
                                    .background(headerColor.copy(alpha = 0.3f))
                            )
                        }
                    }
                }

                if (rowIndex < data.lastIndex) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(headerColor.copy(alpha = 0.25f))
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DataTablePreview_MultiColumns() {
    MaterialTheme {
        DataTable(
            headerColor = MaterialTheme.colorScheme.primary,
            headers = listOf("Nom", "Valeur", "Date"),
            data = listOf(
                listOf("HbA1c", "6.5%", "01/10/2025"),
                listOf("Poids", "72 kg", "15/09/2025"),
                listOf("IMC", "23.1", "15/09/2025")
            ),
            decoration = DataTableDecoration.build {
                showInnerBorder(true)
                alternateRowBackground(true)
            }
        )
    }
}

@Preview(showBackground = true, name = "Avec bordures")
@Composable
fun DataTablePreview_WithBorders() {
    MaterialTheme {
        DataTable(
            headerColor = MaterialTheme.colorScheme.primary,
            headers = listOf("Nom", "Valeur"),
            data = listOf(
                listOf("HbA1c", "6.5%"),
                listOf("Poids", "72 kg"),
                listOf("IMC", "23.1")
            ),
            decoration = DataTableDecoration.build {
                showInnerBorder(false)
                alternateRowBackground(false)
            }
        )
    }
}
