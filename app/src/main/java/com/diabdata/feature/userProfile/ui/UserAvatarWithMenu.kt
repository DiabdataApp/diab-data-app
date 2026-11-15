package com.diabdata.feature.userProfile.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun UserAvatarWithMenu(
    firstName: String?,
    lastName: String?,
    profilePhotoPath: String?,
    onEditProfile: () -> Unit,
    size: Dp = 35.dp
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Box {
        UserAvatar(
            firstName = firstName,
            lastName = lastName,
            profilePhotoPath = profilePhotoPath,
            onClick = { menuExpanded = true },
            size = size
        )
        UserAvatarMenu(
            expanded = menuExpanded,
            onDismiss = { menuExpanded = false },
            onEditProfile = onEditProfile
        )
    }
}

@Preview(showBackground = true, name = "Avatar basic")
@Composable
fun UserAvatarWithMenuPreview() {
    UserAvatarWithMenu(
        firstName = "John",
        lastName = "Doe",
        profilePhotoPath = null,
        onEditProfile = {}
    )
}
