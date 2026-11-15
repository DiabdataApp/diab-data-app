package com.diabdata

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.diabdata.core.database.DataViewModel
import com.diabdata.core.ui.theme.DiabDataTheme
import com.diabdata.core.utils.PermissionManager
import dagger.hilt.android.AndroidEntryPoint
import kotlin.getValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val dataViewModel: DataViewModel by viewModels()

    private val _shortcutDestination = MutableStateFlow<String?>(null)
    val shortcutDestination: StateFlow<String?> = _shortcutDestination.asStateFlow()

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handleShortcutIntent(intent)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val darkTheme = isSystemInDarkTheme()
            val controller = WindowInsetsControllerCompat(window, window.decorView)
            controller.isAppearanceLightStatusBars = !darkTheme

            DiabDataTheme {
                RequestPermissionsOnLaunch()
                App(dataViewModel, shortcutDestination)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleShortcutIntent(intent)
    }

    private fun handleShortcutIntent(intent: Intent?) {
        val destination = intent?.getStringExtra("shortcut_destination")
        if (destination != null) {
            _shortcutDestination.value = destination
        }
    }

    fun consumeShortcut() {
        _shortcutDestination.value = null
        intent?.removeExtra("shortcut_destination")
    }

    @Composable
    fun RequestPermissionsOnLaunch() {
        val permissions = remember { PermissionManager.getRequiredPermissions() }

        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions()
        ) { results ->
            results.forEach { (permission, granted) ->
                Log.d("Permissions", "$permission: ${if (granted) "✅" else "❌"}")
            }
        }

        LaunchedEffect(Unit) {
            val context =
                launcher.launch(permissions.toTypedArray())
        }
    }
}
