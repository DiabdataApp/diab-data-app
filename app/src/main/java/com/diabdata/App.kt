package com.diabdata

import android.os.Build
import androidx.activity.compose.LocalActivity
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.diabdata.core.database.DataViewModel
import com.diabdata.core.ui.LocalSnackbarHostState
import com.diabdata.core.ui.theme.GoogleSansFlexFontFamily
import com.diabdata.core.utils.ScreenSize
import com.diabdata.core.utils.getScreenSize
import com.diabdata.core.utils.ui.SvgIcon
import com.diabdata.feature.databaseView.DatabaseEditionView
import com.diabdata.feature.devices.ui.DevicesScreen
import com.diabdata.feature.graphs.GraphViewer
import com.diabdata.feature.home.HomeScreen
import com.diabdata.feature.settings.sections.aboutApp.ui.AboutAppSettingScreen
import com.diabdata.feature.settings.sections.dataSettings.ui.DataSettingsScreen
import com.diabdata.feature.settings.sections.security.ui.SecuritySettingsScreen
import com.diabdata.feature.settings.ui.SettingsScreen
import com.diabdata.feature.userProfile.UserProfileViewModel
import com.diabdata.feature.userProfile.ui.UserAvatarWithMenu
import com.diabdata.feature.userProfile.ui.UserDetailsScreen
import kotlinx.coroutines.flow.StateFlow
import com.diabdata.shared.R as shared

sealed interface NavIcon {
    data class Vector(val imageVector: ImageVector, val contentDescription: String) : NavIcon
    data class Svg(val resId: Int) : NavIcon
}

data class BottomNavItem(
    val route: String, val label: Int, val selectedIcon: NavIcon, val unselectedIcon: NavIcon
)

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.P)
@Composable
fun App(
    dataViewModel: DataViewModel,
    shortcutDestination: StateFlow<String?>
) {
    // ── Window Size ──
    val windowSize = getScreenSize()

    // View Models
    val userProfileViewModel: UserProfileViewModel = hiltViewModel()

    // ── State ──
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val userDetails by userProfileViewModel.userDetails.collectAsStateWithLifecycle(initialValue = null)
    val pendingDestination by shortcutDestination.collectAsStateWithLifecycle()
    val activity = LocalActivity.current as? MainActivity
    val isProfileRoute = currentRoute == "profile"
    val isSubSettingsRoute = currentRoute?.startsWith("settings/") == true
            && currentRoute != "settings/main"

    val snackbarHostState = remember { SnackbarHostState() }

    val topAppBarSubSettingsText = when (currentRoute) {
        "settings/data" -> stringResource(shared.string.settings_data_section_title)
        "settings/security" -> stringResource(shared.string.settings_security_settings)
        "settings/about" -> stringResource(shared.string.settings_about_app_label)
        else -> ""
    }

    // ── Shortcuts ──
    LaunchedEffect(pendingDestination) {
        pendingDestination?.let { destination ->
            val validRoutes = listOf("home", "charts", "data", "devices", "settings", "profile")
            if (destination in validRoutes) {
                navController.navigate(destination) {
                    popUpTo(navController.graph.startDestinationId) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }
            activity?.consumeShortcut()
        }
    }

    // ── Nav items ──
    val items = listOf(
        BottomNavItem(
            route = "home",
            label = shared.string.home_menu_title,
            unselectedIcon = NavIcon.Svg(shared.drawable.home_icon_vector),
            selectedIcon = NavIcon.Svg(shared.drawable.home_filled_icon_vector)
        ),
        BottomNavItem(
            route = "charts",
            label = shared.string.chart_menu_title,
            unselectedIcon = NavIcon.Svg(shared.drawable.chart_icon_vector),
            selectedIcon = NavIcon.Svg(shared.drawable.chart_filled_icon_vector)
        ),
        BottomNavItem(
            route = "data",
            label = shared.string.database_management_menu_title,
            unselectedIcon = NavIcon.Svg(shared.drawable.database_icon_vector),
            selectedIcon = NavIcon.Svg(shared.drawable.database_filled_icon_vector)
        ),
        BottomNavItem(
            route = "devices",
            label = shared.string.devices_navigation_menu_title,
            unselectedIcon = NavIcon.Svg(shared.drawable.devices_icon_vector),
            selectedIcon = NavIcon.Svg(shared.drawable.devices_filled_icon_vector)
        ),
        BottomNavItem(
            route = "settings",
            label = shared.string.settings_menu_title,
            unselectedIcon = NavIcon.Svg(shared.drawable.settings_icon_vector),
            selectedIcon = NavIcon.Svg(shared.drawable.settings_filled_icon_vector)
        )
    )

    // ── Helper : navigation onClick ──
    val onNavigate: (String) -> Unit = { route ->
        navController.navigate(route) {
            popUpTo(navController.graph.startDestinationId) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    CompositionLocalProvider(LocalSnackbarHostState provides snackbarHostState) {
        when (windowSize) {
            // ┌─────────────────────────────────────────────┐
            // │  🖥️ EXPANDED (≥ 840dp) : Permanent Drawer   │
            // └─────────────────────────────────────────────┘
            ScreenSize.LARGE -> {
                if (isProfileRoute) {
                    Scaffold(
                        snackbarHost = { SnackbarHost(snackbarHostState) },
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    ) { paddingValues ->
                        DiabDataNavHost(
                            navController = navController,
                            dataViewModel = dataViewModel,
                            modifier = Modifier.padding(paddingValues)
                        )
                    }
                } else {
                    PermanentNavigationDrawer(
                        drawerContent = {
                            PermanentDrawerSheet(
                                modifier = Modifier.width(280.dp),
                                drawerContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
                            ) {
                                Spacer(Modifier.height(16.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 26.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    UserAvatarWithMenu(
                                        firstName = userDetails?.firstName,
                                        lastName = userDetails?.lastName,
                                        profilePhotoPath = userDetails?.profilePhotoPath,
                                        onEditProfile = { navController.navigate("profile") }
                                    )
                                    userDetails?.firstName?.let {
                                        Text(
                                            text = it,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }

                                Spacer(Modifier.height(16.dp))
                                HorizontalDivider(
                                    modifier = Modifier
                                        .padding(horizontal = 16.dp)
                                        .background(color = MaterialTheme.colorScheme.onPrimary)
                                )
                                Spacer(Modifier.height(12.dp))

                                val currentBackStack by navController.currentBackStackEntryAsState()
                                val currentDestination = currentBackStack?.destination

                                items.forEach { item ->
                                    val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                                            || currentDestination?.route?.startsWith(item.route) == true
                                    NavigationDrawerItem(
                                        icon = { NavItemIcon(item, selected) },
                                        label = { Text(stringResource(item.label)) },
                                        selected = selected,
                                        onClick = { onNavigate(item.route) },
                                        modifier = Modifier.padding(horizontal = 12.dp)
                                    )
                                    Spacer(Modifier.height(4.dp))
                                }
                            }
                        }
                    ) {
                        Scaffold(
                            snackbarHost = {
                                SnackbarHost(hostState = snackbarHostState)
                            },
                            containerColor = MaterialTheme.colorScheme.surfaceContainer,
                            topBar = {
                                if (isSubSettingsRoute) {
                                    TopAppBar(
                                        title = {
                                            Text(topAppBarSubSettingsText)
                                        },
                                        navigationIcon = {
                                            IconButton(onClick = { navController.popBackStack() }) {
                                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                                            }
                                        },
                                        colors = TopAppBarDefaults.topAppBarColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                                        )
                                    )
                                }
                            }
                        ) { paddingValues ->
                            DiabDataNavHost(
                                navController = navController,
                                dataViewModel = dataViewModel,
                                modifier = Modifier.padding(paddingValues)
                            )
                        }
                    }
                }
            }

            // ┌─────────────────────────────────────────────┐
            // │  📱 MEDIUM (≥ 600dp) : Navigation Rail      │
            // └─────────────────────────────────────────────┘
            ScreenSize.MEDIUM -> {
                Scaffold(
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    topBar = {
                        if (isSubSettingsRoute) {
                            TopAppBar(
                                title = {
                                    Text(topAppBarSubSettingsText)
                                },
                                navigationIcon = {
                                    IconButton(onClick = { navController.popBackStack() }) {
                                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                                    }
                                },
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                                )
                            )
                        }
                    }
                ) { paddingValues ->
                    Row(
                        modifier = Modifier
                            .padding(paddingValues)
                            .fillMaxSize()
                    ) {
                        NavigationRail(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                            header = {
                                Column(
                                    modifier = Modifier
                                        .widthIn(max = 80.dp)
                                        .padding(top = 12.dp, bottom = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    UserAvatarWithMenu(
                                        firstName = userDetails?.firstName,
                                        lastName = userDetails?.lastName,
                                        profilePhotoPath = userDetails?.profilePhotoPath,
                                        onEditProfile = { navController.navigate("profile") }
                                    )
                                    userDetails?.firstName?.let {
                                        Text(
                                            text = it,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.primary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        ) {
                            val currentBackStack by navController.currentBackStackEntryAsState()
                            val currentDestination = currentBackStack?.destination

                            items.forEach { item ->
                                val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                                        || currentDestination?.route?.startsWith(item.route) == true
                                NavigationRailItem(
                                    icon = { NavItemIcon(item, false) },
                                    label = {
                                        Text(
                                            text = stringResource(item.label),
                                            overflow = TextOverflow.Ellipsis,
                                            fontFamily = GoogleSansFlexFontFamily
                                        )
                                    },
                                    selected = selected,
                                    onClick = { onNavigate(item.route) },
                                    alwaysShowLabel = false
                                )
                            }
                        }

                        DiabDataNavHost(
                            navController = navController,
                            dataViewModel = dataViewModel,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // ┌─────────────────────────────────────────────┐
            // │  📱 COMPACT (< 600dp) : Bottom Bar          │
            // └─────────────────────────────────────────────┘
            ScreenSize.COMPACT -> {
                Scaffold(
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    topBar = {
                        when {
                            isProfileRoute -> {  }
                            isSubSettingsRoute -> {
                                TopAppBar(
                                    title = {
                                        Text(topAppBarSubSettingsText)
                                    },
                                    navigationIcon = {
                                        IconButton(onClick = { navController.popBackStack() }) {
                                            Icon(
                                                Icons.AutoMirrored.Filled.ArrowBack,
                                                contentDescription = stringResource(shared.string.common_back)
                                            )
                                        }
                                    },
                                    colors = TopAppBarDefaults.topAppBarColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                                    )
                                )
                            }
                            else -> {
                                TopAppBar(
                                    title = { Text("") },
                                    actions = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.End)
                                        ) {
                                            userDetails?.firstName?.let {
                                                Text(
                                                    text = it,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                            UserAvatarWithMenu(
                                                firstName = userDetails?.firstName,
                                                lastName = userDetails?.lastName,
                                                profilePhotoPath = userDetails?.profilePhotoPath,
                                                size = 30.dp,
                                                onEditProfile = { navController.navigate("profile") }
                                            )
                                        }
                                    },
                                    contentPadding = PaddingValues(
                                        start = 32.dp, top = 5.dp, end = 32.dp, bottom = 10.dp
                                    ),
                                    expandedHeight = 40.dp,
                                    colors = TopAppBarDefaults.topAppBarColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                                    )
                                )
                            }
                        }
                    },
                    bottomBar = {
                        if (!isProfileRoute && !isSubSettingsRoute) {
                            NavigationBar(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                            ) {
                                val currentBackStack by navController.currentBackStackEntryAsState()
                                val currentDestination = currentBackStack?.destination
                                items.forEach { item ->
                                    val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                                            || currentDestination?.route?.startsWith(item.route) == true
                                    NavigationBarItem(
                                        icon = { NavItemIcon(item, selected) },
                                        label = { Text(stringResource(item.label)) },
                                        selected = selected,
                                        onClick = { onNavigate(item.route) }
                                    )
                                }
                            }
                        }
                    }
                ) { paddingValues ->
                    DiabDataNavHost(
                        navController = navController,
                        dataViewModel = dataViewModel,
                        modifier = Modifier.padding(
                            if (isProfileRoute) PaddingValues(
                                start = 0.dp,
                                top = 0.dp,
                                end = 0.dp,
                                bottom = paddingValues.calculateBottomPadding()
                            )
                            else paddingValues
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun NavItemIcon(item: BottomNavItem, selected: Boolean) {
    val icon = if (selected) item.selectedIcon else item.unselectedIcon
    val tint = if (selected) MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.onSurfaceVariant

    when (icon) {
        is NavIcon.Vector -> Icon(
            icon.imageVector,
            icon.contentDescription,
            tint = tint
        )

        is NavIcon.Svg -> SvgIcon(
            resId = icon.resId,
            color = tint
        )
    }
}

@RequiresApi(Build.VERSION_CODES.P)
@Composable
fun DiabDataNavHost(
    navController: NavHostController,
    dataViewModel: DataViewModel,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = "home",
        modifier = modifier,
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = tween(300)
            ) + fadeIn(animationSpec = tween(300))
        },
        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { -it },
                animationSpec = tween(300)
            ) + fadeOut(animationSpec = tween(300))
        },
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { -it },
                animationSpec = tween(300)
            ) + fadeIn(animationSpec = tween(300))
        },
        popExitTransition = {
            slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = tween(300)
            ) + fadeOut(animationSpec = tween(300))
        }
    ) {
        composable("home") { HomeScreen(dataViewModel) }
        composable("charts") { GraphViewer(viewModel = dataViewModel) }
        composable("data") { DatabaseEditionView(dataViewModel) }
        composable("devices") { DevicesScreen(dataViewModel) }
        composable("profile") {
            UserDetailsScreen { navController.popBackStack() }
        }

        // ── Settings nested graph ──
        navigation(
            startDestination = "settings/main",
            route = "settings"
        ) {
            composable("settings/main") {
                SettingsScreen(
                    dataViewModel = dataViewModel,
                    onNavigateToDataSettings = {
                        navController.navigate("settings/data")
                    },
                    onNavigateToSecuritySettings = {
                        navController.navigate("settings/security")
                    },
                    onNavigateToAboutScreen = {
                        navController.navigate("settings/about")
                    }
                )
            }
            composable("settings/data") {
                DataSettingsScreen(
                    dataViewModel = dataViewModel
                )
            }
            composable("settings/security") {
                SecuritySettingsScreen()
            }
            composable("settings/about") {
                AboutAppSettingScreen()
            }
        }
    }
}