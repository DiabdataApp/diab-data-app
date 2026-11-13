package com.diabdata.feature.importantDates.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.diabdata.core.database.DataViewModel
import com.diabdata.core.database.converters.toEntity
import com.diabdata.core.model.ImportantDate
import com.diabdata.core.model.newEntryUuid
import com.diabdata.core.model.nowMillis
import com.diabdata.shared.utils.dataTypes.AddableType
import com.diabdata.core.ui.components.addDataPopup.BasePopupLayout
import com.diabdata.core.ui.components.date_components.DateSelector
import kotlinx.coroutines.launch
import java.time.LocalDate
import com.diabdata.shared.R as shared

@Composable
fun ImportantDatePopup(
    onDismiss: () -> Unit,
    dataViewModel: DataViewModel,
    toUpdate: DataViewModel.MixedDbEntry.ImportantDateEntry? = null
) {
    val context = LocalContext.current
    val today = LocalDate.now()
    val scope = rememberCoroutineScope()

    var diagnosis by remember { mutableStateOf(toUpdate?.importantDate ?: "") }
    var selectedDate by remember {
        mutableStateOf(
            toUpdate?.date?.toLocalDate() ?: LocalDate.now()
        )
    }

    BasePopupLayout(
        title = stringResource(
            if (toUpdate == null) shared.string.popup_title_add else shared.string.popup_title_update,
            AddableType.IMPORTANT_DATE.getDisplayName(context)
        ),
        icon = AddableType.IMPORTANT_DATE.iconRes,
        onDismiss = onDismiss,
        onConfirm = {
            val now = nowMillis()
            val entry = DataViewModel.MixedDbEntry.ImportantDateEntry(
                id = toUpdate?.id ?: 0,
                date = selectedDate.atStartOfDay(),
                createdAt = toUpdate?.createdAt ?: now,
                isArchived = toUpdate?.isArchived ?: false,
                importantDate = diagnosis,
                updatedAt = now,
                addableType = AddableType.IMPORTANT_DATE,
                icon = AddableType.IMPORTANT_DATE.iconRes,
                entryUuid = toUpdate?.entryUuid ?: newEntryUuid()
            )

            if (toUpdate == null) {
                dataViewModel.addImportantDate(entry.toEntity() as ImportantDate)
            } else {
                scope.launch {
                    dataViewModel.updateEntry(entry)
                }
            }
            onDismiss()
        },
        isConfirmEnabled = diagnosis.isNotBlank()
    ) {
        DateSelector(
            date = selectedDate,
            onDateSelected = { selectedDate = it }
        )

        OutlinedTextField(
            value = diagnosis,
            onValueChange = { diagnosis = it },
            label = { Text(stringResource(shared.string.popup_placeholder_event)) },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.small
        )
    }
}