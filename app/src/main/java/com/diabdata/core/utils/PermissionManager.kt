package com.diabdata.core.utils

import android.Manifest
import android.os.Build

object PermissionManager {

    fun getRequiredPermissions(): List<String> {
        val permissions = mutableListOf<String>()

        // Notifications (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        // Camera for QR code scanning (Android 13+)
        permissions.add(Manifest.permission.CAMERA)

        return permissions
    }
}
