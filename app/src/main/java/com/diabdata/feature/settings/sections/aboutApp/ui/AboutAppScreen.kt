package com.diabdata.feature.settings.sections.aboutApp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.diabdata.BuildConfig
import com.diabdata.core.ui.theme.DiabDataTheme
import com.diabdata.core.ui.theme.ExtendedTheme
import com.diabdata.core.ui.theme.GoogleSansFlexFontFamily
import com.diabdata.feature.settings.sections.dataSettings.ui.components.locale
import com.diabdata.feature.settings.ui.components.AppInfoCard
import com.diabdata.feature.settings.ui.components.changelog.ChangelogDialog
import com.diabdata.feature.settings.ui.components.licenses.LicensesDialog

@Composable
fun AboutAppSettingScreen() {
    var showChangeLogDialog by remember { mutableStateOf(false) }
    var showLicensesDialog by remember { mutableStateOf(false) }
    val versionName = BuildConfig.VERSION_NAME

    @Suppress("KotlinConstantConditions", "SimplifyBooleanWithConstants")
    val isBeta = BuildConfig.APP_VARIANT == "development"

    Column(
        modifier = Modifier
            .padding(horizontal = 32.dp)
            .padding(top = 32.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = spacedBy(32.dp)
    ) {
        @Suppress("KotlinConstantConditions")
        AppInfoCard(
            isBeta = isBeta,
            versionName = versionName,
            showChangeLogDialog = { showChangeLogDialog = true },
            showLicensesDialog = { showLicensesDialog = true }
        )
    }

    if (showChangeLogDialog) {
        ChangelogDialog(onDismiss = { showChangeLogDialog = false })
    }

    if (showLicensesDialog) {
        LicensesDialog(onDismiss = { showLicensesDialog = false })
    }
}

@Preview(name = "Interactive", showBackground = true, locale = locale)
@Composable
fun AboutAppSettingScreenInteractivePreview() {
    var darkTheme by remember { mutableStateOf(false) }
    var betaMode by remember { mutableStateOf(false) }

    DiabDataTheme(dynamicColor = false, darkTheme = darkTheme) {
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .padding(24.dp),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = spacedBy(16.dp)
            ) {
                AboutAppSettingScreen()

                Column(
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(vertical = 12.dp, horizontal = 32.dp),
                    verticalArrangement = spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Dark Theme",
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.labelLarge,
                            fontFamily = GoogleSansFlexFontFamily,
                            fontWeight = FontWeight(800),
                        )
                        Switch(
                            checked = darkTheme,
                            onCheckedChange = { darkTheme = it }
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Beta Mode",
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.labelLarge,
                            fontFamily = GoogleSansFlexFontFamily,
                            fontWeight = FontWeight(800),
                        )
                        Switch(
                            checked = betaMode,
                            onCheckedChange = { betaMode = it },
                            colors = SwitchDefaults.colors(
                                checkedTrackColor = ExtendedTheme.colors.betaContainer
                            )
                        )
                    }
                }
            }
        }
    }
}