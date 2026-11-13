package com.diabdata.feature.userProfile.ui

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenuGroup
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenuPopup
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.diabdata.core.utils.ui.SvgIcon
import com.diabdata.feature.casting.relay.ShareMode
import com.diabdata.feature.casting.relay.ui.ShareDialog
import com.diabdata.shared.R

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun UserAvatarMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onEditProfile: () -> Unit
) {
    var showShareDialog by remember { mutableStateOf<ShareMode?>(null) }
    val groupInteractionSource = remember { MutableInteractionSource() }

    showShareDialog?.let { mode ->
        ShareDialog(
            mode = mode,
            onDismiss = { showShareDialog = null }
        )
    }

    DropdownMenuPopup(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = Modifier
            .padding(top = 4.dp)
    ) {
        // --- Group 1 : Edit profile (standalone) ---
        DropdownMenuGroup(
            shapes = MenuDefaults.groupShape(index = 0, count = 2),
            interactionSource = groupInteractionSource,
            containerColor = MenuDefaults.groupStandardContainerColor
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.edit_profile)) },
                leadingIcon = {
                    SvgIcon(
                        resId = R.drawable.edit_user_icon_vector,
                        modifier = Modifier.size(MenuDefaults.LeadingIconSize),
                        color = MenuDefaults.selectableItemColors().leadingIconColor
                    )
                },
                onClick = {
                    onDismiss()
                    onEditProfile()
                },
            )
        }

        Spacer(Modifier.height(MenuDefaults.GroupSpacing))

        // --- Group 2 : Sharing modes ---
        DropdownMenuGroup(
            shapes = MenuDefaults.groupShape(index = 1, count = 2),
            interactionSource = groupInteractionSource,
            containerColor = MenuDefaults.groupStandardContainerColor
        ) {
            // Companion mode
            DropdownMenuItem(
                text = { Text(stringResource(R.string.cast_to_user_computer)) },
                leadingIcon = {
                    SvgIcon(
                        resId = R.drawable.computer_arrow_up_icon_vector,
                        modifier = Modifier.size(MenuDefaults.LeadingIconSize),
                        color = MenuDefaults.selectableItemColors().leadingIconColor
                    )
                },
                onClick = {
                    onDismiss()
                    showShareDialog = ShareMode.COMPANION
                },
            )
            // Doctor mode
            DropdownMenuItem(
                text = { Text(stringResource(R.string.cast_to_doctors_computer)) },
                leadingIcon = {
                    SvgIcon(
                        resId = R.drawable.secure_cast_to_desktop_icon_vector,
                        modifier = Modifier.size(MenuDefaults.LeadingIconSize),
                        color = MenuDefaults.selectableItemColors().leadingIconColor
                    )
                },
                onClick = {
                    onDismiss()
                    showShareDialog = ShareMode.MEDICAL
                },
            )
        }
    }
}