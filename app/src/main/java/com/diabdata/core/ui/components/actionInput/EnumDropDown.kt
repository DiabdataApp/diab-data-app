package com.diabdata.core.ui.components.actionInput

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.diabdata.core.utils.ui.SvgIcon
import com.diabdata.shared.R as shared

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun <T> EnumDropdown(
    label: String,
    options: List<T>,
    selected: T?,
    displayName: (T) -> String,
    iconRes: ((T) -> Int?)? = null,
    onSelectedChange: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }

    val isSearchable = options.size >= 5

    LaunchedEffect(selected) {
        if (isSearchable && selected != null) {
            query = displayName(selected)
        }
    }

    val filteredOptions = if (isSearchable) {
        options.filter { displayName(it).contains(query, ignoreCase = true) }
    } else {
        options
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = if (isSearchable) query else selected?.let { displayName(it) } ?: "",
            onValueChange = { newValue ->
                if (isSearchable) {
                    query = newValue
                    expanded = true
                }
            },
            leadingIcon = {
                val selectedIconRes = selected?.let { iconRes?.invoke(it) }
                if (selectedIconRes != null) {
                    SvgIcon(
                        resId = selectedIconRes,
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                } else if (isSearchable) {
                    IconButton(onClick = { query = "" }) {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = stringResource(shared.string.popup_placeholder_search)
                        )
                    }
                }
            },
            label = { Text(label) },
            placeholder = { if (isSearchable) Text(stringResource(shared.string.popup_placeholder_search)) },
            readOnly = !isSearchable,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(
                    type = if (isSearchable)
                        ExposedDropdownMenuAnchorType.PrimaryEditable
                    else
                        ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                    enabled = true
                )
                .fillMaxWidth(),
            shape = MaterialTheme.shapes.small,
            singleLine = true
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            filteredOptions.forEach { option ->
                DropdownMenuItem(
                    leadingIcon = {
                        if (option == selected) {
                            Icon(
                                imageVector = Icons.Outlined.Check,
                                contentDescription = null
                            )
                        } else {
                            iconRes?.invoke(option)?.let { res ->
                                SvgIcon(
                                    resId = res,
                                    modifier = Modifier.size(24.dp),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    },
                    text = { Text(displayName(option)) },
                    onClick = {
                        onSelectedChange(option)
                        query = displayName(option)
                        expanded = false
                    }
                )
            }
        }
    }
}