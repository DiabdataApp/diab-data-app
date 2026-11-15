package com.diabdata.core.ui.components.actionInput

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.diabdata.shared.R as shared
import com.diabdata.shared.utils.dataTypes.AddableType

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AddDataFab(onSelect: (AddableType) -> Unit, onScanClick: () -> Unit) {
    var fabMenuExpanded by rememberSaveable { mutableStateOf(false) }

    BackHandler(fabMenuExpanded) { fabMenuExpanded = false }

    val fabItems = listOf(
        Triple(
            stringResource(shared.string.medications_general_label_scan),
            shared.drawable.data_matrix_icon_vector
        ) { onScanClick() },
        Triple(
            stringResource(shared.string.weights_weight_label),
            shared.drawable.weight_add_icon_vector
        ) { onSelect(AddableType.WEIGHT) },
        Triple(stringResource(shared.string.hba1c_label), shared.drawable.hba1c_add_icon_vector) {
            onSelect(
                AddableType.HBA1C
            )
        },
        Triple(
            stringResource(shared.string.addable_important_date),
            shared.drawable.important_date_add_icon_vector
        ) { onSelect(AddableType.IMPORTANT_DATE) },
        Triple(
            stringResource(shared.string.medications_general_label),
            shared.drawable.medication_add_icon_vector
        ) { onSelect(AddableType.TREATMENT) },
        Triple(
            stringResource(shared.string.appointments_general_label),
            shared.drawable.event_add_icon_vector
        ) { onSelect(AddableType.APPOINTMENT) }
    )

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomEnd) {
        if (fabMenuExpanded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceDim.copy(alpha = 0.95f))
                    .clickable { fabMenuExpanded = false }
            )
        }

        FloatingActionButtonMenu(
            expanded = fabMenuExpanded,
            modifier = Modifier,
            button = {
                ToggleFloatingActionButton(
                    checked = fabMenuExpanded,
                    onCheckedChange = { fabMenuExpanded = !fabMenuExpanded }
                ) {
                    val imageVector by remember {
                        derivedStateOf {
                            if (fabMenuExpanded) Icons.Filled.Close else Icons.Filled.Add
                        }
                    }

                    Icon(
                        imageVector = imageVector,
                        tint = if (fabMenuExpanded) MaterialTheme.colorScheme.onPrimary else Color.Unspecified,
                        contentDescription = if (fabMenuExpanded) "Fermer menu" else "Ouvrir menu"
                    )
                }
            }
        ) {
            fabItems.forEach { (label, iconRes, action) ->
                FloatingActionButtonMenuItem(
                    onClick = {
                        fabMenuExpanded = false
                        action()
                    },
                    text = { Text(label) },
                    icon = {
                        Icon(
                            painter = painterResource(id = iconRes),
                            contentDescription = null
                        )
                    }
                )
            }
        }
    }
}
