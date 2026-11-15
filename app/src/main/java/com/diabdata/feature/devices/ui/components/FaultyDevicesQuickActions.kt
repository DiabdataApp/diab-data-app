package com.diabdata.feature.devices.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.diabdata.shared.R as shared

data class QuickAction(
    val icon: Int,
    val filledIcon: Int,
    val label: String,
    val onClick: () -> Unit = {}
)

@Composable
fun QuickActionButtons(
    actions: List<QuickAction>,
    color: Color = MaterialTheme.colorScheme.primary
) {
    var selectedIndex by rememberSaveable { mutableIntStateOf(-1) }

    Row(modifier = Modifier.fillMaxWidth()) {
        actions.forEachIndexed { index, action ->
            val isSelected = index == selectedIndex

            Button(
                onClick = {
                    selectedIndex = index
                    action.onClick()
                    selectedIndex = -1
                },
                modifier = Modifier
                    .padding(4.dp)
                    .weight(1f),
                colors = ButtonDefaults.buttonColors().copy(
                    containerColor = color
                )
            ) {
                Icon(
                    painter = painterResource(if (isSelected) action.filledIcon else action.icon),
                    contentDescription = action.label
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(action.label)
            }
        }
    }
}

@Preview
@Composable
fun QuickActionButtonsPreview() {
    val actions = listOf(
        QuickAction(
            icon = shared.drawable.settings_icon_vector,
            filledIcon = shared.drawable.settings_filled_icon_vector,
            label = "Settings"
        ),
        QuickAction(
            icon = shared.drawable.faulty_medical_device_icon_vector,
            filledIcon = shared.drawable.faulty_medical_device_filled_icon_vector,
            label = "Faulty devices"
        ),
        QuickAction(
            icon = shared.drawable.information_icon_vector,
            filledIcon = shared.drawable.information_filled_icon_vector,
            label = "Information"
        )
    )

    QuickActionButtons(
        actions = actions,
        color = MaterialTheme.colorScheme.error
    )
}
