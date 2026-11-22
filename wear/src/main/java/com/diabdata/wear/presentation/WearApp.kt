package com.diabdata.wear.presentation

import androidx.compose.runtime.Composable
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.diabdata.wear.presentation.home.HomeScreen
import com.diabdata.wear.presentation.licenses.LicensesScreen
import com.diabdata.wear.presentation.theme.DiabDataTheme

@Composable
fun WearApp() {
    DiabDataTheme {
        val navController = rememberSwipeDismissableNavController()

        AppScaffold {
            SwipeDismissableNavHost(navController, "home") {
                composable("home") {
                    HomeScreen(
                        onLicensesClick = { navController.navigate("licenses") }
                    )
                }
                composable("licenses") {
                    LicensesScreen()
                }
            }
        }
    }
}
