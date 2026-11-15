package com.diabdata.feature.devices.ui

import android.os.Build
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.diabdata.core.database.DataViewModel
import com.diabdata.core.utils.ui.SvgIcon
import com.diabdata.feature.devices.ui.devicesScreens.FaultyDevices
import com.diabdata.feature.devices.ui.devicesScreens.RecentDevicesScreen
import com.diabdata.shared.R as shared

enum class DeviceDestination(
    @param:StringRes val labelRes: Int,
    val route: String,
    @param:DrawableRes val iconRes: Int,
    @param:DrawableRes val iconResFilled: Int
) {
    OVERVIEW(
        shared.string.devices_device_screen_overview_tab,
        route = "devices",
        iconRes = shared.drawable.list_icon_vector,
        iconResFilled = shared.drawable.list_filled_icon_vector
    ),
    FAULTY(
        shared.string.devices_device_screen_faulty_devices_tab,
        route = "other",
        iconRes = shared.drawable.faulty_medical_device_icon_vector,
        iconResFilled = shared.drawable.faulty_medical_device_filled_icon_vector
    )
}

@Suppress("unused")
@RequiresApi(Build.VERSION_CODES.P)
@Composable
fun DevicesScreen(dataViewModel: DataViewModel, modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val startDestination = DeviceDestination.OVERVIEW

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val selectedDestination =
        DeviceDestination.entries.indexOfFirst { it.route == currentRoute }.takeIf { it != -1 } ?: 0

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        SecondaryTabRow(
            selectedTabIndex = selectedDestination,
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            modifier = Modifier.fillMaxWidth()
        ) {
            DeviceDestination.entries.forEachIndexed { index, destination ->
                Tab(
                    selected = selectedDestination == index,
                    onClick = {
                        navController.navigate(destination.route) {
                            popUpTo(startDestination.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    selectedContentColor = MaterialTheme.colorScheme.primary,
                    unselectedContentColor = MaterialTheme.colorScheme.onSurface,
                    text = {
                        Text(
                            text = stringResource(id = destination.labelRes),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    icon = {
                        if (selectedDestination == index) {
                            SvgIcon(
                                destination.iconResFilled,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            SvgIcon(
                                destination.iconRes,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                )
            }
        }

        NavHost(
            navController = navController,
            startDestination = startDestination.route,
            modifier = Modifier.fillMaxSize(),
            enterTransition = {
                slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300)) + fadeIn(
                    animationSpec = tween(300)
                )
            },
            exitTransition = {
                slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(300)) + fadeOut(
                    animationSpec = tween(300)
                )
            },
            popEnterTransition = {
                slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween(300)) + fadeIn(
                    animationSpec = tween(300)
                )
            },
            popExitTransition = {
                slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(300)) + fadeOut(
                    animationSpec = tween(300)
                )
            }
        ) {
            composable(DeviceDestination.OVERVIEW.route) {
                RecentDevicesScreen(dataViewModel = dataViewModel)
            }
            composable(DeviceDestination.FAULTY.route) {
                FaultyDevices(dataViewModel)
            }
        }
    }
}
