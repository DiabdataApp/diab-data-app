package com.diabdata.feature.databaseView

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.diabdata.R
import com.diabdata.core.database.DataViewModel
import com.diabdata.core.database.converters.toEntity
import com.diabdata.core.model.MedicalDevice
import com.diabdata.core.ui.components.actionInput.FlippableSelectionIcon
import com.diabdata.core.utils.ui.ColoredIconCircle
import com.diabdata.core.utils.ui.SvgIcon
import com.diabdata.core.utils.ui.darken
import com.diabdata.core.utils.ui.getItemShape
import com.diabdata.feature.devices.DevicesViewModel
import com.diabdata.shared.R as shared
import com.diabdata.shared.utils.dataTypes.AddableType
import java.time.format.DateTimeFormatter
import kotlin.math.abs
import kotlinx.coroutines.launch

@Composable
fun DatabaseEditionView(dataViewModel: DataViewModel) {
    val scope = rememberCoroutineScope()
    var selectedTypes by remember { mutableStateOf(setOf<AddableType>()) }
    var selectionMode by remember { mutableStateOf(false) }
    var selectedEntries by remember { mutableStateOf(setOf<DataViewModel.MixedDbEntry>()) }
    var selectedEntry by remember { mutableStateOf<DataViewModel.MixedDbEntry?>(null) }

    val mixedEntries by dataViewModel.allMixedEntries.collectAsState(emptyList())
    val devicesViewModel: DevicesViewModel = hiltViewModel()

    val filteredEntries =
        mixedEntries.filter { it.addableType in selectedTypes || selectedTypes.isEmpty() }

    val typesOfSelectedEntries = selectedEntries.map { it.addableType }.toSet()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .padding(horizontal = 20.dp)
        ) {
            FilterChips(
                types = AddableType.entries,
                selectedTypes = selectedTypes,
                onTypeToggle = { type ->
                    selectedTypes = if (selectedTypes.contains(type)) {
                        selectedTypes - type
                    } else {
                        selectedTypes + type
                    }
                }
            )

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AnimatedVisibility(
                    visible = selectionMode && selectedEntries.isNotEmpty(),
                    enter = expandHorizontally() + fadeIn(),
                    exit = shrinkHorizontally() + fadeOut()
                ) {
                    val interactionSource = remember { MutableInteractionSource() }
                    val isPressed by interactionSource.collectIsPressedAsState()
                    val isHovered by interactionSource.collectIsHoveredAsState()

                    val deleteButtonText = stringResource(shared.string.common_delete)
                    val discardedItemText = stringResource(shared.string.common_set_lifespan)

                    val bgColor = if (isPressed || isHovered) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                    }
                    val textColor = if (isPressed || isHovered) {
                        MaterialTheme.colorScheme.onError
                    } else {
                        MaterialTheme.colorScheme.error
                    }
                    val badgeColor = if (isPressed || isHovered) {
                        MaterialTheme.colorScheme.onError
                    } else {
                        MaterialTheme.colorScheme.error
                    }
                    val badgeTextColor = if (isPressed || isHovered) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onError
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                selectedEntries.forEach { dataViewModel.deleteEntry(it) }
                                selectedEntries = emptySet()
                                selectionMode = false
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = bgColor,
                                contentColor = textColor
                            ),
                            interactionSource = interactionSource
                        ) {
                            BadgedBox(
                                badge = {
                                    Badge(containerColor = badgeColor) {
                                        Text(
                                            selectedEntries.size.toString(),
                                            color = badgeTextColor
                                        )
                                    }
                                }
                            ) {
                                SvgIcon(
                                    resId = shared.drawable.delete_icon_vector,
                                    color = textColor
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            Text(deleteButtonText)
                        }

                        if (typesOfSelectedEntries == setOf(AddableType.DEVICE)) {
                            Button(
                                onClick = {
                                    scope.launch {
                                        scope.launch {
                                            val deviceEntries = selectedEntries
                                                .filterIsInstance<DataViewModel.MixedDbEntry.DeviceEntry>()
                                                .map { it.toEntity() as MedicalDevice }

                                            devicesViewModel.setDevicesLifespanOver(
                                                deviceEntries,
                                                isOver = true
                                            )
                                        }
                                    }
                                    selectedEntries = emptySet()
                                    selectionMode = false
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary.copy(
                                        alpha = 0.1f
                                    ),
                                    contentColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                BadgedBox(
                                    badge = {
                                        Badge(
                                            containerColor = MaterialTheme.colorScheme.primary
                                        ) {
                                            Text(selectedEntries.size.toString())
                                        }
                                    }
                                ) {
                                    SvgIcon(
                                        shared.drawable.recycle_icon_vector,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(Modifier.width(8.dp))
                                Text(discardedItemText)
                            }
                        }
                    }
                }

                if (!selectionMode && selectedEntries.isEmpty()) Spacer(Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        val selectingAll = selectedEntries.size < filteredEntries.size
                        selectedEntries = if (selectingAll) filteredEntries.toSet() else emptySet()
                        selectionMode = selectingAll
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                ) {
                    SvgIcon(
                        resId = if (selectedEntries.size < filteredEntries.size) {
                            shared.drawable.select_all_icon_vector
                        } else {
                            shared.drawable.deselect_all_icon_vector
                        },
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            LazyColumn {
                items(
                    items = filteredEntries,
                    key = { entry -> "${entry.addableType}-${entry.id}" }
                ) { entry ->
                    EntryCardSwipeM3(
                        entry = entry,
                        shape = getItemShape(filteredEntries.indexOf(entry), filteredEntries.size),
                        selectionMode = selectionMode,
                        isSelected = selectedEntries.contains(entry),
                        onClick = {
                            if (selectionMode) {
                                selectedEntries = toggleEntrySelection(selectedEntries, entry)
                                if (selectedEntries.isEmpty()) selectionMode = false
                            } else if (selectedEntries.isEmpty()) {
                                selectedEntry = entry
                            }
                        },
                        onLongPress = {
                            selectionMode = true
                            selectedEntries = selectedEntries + entry
                        },
                        onDeleteFromDb = { dataViewModel.deleteEntry(entry) },
                        onArchive = {
                            val archived = entry.isArchived
                            dataViewModel.setArchived(
                                entry = entry,
                                archived = !archived
                            )
                        }
                    )
                    Spacer(Modifier.height(4.dp))
                }
            }
        }
        rememberCoroutineScope()

        selectedEntry?.let { entry ->
            EditEntryDialog(
                entry = entry,
                onDismiss = { selectedEntry = null },
                dataViewModel = dataViewModel
            )
        }
    }
}

@Composable
fun EntryCardSwipeM3(
    entry: DataViewModel.MixedDbEntry,
    modifier: Modifier = Modifier,
    shape: Shape,
    selectionMode: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    onDeleteFromDb: () -> Unit,
    onArchive: () -> Unit,
    swipeThreshold: Dp = 100.dp
) {
    val scope = rememberCoroutineScope()
    val thresholdPx = with(LocalDensity.current) { swipeThreshold.toPx() }

    val dragOffset = remember { Animatable(0f) }

    val currentOnDelete by rememberUpdatedState(onDeleteFromDb)
    val currentOnArchive by rememberUpdatedState(onArchive)

    val archiveResId =
        if (entry.isArchived) shared.drawable.unarchive_icon_vector else shared.drawable.archive_icon_vector
    val archivedCardBgColor = when {
        selectionMode && isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
        entry.isArchived -> colorResource(R.color.archived_washed)
        else -> MaterialTheme.colorScheme.surface
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        val progress = (abs(dragOffset.value) / thresholdPx).coerceIn(0f, 1f)

        if (dragOffset.value > 0f) {
            Surface(
                shape = shape,
                color = MaterialTheme.colorScheme.error.copy(alpha = 0.2f + 0.8f * progress),
                modifier = Modifier.fillMaxSize()
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.CenterStart) {
                    SvgIcon(
                        resId = shared.drawable.delete_icon_vector,
                        modifier = Modifier
                            .size(32.dp)
                            .padding(start = 16.dp)
                            .alpha(progress),
                        color = MaterialTheme.colorScheme.onError
                    )
                }
            }
        } else if (dragOffset.value < 0f) {
            Surface(
                shape = shape,
                color = colorResource(
                    R.color.archived_primary
                ).copy(alpha = 0.2f + 0.8f * progress),
                modifier = Modifier.fillMaxSize()
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.CenterEnd) {
                    SvgIcon(
                        resId = archiveResId,
                        modifier = Modifier
                            .size(32.dp)
                            .padding(end = 16.dp)
                            .alpha(progress),
                        color = colorResource(R.color.on_archived_primary)
                    )
                }
            }
        }

        Surface(
            shape = shape,
            color = archivedCardBgColor,
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    translationX = dragOffset.value
                }
                .pointerInput(entry.id) {
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { _, delta ->
                            scope.launch { dragOffset.snapTo(dragOffset.value + delta) }
                        },
                        onDragEnd = {
                            scope.launch {
                                when {
                                    dragOffset.value > thresholdPx -> {
                                        currentOnDelete()
                                        dragOffset.animateTo(0f, tween(200))
                                    }

                                    dragOffset.value < -thresholdPx -> {
                                        currentOnArchive()
                                        dragOffset.animateTo(
                                            0f,
                                            spring(stiffness = Spring.StiffnessMedium)
                                        )
                                    }

                                    else -> dragOffset.animateTo(
                                        0f,
                                        spring(stiffness = Spring.StiffnessMedium)
                                    )
                                }
                            }
                        }
                    )
                }
                .combinedClickable(onClick = onClick, onLongClick = onLongPress)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(16.dp)
            ) {
                val color = if (entry.isArchived) {
                    colorResource(R.color.archived_primary)
                } else {
                    MaterialTheme.colorScheme.primary
                }

                Box(contentAlignment = Alignment.Center) {
                    ColoredIconCircle(
                        iconRes = entry.icon,
                        baseColor = if (entry.isArchived) color else entry.addableType.baseColor,
                        size = 40.dp,
                        iconSize = 25.dp
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    EntryContent(entry)
                }
                FlippableSelectionIcon(isSelected)
            }
        }
    }
}

@Composable
private fun EntryContent(entry: DataViewModel.MixedDbEntry) {
    val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy")

    when (entry) {
        is DataViewModel.MixedDbEntry.AppointmentEntry -> {
            Text(
                entry.doctor,
                fontWeight = FontWeight.Bold,
                color = entry.addableType.baseColor.darken()
            )
            Text(entry.date.format(formatter), style = MaterialTheme.typography.bodySmall)
            Text(
                stringResource(entry.type.displayNameRes),
                style = MaterialTheme.typography.bodySmall
            )
            entry.notes?.takeIf { it.isNotBlank() }?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        is DataViewModel.MixedDbEntry.ImportantDateEntry -> {
            Text(
                entry.importantDate,
                fontWeight = FontWeight.Bold,
                color = entry.addableType.baseColor.darken()
            )
            Text(entry.date.format(formatter), style = MaterialTheme.typography.bodySmall)
        }

        is DataViewModel.MixedDbEntry.Hba1cEntry -> {
            Text(
                "${entry.value} %",
                fontWeight = FontWeight.Bold,
                color = entry.addableType.baseColor.darken()
            )
            Text(entry.date.format(formatter), style = MaterialTheme.typography.bodySmall)
        }

        is DataViewModel.MixedDbEntry.TreatmentEntry -> {
            Text(
                entry.name,
                fontWeight = FontWeight.Bold,
                color = entry.addableType.baseColor.darken()
            )
            Text(entry.date.format(formatter), style = MaterialTheme.typography.bodySmall)
            Text(
                stringResource(entry.treatmentType.displayNameRes),
                style = MaterialTheme.typography.bodySmall
            )
        }

        is DataViewModel.MixedDbEntry.WeightEntry -> {
            Text(
                "${entry.value} kg",
                fontWeight = FontWeight.Bold,
                color = entry.addableType.baseColor.darken()
            )
            Text(entry.date.format(formatter), style = MaterialTheme.typography.bodySmall)
        }

        is DataViewModel.MixedDbEntry.DeviceEntry -> {
            val numbersWithIcons = listOfNotNull(
                entry.batchNumber.takeIf { it.isNotBlank() }?.let {
                    shared.drawable.lot_icon_vector to it
                },
                entry.referenceNumber.takeIf { it.isNotBlank() }?.let {
                    shared.drawable.ref_icon_vector to it
                },
                entry.serialNumber.takeIf { it.isNotBlank() }?.let {
                    shared.drawable.sn_icon_vector to it
                }
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (entry.isLifeSpanOver) {
                        SvgIcon(
                            resId = shared.drawable.recycle_icon_vector,
                            modifier = Modifier.size(16.dp),
                            color = entry.addableType.baseColor.darken()
                        )
                    }
                    Text(
                        entry.name,
                        fontWeight = FontWeight.Bold,
                        color = entry.addableType.baseColor.darken()
                    )
                }

                if (numbersWithIcons.isNotEmpty()) {
                    FlowRow(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        numbersWithIcons.forEach { (iconRes, number) ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                SvgIcon(
                                    resId = iconRes,
                                    modifier = Modifier.size(16.dp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = number,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        entry.date.format(formatter),
                        style = MaterialTheme.typography.bodySmall
                    )

                    if (entry.isFaulty) {
                        SvgIcon(
                            resId = shared.drawable.faulty_medical_device_icon_vector,
                            modifier = Modifier.size(12.dp),
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    if (entry.isReported) {
                        SvgIcon(
                            resId = shared.drawable.megaphone_filled_icon_vector,
                            modifier = Modifier.size(12.dp),
                            color = entry.addableType.baseColor.darken()
                        )
                    }
                }
            }
        }
    }
}

fun toggleEntrySelection(
    current: Set<DataViewModel.MixedDbEntry>,
    entry: DataViewModel.MixedDbEntry
): Set<DataViewModel.MixedDbEntry> =
    if (current.contains(entry)) current - entry else current + entry

@Composable
fun FilterChips(
    types: List<AddableType>,
    selectedTypes: Set<AddableType>,
    onTypeToggle: (AddableType) -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        types.forEach { type ->
            val isSelected = type in selectedTypes

            FilterChip(
                selected = isSelected,
                onClick = { onTypeToggle(type) },
                label = { Text(type.getDisplayName(context)) },
                leadingIcon = if (isSelected) {
                    { Icon(Icons.Outlined.Check, contentDescription = null) }
                } else {
                    null
                }
            )
        }
    }
}
